package ma.enset.billing;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Bill {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Instant createdAt = Instant.now();
  private Long customerId;
  private String customerName;
  private String customerEmail;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "bill_id")
  private List<ProductItem> items = new ArrayList<>();

  public BigDecimal getTotal() {
    return items.stream().map(ProductItem::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
