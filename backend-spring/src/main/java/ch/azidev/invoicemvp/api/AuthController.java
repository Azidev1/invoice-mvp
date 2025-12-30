package ch.azidev.invoicemvp.api;

import com.example.invoicemvp.domain.*;
import com.example.invoicemvp.repo.*;
import com.example.invoicemvp.security.JwtService;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final TenantRepository tenants;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService apiJwt;

    public AuthController(TenantRepository tenants, UserRepository users, PasswordEncoder encoder, JwtService apiJwt) {
        this.tenants = tenants; this.users = users; this.encoder = encoder; this.apiJwt = apiJwt;
    }

    public record RegisterRequest(String tenantName, String email, String password) {}
    public record LoginRequest(UUID tenantId, String email, String password) {}
    public record AuthResponse(String token, UUID tenantId, UUID userId, String role) {}

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest req) {
        var t = new Tenant();
        t.setId(UUID.randomUUID());
        t.setName(req.tenantName());
        tenants.save(t);

        var u = new AppUser();
        u.setId(UUID.randomUUID());
        u.setTenant(t);
        u.setEmail(req.email().toLowerCase());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setRole("ADMIN");
        users.save(u);

        String token = apiJwt.createToken(u.getId().toString(),
                Map.of("tenantId", t.getId().toString(), "role", u.getRole()),
                60 * 60 * 8);

        return new AuthResponse(token, t.getId(), u.getId(), u.getRole());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {
        var u = users.findByTenant_IdAndEmail(req.tenantId(), req.email().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials"));

        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }

        String token = apiJwt.createToken(u.getId().toString(),
                Map.of("tenantId", u.getTenant().getId().toString(), "role", u.getRole()),
                60 * 60 * 8);

        return new AuthResponse(token, u.getTenant().getId(), u.getId(), u.getRole());
    }
}
