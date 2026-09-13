package com.gateway.service.config;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;
import java.time.Duration;
@Configuration
public class AppConfig {
    @Bean
    public KeyResolver keyResolver(){
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
    }
    @Bean
    public RedisRateLimiter redisRateLimiter(){
        return new RedisRateLimiter(10,20, 1);
    }
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder routeLocatorBuilder){

        return routeLocatorBuilder.routes().route(r -> r
                // configure circuit breaker
                .path("/category/**")

                .filters(
                        f-> f
//                                .rewritePath("/category/?(?<segment>.*)","/${segment}")
                                .rewritePath("/category/(?<segment>.*)", "/category/${segment}")
                                .addResponseHeader("-X-CUSTOM-HEADER", "ADDED BY DVD")
                                .circuitBreaker(breaker -> breaker
                                        .setName("category-breaker")

                                        .setFallbackUri("forward:/categoryFallBackUri")
                                )
                )
                .uri("lb://category-service"))
                // configure retry
                .route(r -> r
                        .path("/video/**")
                        .filters(f -> f
                                .rewritePath("/video/?(?<segment>.*)", "/${segment}")
                                        .retry(retryConfig -> retryConfig.setRetries(3)
                                                .setMethods(HttpMethod.GET)
                                                .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                                .addResponseHeader("-X-CUSTOM-HEADER", "ADDED BY DVD")
//                                .circuitBreaker(breaker -> breaker
//                                        .setName("video-breaker")
//                                        .setFallbackUri("forward:/videoFallBackUri")
//                                )
                         //        we have to turn off the video service before checking the retry pattern.
                        )
                        .uri("lb://video-service")
                )


               // rate Limiter
                .route(r -> r
                        .path("/course/**")
                        .filters(f -> f.rewritePath("/course/?(?<segment>.*)", "/${segment}")
                               // .circuitBreaker(breaker -> breaker.setName("course-breaker")
                                        .requestRateLimiter(rateConfig -> rateConfig.setKeyResolver(keyResolver()).setRateLimiter(redisRateLimiter()))
                                      //  .setFallbackUri("forward:/courseFallBack")

                                )
                        .uri("lb://course-service")
                ) 
                .build();
    }
}