package ch.azidev.invoicemvp.internal;

import com.example.invoicemvp.domain.*;
import com.example.invoicemvp.repo.*;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/documents")
public class InternalDocumentController {

    private final DocumentRepository docs;
    private final DocumentExtractionRepository extractions;
    private final TenantRepository tenants;

    public InternalDocumentController(DocumentRepository docs, DocumentExtractionRepository extractions, TenantRepository tenants) {
        this.docs = docs; this.extractions = extractions; this.tenants = tenants;
    }

    public record StatusRequest(String status, String errorMessage) {}
    public record ResultsRequest(String ocrText, String fields, String confidence, String engine, Integer pages, String tenantId) {}

    @PostMapping("/{id}/status")
    public void setStatus(@PathVariable UUID id, @RequestBody StatusRequest req) {
        Document d = docs.findById(id).orElseThrow();
        d.setStatus(DocumentStatus.valueOf(req.status()));
        d.setErrorMessage(req.errorMessage());
        docs.save(d);
    }

    @PostMapping("/{id}/results")
    public void saveResults(@PathVariable UUID id, @RequestBody ResultsRequest req) {
        Document d = docs.findById(id).orElseThrow();
        d.setStatus(DocumentStatus.REVIEW);
        d.setPageCount(req.pages());
        docs.save(d);

        var e = new DocumentExtraction();
        e.setDocumentId(id);
        e.setTenant(tenants.getReferenceById(UUID.fromString(req.tenantId())));
        e.setOcrText(req.ocrText());
        e.setFieldsJson(req.fields());
        e.setConfidenceJson(req.confidence());
        e.setEngine(req.engine());
        extractions.save(e);
    }
}
