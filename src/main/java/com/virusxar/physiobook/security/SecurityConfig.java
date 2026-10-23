package com.virusxar.physiobook.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class SecurityConfig {

    private static final String PHYSIO = "PHYSIOTHERAPIST";
    private static final String ADMIN = "ADMIN";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // public
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/physiotherapists",
                                "/api/physiotherapists/*",
                                "/api/physiotherapists/*/availability",
                                "/api/physiotherapists/*/slots").permitAll()

                        // admin
                        .requestMatchers(HttpMethod.POST, "/api/physiotherapists", "/api/physiotherapists/*/account").hasRole(ADMIN)

                        // physio
                        .requestMatchers(HttpMethod.PUT, "/api/physiotherapists/**").hasAnyRole(PHYSIO, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/physiotherapists/*/appointments").hasAnyRole(PHYSIO, ADMIN)

                        // patient
                        .requestMatchers(HttpMethod.GET, "/api/patients").hasAnyRole(PHYSIO, ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/patients").hasAnyRole(PHYSIO, ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/patients/*").hasAnyRole(PHYSIO, ADMIN)

                        // episodes and notes
                        .requestMatchers("/api/patients/*/episodes", "/api/episodes/**").hasRole(PHYSIO)
                        .requestMatchers("/api/patients/*/notes", "/api/notes/**").hasRole(PHYSIO)

                        // appointment
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/appointments/*/confirm",
                                "/api/appointments/*/complete",
                                "/api/appointments/*/no-show").hasAnyRole(PHYSIO, ADMIN)

                        // other
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecretKey jwtSecretKey(@Value("${app.jwt.secret}") String secret) {
        byte[] bytes = Base64.getDecoder().decode(secret);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret musi mieć co najmniej 32 bajty (256 bitów)");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(MacAlgorithm.HS256).build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("role");
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    // 401: missing, invalid or expired token. Response uses the same format as the rest of the API errors.
    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, ex) -> {
            response.setStatus(401);
            response.setContentType("application/problem+json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("""
                    {"title":"Unauthorized","status":401,"detail":"Brak tokena albo token jest nieważny"}""");
        };
    }

    // 403: valid token, but the role has no access to this URL
    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> {
            response.setStatus(403);
            response.setContentType("application/problem+json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("""
                    {"title":"Forbidden","status":403,"detail":"Brak uprawnień"}""");
        };
    }
}
