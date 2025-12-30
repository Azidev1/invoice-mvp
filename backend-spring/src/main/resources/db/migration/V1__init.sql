CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE tenants (
                         id UUID PRIMARY KEY,
                         name TEXT NOT NULL,
                         created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                       email TEXT NOT NULL,
                       password_hash TEXT NOT NULL,
                       role TEXT NOT NULL,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                       CONSTRAINT uk_users_tenant_email UNIQUE (tenant_id, email)
);

CREATE INDEX idx_users_tenant ON users(tenant_id);

CREATE TABLE documents (
                           id UUID PRIMARY KEY,
                           tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,

                           status TEXT NOT NULL,
                           source TEXT NOT NULL,

                           original_filename TEXT NOT NULL,
                           content_type TEXT NOT NULL,
                           size_bytes BIGINT NOT NULL,
                           sha256 TEXT NULL,

                           storage_bucket TEXT NOT NULL,
                           storage_key TEXT NOT NULL,

                           page_count INT NULL,
                           error_message TEXT NULL,

                           created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                           CONSTRAINT uk_documents_tenant_storage UNIQUE (tenant_id, storage_key)
);

CREATE INDEX idx_documents_tenant_status ON documents(tenant_id, status);
CREATE INDEX idx_documents_tenant_created ON documents(tenant_id, created_at DESC);

CREATE TABLE document_extractions (
                                      document_id UUID PRIMARY KEY REFERENCES documents(id) ON DELETE CASCADE,
                                      tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,

                                      ocr_text TEXT NOT NULL,
                                      fields_json JSONB NOT NULL,
                                      confidence_json JSONB NOT NULL,
                                      engine TEXT NOT NULL,
                                      extracted_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_extractions_tenant ON document_extractions(tenant_id);
