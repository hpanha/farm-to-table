package farm_to_table.farm_to_table.Config;

import farm_to_table.farm_to_table.Repository.CategoryRepository;
import farm_to_table.farm_to_table.Repository.PaymentMethodRepository;
import farm_to_table.farm_to_table.Repository.ProductRepository;
import farm_to_table.farm_to_table.Repository.RoleRepository;
import farm_to_table.farm_to_table.model.CategoryEntity;
import farm_to_table.farm_to_table.model.PaymentMethodEntity;
import farm_to_table.farm_to_table.model.ProductEntity;
import farm_to_table.farm_to_table.model.RoleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
@Order(1)
@RequiredArgsConstructor
public class MasterDataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedRole(1L, "ADMIN", "System Administrator");
        seedRole(2L, "FARMER", "Produce Grower & Supplier");
        seedRole(3L, "BUYER", "Marketplace Customer");

        seedCategories();
        seedPaymentMethods();
        seedSampleProducts();
    }

    private void seedRole(Long id, String name, String description) {
        if (!roleRepository.existsById(id)) {
            RoleEntity r = new RoleEntity();
            r.setId(id);
            r.setName(name);
            r.setDescription(description);
            roleRepository.save(r);
        }
    }

    private void seedCategories() {
        seedCategory(1L, "Vegetables", "Fresh organic vegetables from local growers", "ACTIVE");
        seedCategory(2L, "Fruits", "Sweet seasonal tropical fruits", "ACTIVE");
        seedCategory(3L, "Grains & Rice", "Locally harvested fragrant rice and staples", "ACTIVE");
        seedCategory(4L, "Herbs & Spices", "Fresh aromatic herbs and organic spices", "ACTIVE");
    }

    private void seedCategory(Long id, String name, String desc, String status) {
        if (!categoryRepository.existsById(id)) {
            CategoryEntity c = new CategoryEntity();
            c.setId(id);
            c.setName(name);
            c.setDescription(desc);
            c.setStatus(status);
            categoryRepository.save(c);
        }
    }

    private void seedPaymentMethods() {
        seedPaymentMethod(1L, "KHQR", "Bakong / KHQR", "Scan to pay with any banking app");
        seedPaymentMethod(2L, "COD", "Cash on Delivery", "Pay upon receipt of goods");
        seedPaymentMethod(3L, "CARD", "Credit / Debit Card", "Visa / Mastercard");
    }

    private void seedPaymentMethod(Long id, String code, String title, String description) {
        if (!paymentMethodRepository.existsById(id)) {
            PaymentMethodEntity p = new PaymentMethodEntity();
            p.setId(id);
            p.setCode(code);
            p.setTitle(title);
            p.setDescription(description);
            paymentMethodRepository.save(p);
        }
    }

    private void seedSampleProducts() {
        if (productRepository.count() == 0) {
            ProductEntity p1 = new ProductEntity();
            p1.setUserId("1");
            p1.setCategoryId("1");
            p1.setName("Organic Battambang Oranges");
            p1.setQty(120);
            p1.setUnitPrice(3.50);
            p1.setFarmerPrice(2.80);
            p1.setExpectedHavestDate(new Date());
            p1.setExpiredDate(new Date(System.currentTimeMillis() + 864000000L));
            p1.setLocation("Battambang Province");
            productRepository.save(p1);

            ProductEntity p2 = new ProductEntity();
            p2.setUserId("1");
            p2.setCategoryId("1");
            p2.setName("Fresh Romdoul Jasmine Rice");
            p2.setQty(250);
            p2.setUnitPrice(1.20);
            p2.setFarmerPrice(0.95);
            p2.setExpectedHavestDate(new Date());
            p2.setExpiredDate(new Date(System.currentTimeMillis() + 2592000000L));
            p2.setLocation("Takeo Province");
            productRepository.save(p2);

            ProductEntity p3 = new ProductEntity();
            p3.setUserId("1");
            p3.setCategoryId("2");
            p3.setName("Kampot Red Dragon Fruit");
            p3.setQty(80);
            p3.setUnitPrice(2.20);
            p3.setFarmerPrice(1.70);
            p3.setExpectedHavestDate(new Date());
            p3.setExpiredDate(new Date(System.currentTimeMillis() + 604800000L));
            p3.setLocation("Kampot Province");
            productRepository.save(p3);
        }
    }
}
