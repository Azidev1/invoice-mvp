package ch.azidev.invoicemvp.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class ApiJwtFilter extends OncePerRequestFilter {
    private final JwtService apiJwt;

    public ApiJwtFilter(JwtService apiJwt) { this.apiJwt = apiJwt; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) { res.setStatus(401); return; }

        try {
            var claims = apiJwt.parse(auth.substring(7)).getBody();
            UUID userId = UUID.fromString(claims.getSubject());
            UUID tenantId = UUID.fromString((String) claims.get("tenantId"));
            String role = (String) claims.get("role");

            var principal = new AuthPrincipal(userId, tenantId, role);
            var authentication = new UsernamePasswordAuthenticationToken(principal, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(req, res);
        } catch (Exception e) {
            res.setStatus(401);
        }
    }
}
