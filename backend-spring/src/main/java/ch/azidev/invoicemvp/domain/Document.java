package ch.azidev.invoicemvp.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="documents")
public class Document {
    @Id private UUID id;

    @ManyToOne(optional=false) @JoinColumn(name="tenant_id")
    private Tenant tenant;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private DocumentStatus status;

    @Column(nullable=false) private String source;

    @Column(name="original_filename", nullable=false) private String originalFilename;
    @Column(name="content_type", nullable=false) private String contentType;
    @Column(name="size_bytes", nullable=false) private Long sizeBytes;
    private String sha256;

    @Column(name="storage_bucket", nullable=false) private String storageBucket;
    @Column(name="storage_key", nullable=false) private String storageKey;

    @Column(name="page_count") private Integer pageCount;
    @Column(name="error_message") private String errorMessage;

    @Column(name="created_at", nullable=false) private Instant createdAt;
    @Column(name="updated_at", nullable=false) private Instant updatedAt;

    @PrePersist void onCreate(){ createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate(){ updatedAt = Instant.now(); }

    // getters/setters
    public UUID getId(){ return id; }
    public void setId(UUID id){ this.id=id; }
    public Tenant getTenant(){ return tenant; }
    public void setTenant(Tenant tenant){ this.tenant=tenant; }
    public DocumentStatus getStatus(){ return status; }
    public void setStatus(DocumentStatus status){ this.status=status; }
    public String getSource(){ return source; }
    public void setSource(String source){ this.source=source; }
    public String getOriginalFilename(){ return originalFilename; }
    public void setOriginalFilename(String originalFilename){ this.originalFilename=originalFilename; }
    public String getContentType(){ return contentType; }
    public void setContentType(String contentType){ this.contentType=contentType; }
    public Long getSizeBytes(){ return sizeBytes; }
    public void setSizeBytes(Long sizeBytes){ this.sizeBytes=sizeBytes; }
    public String getSha256(){ return sha256; }
    public void setSha256(String sha256){ this.sha256=sha256; }
    public String getStorageBucket(){ return storageBucket; }
    public void setStorageBucket(String storageBucket){ this.storageBucket=storageBucket; }
    public String getStorageKey(){ return storageKey; }
    public void setStorageKey(String storageKey){ this.storageKey=storageKey; }
    public Integer getPageCount(){ return pageCount; }
    public void setPageCount(Integer pageCount){ this.pageCount=pageCount; }
    public String getErrorMessage(){ return errorMessage; }
    public void setErrorMessage(String errorMessage){ this.errorMessage=errorMessage; }
}
