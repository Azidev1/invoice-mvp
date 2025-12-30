package ch.azidev.invoicemvp.repo;

import com.example.invoicemvp.domain.DocumentExtraction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DocumentExtractionRepository extends JpaRepository<DocumentExtraction, UUID> {}
