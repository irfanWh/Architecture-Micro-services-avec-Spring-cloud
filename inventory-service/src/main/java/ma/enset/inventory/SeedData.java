package ma.enset.inventory;

import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;

@Configuration
public class SeedData {
  @Bean
  CommandLineRunner seed(ProductRepository repo) {
    return args -> {
      if (repo.count() == 0) {
        repo.save(new Product(null, "Ordinateur portable", new BigDecimal("8500.00"), 20));
        repo.save(new Product(null, "Clavier mecanique", new BigDecimal("450.00"), 50));
        repo.save(new Product(null, "Ecran 27 pouces", new BigDecimal("2400.00"), 15));
      }
    };
  }
}
