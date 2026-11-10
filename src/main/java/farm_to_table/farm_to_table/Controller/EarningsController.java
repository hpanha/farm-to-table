package farm_to_table.farm_to_table.Controller;

import farm_to_table.farm_to_table.DTO.Earnings.EarningsDtos.Cashout;
import farm_to_table.farm_to_table.DTO.Earnings.EarningsDtos.CashoutRequest;
import farm_to_table.farm_to_table.DTO.Earnings.EarningsDtos.CashoutStatusUpdate;
import farm_to_table.farm_to_table.DTO.Earnings.EarningsDtos.Summary;
import farm_to_table.farm_to_table.Service.FarmerEarningsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EarningsController {

    private final FarmerEarningsService service;

    /** GET /api/farmer/earnings: gross, commission, net, and what is available to cash out. */
    @GetMapping("/api/farmer/earnings")
    public Summary earnings(Authentication authentication) {
        return service.summaryFor(authentication.getName());
    }

    /** POST /api/farmer/cashouts  body { "warehouseId": 1 }: request cash at this warehouse. */
    @PostMapping("/api/farmer/cashouts")
    @ResponseStatus(HttpStatus.CREATED)
    public Cashout requestCashout(@RequestBody CashoutRequest body, Authentication authentication) {
        if (body == null || body.warehouseId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "warehouseId is required");
        }
        return service.requestCashout(authentication.getName(), body.warehouseId());
    }

    /** GET /api/farmer/pending-orders: this farmer's sales whose order is not delivered yet. */
    @GetMapping("/api/farmer/pending-orders")
    public List<farm_to_table.farm_to_table.DTO.Earnings.EarningsDtos.PendingOrder> pendingOrders(
            Authentication authentication) {
        return service.pendingOrdersFor(authentication.getName());
    }

    /** POST /api/farmer/cashouts/{id}/dev-pay: dev bypass to mark a cash-out paid (flag-gated). */
    @PostMapping("/api/farmer/cashouts/{id}/dev-pay")
    public Cashout devPay(@PathVariable Long id, Authentication authentication) {
        return service.devMarkPaid(id, authentication.getName());
    }

    @GetMapping("/api/farmer/cashouts")
    public List<Cashout> myCashouts(Authentication authentication) {
        return service.myCashouts(authentication.getName());
    }

    /** GET /api/cashouts (ADMIN, warehouse staff): all cash-out requests. */
    @GetMapping("/api/cashouts")
    public List<Cashout> allCashouts(@RequestParam(required = false) String status) {
        return service.allCashouts(status);
    }

    /** PATCH /api/cashouts/{id}/status  body { "status": "PAID" } (ADMIN, at the warehouse). */
    @PatchMapping("/api/cashouts/{id}/status")
    public Cashout markPaid(@PathVariable Long id, @RequestBody CashoutStatusUpdate body) {
        if (body == null || !"PAID".equalsIgnoreCase(body.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status must be PAID");
        }
        return service.markPaid(id);
    }
}
