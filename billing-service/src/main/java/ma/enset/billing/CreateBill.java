package ma.enset.billing;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.util.List;
public record CreateBill(@NotNull @Positive Long customerId,@NotEmpty List<@Valid Line> items) { public record Line(@NotNull @Positive Long productId,@NotNull @Positive Integer quantity) {} }
