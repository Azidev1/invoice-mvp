package ch.azidev.invoicemvp.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="tenants")
public class Tenant {
    @Id private UUID id;
    @Column(nullable=false) private String name;
    @Column(name="created_at", nullable=false) private Instant createdAt;

    @PrePersist void onCreate(){ createdAt = Instant.now(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Instant getCreatedAt() { return createdAt; }
}
