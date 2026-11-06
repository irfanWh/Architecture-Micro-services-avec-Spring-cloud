package ma.enset.customer;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/customers")
public class CustomerController {
  private final CustomerRepository repo;

  public CustomerController(CustomerRepository repo) {
    this.repo = repo;
  }

  @GetMapping
  public List<Customer> all() {
    return repo.findAll();
  }

  @GetMapping("/{id}")
  public Customer one(@PathVariable Long id) {
    return repo.findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer introuvable"));
  }

  @PostMapping
  public ResponseEntity<Customer> create(@Valid @RequestBody Customer value) {
    value.setId(null);
    var saved = repo.save(value);
    return ResponseEntity.created(java.net.URI.create("/customers/" + saved.getId())).body(saved);
  }

  @PutMapping("/{id}")
  public Customer update(@PathVariable Long id, @Valid @RequestBody Customer value) {
    one(id);
    value.setId(id);
    return repo.save(value);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    repo.delete(one(id));
  }
}
