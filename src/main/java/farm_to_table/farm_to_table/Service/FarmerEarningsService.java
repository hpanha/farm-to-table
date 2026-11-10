package farm_to_table.farm_to_table.Service;

import farm_to_table.farm_to_table.DTO.Earnings.EarningsDtos.Cashout;
import farm_to_table.farm_to_table.DTO.Earnings.EarningsDtos.Line;
import farm_to_table.farm_to_table.DTO.Earnings.EarningsDtos.PendingOrder;
import farm_to_table.farm_to_table.Repository.OrderRepository;
import farm_to_table.farm_to_table.model.OrderEntity;
import farm_to_table.farm_to_table.DTO.Earnings.EarningsDtos.Summary;
import farm_to_table.farm_to_table.Repository.CashoutRepository;
import farm_to_table.farm_to_table.Repository.OrderDetailRepository;
import farm_to_table.farm_to_table.Repository.ProductRepository;
import farm_to_table.farm_to_table.Repository.UserRepository;
import farm_to_table.farm_to_table.Repository.WareHouseRepository;
import farm_to_table.farm_to_table.model.CashoutEntity;
import farm_to_table.farm_to_table.model.OrderDetailEntity;
import farm_to_table.farm_to_table.model.UserEntity;
import farm_to_table.farm_to_table.model.WareHouseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * Farmer earnings from delivered orders (commission is already snapshotted on each line),
 * and cash-out requests that the farmer collects in cash at a warehouse.
 */
@Service
@RequiredArgsConstructor
public class FarmerEarningsService {

    static final String REQUESTED = "REQUESTED";
    static final String PAID = "PAID";

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CashoutRepository cashoutRepository;
    private final WareHouseRepository warehouseRepository;
    private final OrderRepository orderRepository;

    /** Dev/test flag (same as the other bypasses). */
    @org.springframework.beans.factory.annotation.Value("${app.dev.bypass-approval:false}")
    private boolean devBypass;

    @Transactional(readOnly = true)
    public Summary summaryFor(String username) {
        String farmerId = farmerIdOf(username);
        List<OrderDetailEntity> lines = deliveredLinesOf(farmerId);
        Map<Long, CashoutEntity> cashouts = cashoutRepository.findByFarmerUserIdOrderByIdDesc(farmerId).stream()
                .collect(Collectors.toMap(CashoutEntity::getId, c -> c, (a, b) -> a, LinkedHashMap::new));

        List<Line> lineDtos = lines.stream().map(l -> toLine(l, cashouts)).toList();

        return new Summary(
                round2(sum(lines, l -> nz(l.getSubTotal()))),
                round2(sum(lines, l -> nz(l.getCommissionAmount()))),
                round2(sum(lines, l -> nz(l.getFarmerNetAmount()))),
                round2(sum(lines, l -> l.getCashoutId() == null ? nz(l.getFarmerNetAmount()) : 0)),
                round2(sum(lines, l -> hasStatus(l, cashouts, REQUESTED) ? nz(l.getFarmerNetAmount()) : 0)),
                round2(sum(lines, l -> hasStatus(l, cashouts, PAID) ? nz(l.getFarmerNetAmount()) : 0)),
                lineDtos,
                cashouts.values().stream().map(this::toCashout).toList());
    }

    @Transactional
    public Cashout requestCashout(String username, Long warehouseId) {
        String farmerId = farmerIdOf(username);
        WareHouseEntity warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Warehouse not found"));

        List<OrderDetailEntity> available = deliveredLinesOf(farmerId).stream()
                .filter(l -> l.getCashoutId() == null)
                .toList();
        if (available.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No delivered sales are available to cash out");
        }

        CashoutEntity cashout = new CashoutEntity();
        cashout.setFarmerUserId(farmerId);
        cashout.setWarehouseId(warehouse.getId());
        cashout.setWarehouseName(warehouse.getName());
        cashout.setWarehouseAddress(warehouse.getAddress());
        cashout.setAmount(round2(sum(available, l -> nz(l.getFarmerNetAmount()))));
        cashout.setStatus(REQUESTED);
        cashout.setReference("TMP-" + UUID.randomUUID());
        CashoutEntity saved = cashoutRepository.save(cashout);
        saved.setReference("CO-" + saved.getId());
        saved = cashoutRepository.save(saved);

        for (OrderDetailEntity line : available) {
            line.setCashoutId(saved.getId());
            orderDetailRepository.save(line);
        }
        return toCashout(saved);
    }

    /** Sale lines of this farmer whose order is still PENDING, CONFIRMED or SHIPPING. */
    @Transactional(readOnly = true)
    public List<PendingOrder> pendingOrdersFor(String username) {
        String farmerId = farmerIdOf(username);
        return orderDetailRepository.findAll().stream()
                .filter(l -> l.getOrderId() != null)
                .filter(l -> farmerId.equals(ownerOf(l.getProductId())))
                .sorted(Comparator.comparing(OrderDetailEntity::getId))
                .map(l -> {
                    Long orderId = parseId(l.getOrderId());
                    OrderEntity order = orderId == null ? null : orderRepository.findById(orderId).orElse(null);
                    if (order == null) return null;
                    String status = order.getOrderStatus();
                    if ("DELIVERED".equals(status) || "CANCELLED".equals(status)) return null;
                    Long pid = parseId(l.getProductId());
                    String productName = pid == null ? null
                            : productRepository.findById(pid).map(p -> p.getName()).orElse(null);
                    return new PendingOrder(order.getId(), order.getOrderNumber(), status,
                            productName, l.getQty(), l.getSubTotal());
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Cashout> myCashouts(String username) {
        return cashoutRepository.findByFarmerUserIdOrderByIdDesc(farmerIdOf(username)).stream()
                .map(this::toCashout)
                .toList();
    }

    /** Admin list. status is optional (REQUESTED or PAID). */
    @Transactional(readOnly = true)
    public List<Cashout> allCashouts(String status) {
        List<CashoutEntity> rows = (status == null || status.isBlank())
                ? cashoutRepository.findAllByOrderByIdDesc()
                : cashoutRepository.findByStatusOrderByIdDesc(status.trim().toUpperCase());
        return rows.stream().map(this::toCashout).toList();
    }

    /**
     * Dev bypass: the farmer (for their own request) or an admin marks a cash-out as paid,
     * without going through the warehouse. Needs the dev flag.
     */
    @Transactional
    public Cashout devMarkPaid(Long id, String actorUsername) {
        if (!devBypass) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Dev bypass is disabled on the server");
        }
        UserEntity actor = userRepository.findByUsername(actorUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Log in first"));
        CashoutEntity cashout = cashoutRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cash-out not found"));
        boolean admin = actor.getRolesId() != null && actor.getRolesId() == 1L;
        if (!admin && !String.valueOf(actor.getId()).equals(cashout.getFarmerUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only mark your own cash-outs paid");
        }
        return markPaid(id);
    }

    /** Admin at the warehouse: the cash has been handed to the farmer. */
    @Transactional
    public Cashout markPaid(Long id) {
        CashoutEntity cashout = cashoutRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cash-out not found"));
        if (!REQUESTED.equals(cashout.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only REQUESTED cash-outs can be paid (current: " + cashout.getStatus() + ")");
        }
        cashout.setStatus(PAID);
        cashout.setPaidAt(LocalDateTime.now());
        return toCashout(cashoutRepository.save(cashout));
    }

    // ---------- helpers ----------

    private String farmerIdOf(String username) {
        return userRepository.findByUsername(username)
                .map(u -> String.valueOf(u.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Log in first"));
    }

    /** Delivered lines (commission snapshot present) whose product belongs to this farmer. */
    private List<OrderDetailEntity> deliveredLinesOf(String farmerId) {
        return orderDetailRepository.findAll().stream()
                .filter(l -> l.getCommissionAmount() != null)
                .filter(l -> farmerId.equals(ownerOf(l.getProductId())))
                .sorted(Comparator.comparing(OrderDetailEntity::getId))
                .toList();
    }

    private String ownerOf(String productId) {
        Long id = parseId(productId);
        if (id == null) return null;
        return productRepository.findById(id).map(p -> p.getUserId()).orElse(null);
    }

    private static Long parseId(String raw) {
        if (raw == null) return null;
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean hasStatus(OrderDetailEntity l, Map<Long, CashoutEntity> cashouts, String status) {
        if (l.getCashoutId() == null) return false;
        CashoutEntity c = cashouts.get(l.getCashoutId());
        return c != null && status.equals(c.getStatus());
    }

    private Line toLine(OrderDetailEntity l, Map<Long, CashoutEntity> cashouts) {
        CashoutEntity c = l.getCashoutId() == null ? null : cashouts.get(l.getCashoutId());
        Long pid = parseId(l.getProductId());
        String productName = pid == null ? null
                : productRepository.findById(pid).map(p -> p.getName()).orElse(null);
        return new Line(l.getId(), l.getOrderId(), l.getProductId(), productName, l.getQty(),
                l.getSubTotal(), l.getCommissionRate(), l.getCommissionAmount(), l.getFarmerNetAmount(),
                l.getCashoutId(), c == null ? null : c.getStatus(), l.getUpdatedAt());
    }

    private Cashout toCashout(CashoutEntity c) {
        return new Cashout(c.getId(), c.getAmount(), c.getStatus(), c.getReference(),
                c.getWarehouseId(), c.getWarehouseName(), c.getWarehouseAddress(),
                c.getCreatedAt(), c.getPaidAt());
    }

    private static <T> double sum(List<T> items, ToDoubleFunction<T> f) {
        return items.stream().mapToDouble(f).sum();
    }

    private static double nz(Double d) {
        return d == null ? 0.0 : d;
    }

    private static Double round2(double v) {
        return CommissionService.round2(v);
    }
}
