package ma.enset.billing;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "customer-service")
public interface CustomerClient {
  @GetMapping("/customers/{id}")
  Customer find(@PathVariable Long id);

  record Customer(Long id, String name, String email) {}
}
