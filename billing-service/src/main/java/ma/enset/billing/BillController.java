package ma.enset.billing;
import jakarta.validation.Valid; import java.util.List; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/bills") public class BillController {
 private final BillRepository repo;private final BillingService service;
 public BillController(BillRepository repo,BillingService service) {this.repo=repo;this.service=service;}
 @GetMapping public List<Bill> all() {return repo.findAll();}
 @GetMapping("/{id}") public Bill one(@PathVariable Long id) {return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Facture introuvable"));}
 @GetMapping("/{id}/full") public Bill full(@PathVariable Long id) {return one(id);}
 @PostMapping public ResponseEntity<Bill> create(@Valid @RequestBody CreateBill request) {var bill=service.create(request);return ResponseEntity.created(java.net.URI.create("/bills/"+bill.getId())).body(bill);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) {repo.delete(one(id));}
}
