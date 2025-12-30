package ch.azidev.invoicemvp.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="document_extractions")
public class DocumentExtraction {
    @Id @Column(name="document_id") private UUID documentId;

    @ManyToOne(optional=false) @JoinColumn(name="tenant_id")
    private Tenant tenant;

    @Lob @Column(name="ocr_text", nullable=false) private String ocrText;

    @Column(name="fields_json", columnDefinition="jsonb", nullable=false)
    private String fieldsJson;

    @Column(name="confidence_json", columnDefinition="jsonb", nullable=false)
    private String confidenceJson;

    @Column(nullable=false) private String engine;
    @Column(name="extracted_at", nullable=false) private Instant extractedAt;

    @PrePersist void onCreate(){ extractedAt = Instant.now(); }

    public UUID getDocumentId(){ return documentId; }
    public void setDocumentId(UUID documentId){ this.documentId=documentId; }
    public Tenant getTenant(){ return tenant; }
    public void setTenant(Tenant tenant){ this.tenant=tenant; }
    public String getOcrText(){ return ocrText; }
    public void setOcrText(String ocrText){ this.ocrText=ocrText; }
    public String getFieldsJson(){ return fieldsJson; }
    public void setFieldsJson(String fieldsJson){ this.fieldsJson=fieldsJson; }
    public String getConfidenceJson(){ return confidenceJson; }
    public void setConfidenceJson(String confidenceJson){ this.confidenceJson=confidenceJson; }
    public String getEngine(){ return engine; }
    public void setEngine(String engine){ this.engine=engine; }
}
