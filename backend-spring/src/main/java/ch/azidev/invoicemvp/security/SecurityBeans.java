package ch.azidev.invoicemvp.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityBeans {

    @Bean
    JwtService apiJwt(@Value("${jwt.secret}") String secret, @Value("${jwt.issuer}") String issuer) {
        return new JwtService(secret, issuer);
    }

    @Bean
    JwtService internalJwt(@Value("${internalJwt.secret}") String secret, @Value("${internalJwt.issuer}") String issuer) {
        return new JwtService(secret, issuer);
    }

    @Bean ApiJwtFilter apiJwtFilter(JwtService apiJwt) { return new ApiJwtFilter(apiJwt); }
    @Bean InternalJwtFilter internalJwtFilter(JwtService internalJwt) { return new InternalJwtFilter(internalJwt); }

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
