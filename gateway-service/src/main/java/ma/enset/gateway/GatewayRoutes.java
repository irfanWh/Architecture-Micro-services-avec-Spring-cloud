package ma.enset.gateway;
import org.springframework.context.annotation.*; import org.springframework.cloud.gateway.route.*; import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
@Configuration @Profile("!static")
public class GatewayRoutes { @Bean RouteLocator routes(RouteLocatorBuilder builder) {return builder.routes()
.route("customers",r -> r.path("/customers/**").uri("lb://customer-service"))
.route("products",r -> r.path("/products/**").uri("lb://inventory-service"))
.route("bills",r -> r.path("/bills/**").uri("lb://billing-service")).build();}}
