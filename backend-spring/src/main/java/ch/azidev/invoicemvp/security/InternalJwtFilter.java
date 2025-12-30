package ch.azidev.invoicemvp.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

public class InternalJwtFilter extends OncePerRequestFilter {
    private final JwtService internalJwt;

    public InternalJwtFilter(JwtService internalJwt) { this.internalJwt = internalJwt; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) { res.setStatus(401); return; }

        try {
            var claims = internalJwt.parse(auth.substring(7)).getBody();
            var authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(req, res);
        } catch (Exception e) {
            res.setStatus(401);
        }
    }
}
