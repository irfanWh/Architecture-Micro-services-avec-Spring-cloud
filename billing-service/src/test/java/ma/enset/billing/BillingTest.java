package ma.enset.billing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:billingtest;DB_CLOSE_DELAY=-1",
      "spring.cloud.config.enabled=false",
      "eureka.client.enabled=false"
    })
class BillingTest {
  @Autowired BillingService service;
  @Autowired BillRepository repo;
  @MockitoBean CustomerClient customers;
  @MockitoBean ProductClient products;

  @BeforeEach
  void setup() {
    repo.deleteAll();
    when(customers.find(1L)).thenReturn(new CustomerClient.Customer(1L, "Amina", "a@example.com"));
    when(products.find(1L))
        .thenReturn(new ProductClient.Product(1L, "Clavier", new BigDecimal("125.50"), 10));
  }

  @Test
  void createsSnapshotAndExactTotal() {
    var bill = service.create(new CreateBill(1L, List.of(new CreateBill.Line(1L, 3))));
    assertEquals(new BigDecimal("376.50"), bill.getTotal());
    assertEquals("Amina", repo.findById(bill.getId()).orElseThrow().getCustomerName());
    verify(products).find(1L);
  }

  @Test
  void rejectsInsufficientStockWithoutSaving() {
    assertThrows(
        ResponseStatusException.class,
        () -> service.create(new CreateBill(1L, List.of(new CreateBill.Line(1L, 11)))));
    assertEquals(0, repo.count());
  }

  @Test
  void rejectsDuplicateProductsAtomically() {
    assertThrows(
        ResponseStatusException.class,
        () ->
            service.create(
                new CreateBill(
                    1L, List.of(new CreateBill.Line(1L, 2), new CreateBill.Line(1L, 2)))));
    assertEquals(0, repo.count());
  }
}
