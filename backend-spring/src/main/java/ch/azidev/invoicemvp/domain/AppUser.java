package ch.azidev.invoicemvp.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="users",
        uniqueConstraints=@UniqueConstraint(name="uk_users_tenant_email", columnNames={"tenant_id","email"}))
public class AppUser {
    @Id private UUID id;

    @ManyToOne(optional=false) @JoinColumn(name="tenant_id")
    private Tenant tenant;

    @Column(nullable=false) private String email;
    @Column(name="password_hash", nullable=false) private String passwordHash;
    @Column(nullable=false) private String role;

    @Column(name="created_at", nullable=false) private Instant createdAt;

    @PrePersist void onCreate(){ createdAt = Instant.now(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
