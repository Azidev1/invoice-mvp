package ch.azidev.invoicemvp.api;

import com.example.invoicemvp.domain.*;
import com.example.invoicemvp.repo.DocumentRepository;
import com.example.invoicemvp.security.AuthContext;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentRepository docs;
    private final RabbitTemplate rabbit;

    public DocumentController(DocumentRepository docs, RabbitTemplate rabbit) {
        this.docs = docs; this.rabbit = rabbit;
    }

    public record CreateDocRequest(UUID documentId, String objectKey, String filename, String contentType, long sizeBytes, String sha256) {}
    public record CreateDocResponse(UUID id, String status) {}

    public record DocumentDto(UUID id, String status, String filename, String contentType, long sizeBytes, String storageKey) {
        static DocumentDto from(Document d) {
            return new DocumentDto(d.getId(), d.getStatus().name(), d.getOriginalFilename(), d.getContentType(), d.getSizeBytes(), d.getStorageKey());
        }
    }

    @PostMapping
    public CreateDocResponse create(@RequestBody CreateDocRequest req) {
        var auth = AuthContext.principal();

        Document d = docs.findByIdAndTenant_Id(req.documentId(), auth.tenantId()).orElseThrow();
        d.setStatus(DocumentStatus.UPLOADED);
        d.setSha256(req.sha256());
        docs.save(d);

        Map<String,Object> msg = new HashMap<>();
        msg.put("eventId", UUID.randomUUID().toString());
        msg.put("eventType", "DOCUMENT_UPLOADED");
        msg.put("documentId", d.getId().toString());
        msg.put("tenantId", auth.tenantId().toString());
        msg.put("bucket", d.getStorageBucket());
        msg.put("objectKey", d.getStorageKey());
        msg.put("contentType", d.getContentType());
        msg.put("sizeBytes", d.getSizeBytes());
        msg.put("createdAt", Instant.now().toString());

        rabbit.convertAndSend("documents.exchange", "documents.uploaded", msg);

        return new CreateDocResponse(d.getId(), d.getStatus().name());
    }

    @GetMapping("/{id}")
    public DocumentDto get(@PathVariable UUID id) {
        var auth = AuthContext.principal();
        Document d = docs.findByIdAndTenant_Id(id, auth.tenantId()).orElseThrow();
        return DocumentDto.from(d);
    }
}
