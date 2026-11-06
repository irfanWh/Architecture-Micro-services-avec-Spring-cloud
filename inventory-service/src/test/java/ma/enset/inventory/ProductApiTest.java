package ma.enset.inventory;
import org.junit.jupiter.api.Test; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; import org.springframework.boot.test.context.SpringBootTest; import org.springframework.test.web.servlet.MockMvc; import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*; import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1","eureka.client.enabled=false","spring.cloud.config.enabled=false"}) @AutoConfigureMockMvc
class ProductApiTest { @Autowired MockMvc mvc;
 @Test void crudAndValidation() throws Exception {
 var json="{\"name\":\"Clavier\",\"price\":120.50,\"quantity\":10}";
 var response=mvc.perform(post("/products").contentType("application/json").content(json)).andExpect(status().isCreated()).andReturn();
 var location=response.getResponse().getHeader("Location");
 mvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Clavier"));
 mvc.perform(put(location).contentType("application/json").content(json)).andExpect(status().isOk());
 mvc.perform(delete(location)).andExpect(status().isNoContent());mvc.perform(get(location)).andExpect(status().isNotFound());
 mvc.perform(post("/products").contentType("application/json").content("{}")).andExpect(status().isBadRequest());
 } }
