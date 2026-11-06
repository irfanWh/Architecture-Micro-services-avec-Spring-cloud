package ma.enset.billing;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:billingapi;DB_CLOSE_DELAY=-1",
      "spring.cloud.config.enabled=false",
      "eureka.client.enabled=false"
    })
@AutoConfigureMockMvc
class BillingApiTest {
  @Autowired MockMvc mvc;
  @MockitoBean CustomerClient customers;
  @MockitoBean ProductClient products;

  @Test
  void rejectsInvalidRequestBeforeCallingRemoteServices() throws Exception {
    mvc.perform(
            post("/bills")
                .contentType("application/json")
                .content("{\"customerId\":1,\"items\":[{\"productId\":1,\"quantity\":0}]}"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(customers, products);
  }

  @Test
  void translatesMissingCustomerToBadRequest() throws Exception {
    var error = mock(FeignException.class);
    when(error.status()).thenReturn(404);
    when(customers.find(999L)).thenThrow(error);
    mvc.perform(
            post("/bills")
                .contentType("application/json")
                .content("{\"customerId\":999,\"items\":[{\"productId\":1,\"quantity\":1}]}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Client ou produit introuvable"));
  }

  @Test
  void reportsDependencyOutage() throws Exception {
    var error = mock(FeignException.class);
    when(error.status()).thenReturn(503);
    when(customers.find(1L)).thenThrow(error);
    mvc.perform(
            post("/bills")
                .contentType("application/json")
                .content("{\"customerId\":1,\"items\":[{\"productId\":1,\"quantity\":1}]}"))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void missingBillReturnsNotFound() throws Exception {
    mvc.perform(get("/bills/99999999")).andExpect(status().isNotFound());
  }
}
