package ma.enset.inventory;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank private String name;

  @NotNull
  @DecimalMin("0.01")
  @Digits(integer = 17, fraction = 2)
  private java.math.BigDecimal price;

  @NotNull @PositiveOrZero private Integer quantity;
}
