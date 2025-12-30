package ch.azidev.invoicemvp.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class AuthContext {
    public static AuthPrincipal principal() {
        return (AuthPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
