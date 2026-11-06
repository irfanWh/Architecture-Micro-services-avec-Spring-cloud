package ma.enset.inventory;
import jakarta.validation.Valid; import java.util.List; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/products")
public class ProductController {
 private final ProductRepository repo; public ProductController(ProductRepository repo) {this.repo=repo;}
 @GetMapping public List<Product> all() { return repo.findAll(); }
 @GetMapping("/{id}") public Product one(@PathVariable Long id) {return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Product introuvable"));}
 @PostMapping public ResponseEntity<Product> create(@Valid @RequestBody Product value) {value.setId(null); var saved=repo.save(value);return ResponseEntity.created(java.net.URI.create("/products/"+saved.getId())).body(saved);}
 @PutMapping("/{id}") public Product update(@PathVariable Long id,@Valid @RequestBody Product value) {one(id);value.setId(id);return repo.save(value);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) {repo.delete(one(id));}
}
