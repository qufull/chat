package com.example.apigateway.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TokenRelayFilter implements GlobalFilter {

    private final ReactiveOAuth2AuthorizedClientManager authorizedClientManager;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    Authentication auth = ctx.getAuthentication();
                    if (auth == null) {
                        return Mono.empty();
                    }

                    return authorizedClientManager
                            .authorize(OAuth2AuthorizeRequest.withClientRegistrationId("keycloak")
                                    .principal(auth)
                                    .build());
                })
                .map(client -> {
                    String token = client.getAccessToken().getTokenValue();
                    ServerHttpRequest mutated = exchange.getRequest().mutate()
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .build();
                    return exchange.mutate().request(mutated).build();
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }
}
