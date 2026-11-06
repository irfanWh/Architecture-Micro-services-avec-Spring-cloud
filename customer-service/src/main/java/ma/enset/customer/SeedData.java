package ma.enset.customer;
import org.springframework.context.annotation.*; import org.springframework.boot.CommandLineRunner; import java.math.BigDecimal;
@Configuration public class SeedData { @Bean CommandLineRunner seed(CustomerRepository repo) {return args -> {if(repo.count()==0) {repo.save(new Customer(null,"Amina Bennani","amina@example.com")); repo.save(new Customer(null,"Youssef Alami","youssef@example.com"));}};} }
