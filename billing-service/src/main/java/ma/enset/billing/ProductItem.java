package ma.enset.billing;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long productId;
  private String productName;

  @Column(precision = 19, scale = 2)
  private BigDecimal unitPrice;

  private Integer quantity;

  public BigDecimal getTotal() {
    return unitPrice.multiply(BigDecimal.valueOf(quantity));
  }
}
