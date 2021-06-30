package com.shortner.appgateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;


import java.time.Duration;

@Configuration
public class AppGatewayConfig {

    /*
      Route locator that maps different routes based on the incoming request
     */
    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder routeLocatorBuilder)
    {
        return routeLocatorBuilder.routes()
                .route(p ->p
                        .path("/url/generate")
                        .filters(f->f.requestRateLimiter().configure(c->c.setRateLimiter(redisRateLimiter())))
                        .uri("http://localhost:8100"))
                .route(p ->p
                        .path("/welcome")
                        .filters(f -> f.circuitBreaker(c->c.setName("codedTribeCB").setFallbackUri("/defaultFallback")))
                        .uri("http://localhost:8082"))
                .build();
    }


    /* Fallback Url if the response takes more than 2 seconds

     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer()
    {
        return factory->factory.configureDefault(id ->new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(2)).build()).build());
    }

    /*
     Redis rate limiter
     default replenish rate:number request allowed in 1 second without drop request
     default burst capacity:maximum number of requests a user can send in 1 second
     */
    @Bean
    public RedisRateLimiter redisRateLimiter()
    {
        return new RedisRateLimiter(10,10);
    }

    /*
    Key for redis always returns 1 as the key
     */
    @Bean
    KeyResolver userKeyResolver()
    {
        return exchange -> Mono.just("1");
    }
}
