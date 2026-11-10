package farm_to_table.farm_to_table.DTO.Earnings;

import java.time.LocalDateTime;
import java.util.List;

public final class EarningsDtos {
    private EarningsDtos() {}

    /** One delivered order line that belongs to the farmer. */
    public record Line(Long orderDetailId, String orderId, String productId, String productName,
                       Integer qty, Double subTotal, Double commissionRate, Double commissionAmount,
                       Double farmerNetAmount, Long cashoutId, String cashoutStatus, LocalDateTime deliveredAt) {}

    public record Cashout(Long id, Double amount, String status, String reference,
                          Long warehouseId, String warehouseName, String warehouseAddress,
                          LocalDateTime requestedAt, LocalDateTime paidAt) {}

    public record Summary(Double grossTotal, Double commissionTotal, Double netTotal,
                          Double availableAmount, Double pendingAmount, Double paidAmount,
                          List<Line> lines, List<Cashout> cashouts) {}

    /** A farmer's sale line whose order is not delivered or cancelled yet. */
    public record PendingOrder(Long orderId, String orderNumber, String orderStatus, String productName,
                               Integer qty, Double subTotal) {}

    public record CashoutRequest(Long warehouseId) {}

    public record CashoutStatusUpdate(String status) {}
}
