package ch.azidev.invoicemvp.repo;

import com.example.invoicemvp.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Optional<Document> findByIdAndTenant_Id(UUID id, UUID tenantId);
}
