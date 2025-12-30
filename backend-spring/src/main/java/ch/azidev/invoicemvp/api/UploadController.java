package ch.azidev.invoicemvp.api;

import com.example.invoicemvp.config.AppProps;
import com.example.invoicemvp.domain.*;
import com.example.invoicemvp.repo.*;
import com.example.invoicemvp.security.AuthContext;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final S3Presigner presigner;
    private final AppProps props;
    private final DocumentRepository docs;
    private final TenantRepository tenants;

    public UploadController(S3Presigner presigner, AppProps props, DocumentRepository docs, TenantRepository tenants) {
        this.presigner = presigner; this.props = props; this.docs = docs; this.tenants = tenants;
    }

    public record PresignRequest(String filename, String contentType, long size) {}
    public record PresignResponse(UUID documentId, String bucket, String objectKey, String uploadUrl) {}

    @PostMapping("/presign")
    public PresignResponse presign(@RequestBody PresignRequest req) {
        var auth = AuthContext.principal();
        UUID tenantId = auth.tenantId();
        UUID docId = UUID.randomUUID();

        String safeName = req.filename().replaceAll("[^a-zA-Z0-9._-]", "_");
        String key = tenantId + "/" + docId + "/" + safeName;

        var d = new Document();
        d.setId(docId);
        d.setTenant(tenants.getReferenceById(tenantId));
        d.setStatus(DocumentStatus.PRESIGNED);
        d.setSource("WEB_UPLOAD");
        d.setOriginalFilename(req.filename());
        d.setContentType(req.contentType());
        d.setSizeBytes(req.size());
        d.setStorageBucket(props.storage().s3Bucket());
        d.setStorageKey(key);
        docs.save(d);

        var putReq = PutObjectRequest.builder()
                .bucket(props.storage().s3Bucket())
                .key(key)
                .contentType(req.contentType())
                .build();

        var preReq = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(putReq)
                .build();

        String url = presigner.presignPutObject(preReq).url().toString();
        return new PresignResponse(docId, props.storage().s3Bucket(), key, url);
    }
}
