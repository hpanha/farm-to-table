package farm_to_table.farm_to_table.Service;

import farm_to_table.farm_to_table.DTO.Order.OrderRequestDto;
import farm_to_table.farm_to_table.DTO.Order.OrderResponseDto;
import farm_to_table.farm_to_table.DTO.Order.OrderStatusDto;
import farm_to_table.farm_to_table.DTO.Order.OrderUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponseDto create(OrderRequestDto dto);

    OrderResponseDto update(Long id, OrderUpdateDto dto);

    OrderResponseDto updateStatus(Long id, OrderStatusDto dto);

    /** Dev bypass: move an order to DELIVERED through its steps. Needs the dev flag and an admin or a farmer owning a line. */
    OrderResponseDto devDeliver(Long id, String actorUsername);

    void delete(Long id);

    OrderResponseDto getById(Long id);

    OrderResponseDto getByOrderNumber(String orderNumber);

    Page<OrderResponseDto> search(String customerId, String status, String keyword, Pageable pageable);
}