package ma.enset.billing;
import feign.FeignException; import java.util.Map; import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
@RestControllerAdvice public class FeignErrors { @ExceptionHandler(FeignException.class) ResponseEntity<?> remote(FeignException e) {return ResponseEntity.status(e.status()==404?HttpStatus.BAD_REQUEST:HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("message",e.status()==404?"Client ou produit introuvable":"Service client ou inventaire indisponible"));} }
