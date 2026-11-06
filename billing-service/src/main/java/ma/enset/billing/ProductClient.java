package ma.enset.billing;
import java.math.BigDecimal; import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*;
@FeignClient(name="inventory-service") public interface ProductClient { @GetMapping("/products/{id}") Product find(@PathVariable Long id); record Product(Long id,String name,BigDecimal price,Integer quantity) {} }
