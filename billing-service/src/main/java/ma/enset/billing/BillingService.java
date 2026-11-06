package ma.enset.billing;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import org.springframework.http.HttpStatus; import org.springframework.web.server.ResponseStatusException; import java.util.HashSet;
@Service public class BillingService {
 private final BillRepository repo; private final CustomerClient customers; private final ProductClient products;
 public BillingService(BillRepository repo,CustomerClient customers,ProductClient products) {this.repo=repo;this.customers=customers;this.products=products;}
 @Transactional public Bill create(CreateBill request) {
 var customer=customers.find(request.customerId()); var bill=new Bill();bill.setCustomerId(customer.id());bill.setCustomerName(customer.name());bill.setCustomerEmail(customer.email());
 var seen=new HashSet<Long>();
 for(var line:request.items()) {if(!seen.add(line.productId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Produit duplique");
 var product=products.find(line.productId());
 if(product.quantity()<line.quantity()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Quantite superieure au stock disponible");
 bill.getItems().add(new ProductItem(null,product.id(),product.name(),product.price(),line.quantity()));}
 return repo.save(bill);
 }
}
