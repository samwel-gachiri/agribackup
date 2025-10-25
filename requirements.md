# EUDR Compliance Requirements (complete specification)

Last updated: 2025-10-20

Purpose
-------
This document translates the EU Deforestation Regulation (EUDR) obligations into a complete, implementer-focused set of functional, data, security, operational and testing requirements for AgriBackup. The goal is to ensure the platform can be used by exporters/operators to meet EUDR due diligence and traceability obligations.

Scope
-----
- System components: backend `farmers-portal-apis`, frontend `farmer-portal-frontend`, S3 storage, and operational processes.
- Audience: product owners, architects, backend/frontend engineers, DevOps, compliance officers.
- This file focuses on requirements only — design and tasks will follow in `design.md` and `tasks.md`.

How to use this file
--------------------
- Each requirement is precise and has acceptance criteria.
- Requirements are grouped into categories (Data, APIs, Security, Operations, Testing).
- Later documents will map these to implementation tasks and designs.

LEGAL BACKGROUND (brief)
------------------------
- EUDR requires operators placing covered commodities on the EU market (or exporting from the EU) to perform due diligence and ensure commodities are "deforestation-free" and produced in compliance with applicable legislation of the country of production. It requires documentation, traceability to the production unit, risk assessment, mitigation, record keeping and cooperation with competent authorities. This specification operationalizes those obligations.

GLOSSARY (terms used in requirements)
- Operator: entity placing commodities on the EU market (or exporting from EU).
- Trader: entity buying/selling within the EU not placing on the market.
- Batch / Consignment: the physical set of commodity items moved together and subject to due diligence.
- Production unit / Parcel: the plot, farm or area where the commodity was produced (polygon GeoJSON preferred).
- Dossier: packaged, machine- and human-readable record for a batch including metadata, docs and audit trail.

PRINCIPLES
----------
All implemented features must follow these principles:
- Verifiable: every claim/data point must have source metadata and evidence (document ids, checksums, timestamps).
- Immutable audit: critical events are audit-logged append-only with actor/timestamp and integrity protection.
- Least-privilege access: private storage and presigned short-lived access; role-based authorization.
- Machine-readable: use ISO dates, HS codes, GeoJSON, JSON payloads for automated processing.
- GDPR-aware: capture only required personal data, store consent and provide deletion/retention workflows.

REQUIREMENTS
============

1. Identity, Roles & Authorization
----------------------------------
1.1 Role model
- Requirement: Support role-based accounts for at least: OPERATOR, TRADER, EXPORTER, FARMER, ZONE_SUPERVISOR, VERIFIER, SYSTEM_ADMIN, AUDITOR.
- Acceptance: Roles exist in DB and are enforced by backend; protected endpoints return 403 for unauthorized access.

1.2 Delegation & Agent flows
- Requirement: Support delegation (operator may delegate due diligence actions to an agent/service provider) and track who performed the DD.
- Acceptance: Every DD action stores actor_id, actor_role and delegation metadata.

2. Data & Provenance (core)
---------------------------
2.1 Batch/Consignment model
- Requirement: Implement `batches` table with fields: id (UUID), batch_code, commodity, hs_code, quantity, unit, country_of_production, harvest_date (or period start/end), created_by, created_at, status, producer_ids[], parcel_ids[].
- Acceptance: API `GET /api/produce-service/batches/{id}` returns the above and is linked to documents and audit logs.

2.2 Production unit & parcel geometry
- Requirement: Store production units as polygons (GeoJSON) in a spatial DB (PostGIS preferred). Each parcel has id, owner_id, geojson, area_m2, administrative_region, last_verified_at.
- Acceptance: Parcels can be queried for intersection with GeoJSON geometries; frontend can display and edit polygons via map UI.

2.3 Supplier & chain-of-custody metadata
- Requirement: Maintain supplier entities (id, legal_name, contact, registration_number) and record chain-of-custody events: event_id, batch_id, from_entity, to_entity, action_type, timestamp, document_refs.
- Acceptance: For any batch, the full chain-of-custody timeline is retrievable.

2.4 Documents metadata
- Requirement: Implement `documents` table: id (uuid), owner_entity_id, batch_id, doc_type (enum: LICENSE, PERMIT, INVOICE, BOL, SUPPLIER_DECLARATION, VERIFIER_REPORT, CERTIFICATE, OTHER), issuer, issue_date, expiry_date, s3_key, file_name, mime_type, checksum_sha256, file_size, uploader_id, uploaded_at, exif_lat, exif_lng, exif_timestamp, tags (json), retention_until, visibility.
- Acceptance: Document upload returns a document id; document metadata stored and checksum validated.

3. Document Storage, Access & Integrity
--------------------------------------
3.1 Private object storage & SSE
- Requirement: All documents must be stored in a private S3 bucket (or equivalent), with server-side encryption (SSE-S3 or SSE-KMS).
- Acceptance: No publicly accessible persistent URLs; S3 bucket policy denies public GetObject.

3.2 Presigned URL generation
- Requirement: Implement presigned URL generator `generatePresignedUrl(s3Key, method, expirySeconds)` using AWS S3 Presigner. Expose endpoint: `GET /api/documents/{id}/presigned?expirySeconds=300&method=GET`.
- Constraints: Default expiry = 300s; maximum expiry configurable but capped (e.g., 3600s).
- Acceptance: Presigned link grants access only within expiry and logs the generation event.

3.3 Checksum and EXIF extraction
- Requirement: On upload compute SHA‑256 checksum, store it in `documents.checksum_sha256`. If image, extract EXIF geotag/time and store exif fields.
- Acceptance: Checksum stored and testable via re-download; EXIF stored if present.

3.4 Immutable evidence & copies
- Requirement: Once a document is uploaded and associated to batch, record an immutable audit entry and prevent silent overwrite (new upload should create new document id and keep prior copy unless a certified delete is performed with audit trace).
- Acceptance: Old document records remain accessible and have immutable history entries.

4. Audit Logging & Tamper Evidence
---------------------------------
4.1 Append-only audit log
- Requirement: Implement `audit_logs` append-only table: id, entity_type, entity_id, action, actor_id, actor_role, timestamp, details_json, record_hash.
- Acceptance: All critical events (document upload, presigned link generation, licence review, risk assessment, dossier generation, data edits) are logged.

4.2 Record hashing & integrity
- Requirement: Compute record_hash = SHA256(entity_type + entity_id + action + timestamp + details_json) and optionally maintain periodic Merkle-root or signed root for tamper evidence.
- Acceptance: A simple verification script can check that stored records match recomputed hashes.

5. Risk Assessment & Mitigation
--------------------------------
5.1 Risk assessment service
- Requirement: Implement `RiskAssessmentService` that produces a risk object per batch: { risk_level: NONE|LOW|MEDIUM|HIGH, score: numeric, reasons: [strings], computed_at }.
- Inputs: country risk (configurable table), commodity risk, parcel alerts (GFW/GLAD), supplier history (prior findings), certification status.
- Acceptance: Calling `POST /api/eudr/assess?batchId={}` returns risk object and writes audit log.

5.2 Mitigation workflows
- Requirement: For MEDIUM/HIGH risk, create a mitigation ticket with actions: request additional documents, require verifier visit, hold batch. Provide statuses and comment thread.
- Acceptance: The mitigation ticket appears in admin queue and actions are auditable; batch cannot be exported while on critical hold.

6. Dossier / Regulator Reporting
--------------------------------
6.1 Dossier generation endpoint
- Requirement: `GET /api/eudr/report?batchId={id}&format={json|zip|pdf}` returns a machine-readable JSON and/or ZIP (JSON metadata + presigned links to documents) and a human-readable PDF summary. Include: batch metadata, parcel geojson, harvest_date, supplier chain, documents metadata with presigned links, audit trail entries, risk assessment and mitigation actions.
- AuthZ: Accessible to operator owning the batch, authorized auditors, and system admins; every access generates audit log.
- Acceptance: Dossier generation is tested and dossier contains presigned links that are valid for the required short period and audit entries of access are created.

6.2 Competent authority access
- Requirement: Provide an API for competent authority requests (secured) and a manual process (admin UI) to share dossiers; support callback URLs.
- Acceptance: Authorities receive documents and metadata; all access is logged.

7. Spatial monitoring & external data
------------------------------------
7.1 External alert integration
- Requirement: Integrate at least one external deforestation/forest-loss data source (Global Forest Watch, Hansen/GLAD/Sentinel). Implement a scheduled job to poll and reconcile alerts against parcel polygons.
- Acceptance: Parcels with alerts have `parcel.alerts` entries and feeds into risk assessment.

7.2 Spatial queries & PostGIS
- Requirement: Migrate DB to Postgres+PostGIS or enable equivalent spatial queries. Support spatial indexes and efficient intersection queries.
- Acceptance: Spatial query performance acceptable for dashboards; unit tests validate intersection with alerts.

8. UI & UX requirements
-----------------------
8.1 Parcel capture UI
- Requirement: In frontend: map-based polygon drawing/import (GeoJSON), validation (minimum vertices, valid geometry), confirm producer ownership, and ability to upload parcel docs.
- Acceptance: Users can draw a polygon and save; backend stores polygon and returns parcel id.

8.2 Document upload flows
- Requirement: Upload flows (exporter, verifier, admin) must capture required metadata fields and validate file type and size (<=10MB by default). Show progress, error handling, and return doc id.
- Acceptance: Upload returns 201 with document metadata.

8.3 Dossier & risk UI
- Requirement: Admin and exporter dashboards show batch risk, mitigation tickets, and reseller/exporter-specific dossiers with action buttons.
- Acceptance: MEDIUM/HIGH flagged batches visible in admin queue.

9. Retention & Archival
-----------------------
9.1 Retention policy
- Requirement: Default retention = 5 years from relevant event (e.g., upload or batch close). Support legal holds to prevent deletion. Implement archival to cold storage and searchable indexes for archived data.
- Acceptance: Data older than retention moves to archived state; legal hold prevents archival/deletion.

9.2 Deletion & redaction
- Requirement: Implement GDPR-aware deletion with redaction: personal data removal where required but keeping compliance evidence where legally necessary (log the reason and apply legal hold exceptions).
- Acceptance: Deletion procedures produce audit log and respect legal hold.

10. Security
-----------
10.1 Transport & authn
- Requirement: HTTPS only; JWT tokens with short expiries + refresh flow; admin MFA enforced.
- Acceptance: All endpoints rejected over HTTP.

10.2 IAM & S3 policies
- Requirement: Least privilege IAM role for the app; S3 prefixes per environment/tenant; KMS keys managed and rotated.
- Acceptance: IAM and bucket policies reviewed and documented.

10.3 Secrets & keys
- Requirement: Store AWS creds, KMS, external API keys in secure secret manager; do not hardcode.
- Acceptance: No credentials in repo; CI/CD reads secrets from environment/secret store.

11. Testing & QA
----------------
11.1 Automated tests
- Requirement: Unit tests for services; integration tests for upload→checksum→presigned flow; end-to-end tests for batch→dossier flows.
- Acceptance: CI runs tests and coverage for critical modules.

11.2 Test data & fixtures
- Requirement: Provide test fixtures for parcels (GeoJSON), simulated GFW alerts, sample documents and sample batches.
- Acceptance: Tests pass with fixtures.

12. Operational & SOP requirements
---------------------------------
12.1 SOPs
- Requirement: Define SOPs for: handling MEDIUM/HIGH risks, responding to competent authority requests, evidence retention, incident response, verifier accreditation process.
- Acceptance: SOP documents stored in repo and accessible to admins.

12.2 Monitoring & alerts
- Requirement: Monitor batch volumes, risk spikes, failed uploads, and anomalous access patterns; integrate alerting (PagerDuty/Slack/email).
- Acceptance: Alerts configured and tested.

13. Interoperability & data formats
----------------------------------
- Requirement: Use GeoJSON for geometries, ISO 8601 dates, HS codes where possible, JSON APIs, and CSV export for bulk reporting.
- Acceptance: All APIs documented in OpenAPI with examples.

14. Audit & Compliance Reports
-----------------------------
- Requirement: Provide periodic compliance reports (summary of batches, risks, mitigation actions, dossiers generated) and an audit export function.
- Acceptance: Admin can generate a quarterly compliance report.

15. Verifier/Third-party Integrations
------------------------------------
- Requirement: Support verifier accounts, verifier-uploaded reports, and linking of accreditation metadata (accreditation body, certificate id, expiry).
- Acceptance: Verifier reports attached to batches and visible in dossiers.

16. Performance & Scalability
----------------------------
- Requirement: System able to handle bulk uploads and large exporters (thousands of batches per day); S3 and DB pagination, async processing for heavy tasks.
- Acceptance: Load tests indicate acceptable performance for expected scale; background jobs process risk/alert reconciliation asynchronously.

17. Backups & DR
----------------
- Requirement: DB backups, S3 replication/lifecycle rules, disaster recovery runbooks; periodic restored-testing.
- Acceptance: Successful restore test every quarter.

18. Legal & Documentation
-------------------------
- Requirement: Provide legal guidance pages, supplier questionnaires, templates (supplier declaration, mitigation plan) and update privacy policy.
- Acceptance: Documents reviewed by legal and stored in repo.

IMPLEMENTATION ROADMAP (high level)
===================================
Phase 0 — Preparation (1 week)
- Review legal requirements with counsel. Finalize data model and retention policy.

Phase 1 — Foundation (2-4 weeks)
- Documents table + DocumentService; S3 private uploads; presigned URL endpoint; checksum and EXIF extraction; audit logs for uploads.
- Batch model and API; link batches → documents → parcels.

Phase 2 — Risk & Dossier (3-6 weeks)
- RiskAssessmentService (basic heuristics), mitigation ticketing, dossier generation endpoint (JSON/ZIP/PDF), admin UI for review.

Phase 3 — Spatial & External Data (3-8 weeks)
- PostGIS migration (if required), parcel polygon UI, integrate GFW/alert feeds and reconcile with parcels.

Phase 4 — Harden & Scale (ongoing)
- Retention/archival jobs, legal hold features, tamper-evidence hashing, verifier workflows, audits and compliance reporting.

ACCEPTANCE CRITERIA (summary)
=============================
- Documents: uploaded to private S3, metadata stored, checksum validated, presigned links generated and logged.
- Batches: chain-of-custody and parcel geometry present; dossier contains all docs + audit trail; dossier download logged.
- Risk: risk assessment available for batches; mitigation triggered for MEDIUM/HIGH.
- Audit: audit logs are append-only and hash-verified.
- Retention: data older than retention archived and retrievable; legal hold respected.

APPENDICES
==========
A. Suggested DB migration names (Liquibase)
- `20251020-add-documents-table.xml`
- `20251020-add-batches-table.xml`
- `20251020-add-audit-logs-table.xml`

B. Example API endpoints (summary)
- POST /api/documents (multipart + metadata) -> create document
- GET  /api/documents/{id} -> metadata
- GET  /api/documents/{id}/presigned?expirySeconds=300
- POST /api/produce-service/batches -> create batch
- GET  /api/produce-service/batches/{id}
- POST /api/eudr/assess?batchId={id}
- GET  /api/eudr/report?batchId={id}&format=zip

C. Tests to create (minimal)
- Upload document → assert doc record, checksum, and private S3 key.
- Generate presigned URL → assert access within expiry and audit log recorded.
- Create batch with parcel polygon → assert spatial store and parcel intersection with test alert.
- Run risk assessment on known test parcels → assert expected risk levels.

If you want I will now:
1) Generate Liquibase changeSets for `documents`, `batches`, and `audit_logs` (safe, low-risk first step).
2) Implement `S3Service.generatePresignedUrl(...)` and a small `DocumentsController` with `GET /api/documents/{id}/presigned`.
3) Scaffold `DocumentService` to compute checksum and extract EXIF metadata on upload.

Please confirm which immediate task (1, 2, or 3) I should start with and I will update the todo list and begin implementing.
# EUDR (EU Deforestation Regulation) Requirements for AgriBackup

Last updated: 2025-10-19

Purpose
-------
This document inventories the project's current APIs and dependencies (backend and frontend), summarizes gaps against EUDR expectations, and provides a prioritized, concrete requirements list (data model, endpoints, security, operational) needed so exporters using AgriBackup can satisfy EUDR due diligence and traceability requirements.

Scope
-----
- Applies to exporter-facing functionality and backend services in `farmers-portal-apis` and the supporting frontend flows in `farmer-portal-frontend`.
- Focused on technical and process requirements necessary to: capture provenance, store evidentiary documents, provide auditable records, enable regulator access/dossiers, and compute basic risk/mitigation.

Summary of current capabilities (what exists today)
-----------------------------------------------
- Authentication & RBAC: JWT-based auth and role concepts (FARMER, EXPORTER, SYSTEM_ADMIN, ZONE_SUPERVISOR).
- Exporter onboarding and verification flow: exporters can register without licenseId; can submit license ID and upload a license document via `POST /api/exporters-service/exporter/submit-license-document` and `POST /api/exporters-service/exporter/submit-license`.
- S3 integration: `S3Service` uploads files (produce images and license-documents) and returns public HTTPS URLs. `S3Service` currently uses AWS SDK S3Client and PutObject/ DeleteObject.
- Admin license review: `AdminLicenseService` retrieves exporters with verification status UNDER_REVIEW and exposes review endpoints that change exporter verification status and send templated email notifications.
- Zones: `Zone` entity stores center latitude/longitude and radius (circle-based zone model), with exporter -> zones relationship.
- Some frontend UI elements reference EUDR/traceability and the platform advertises support for secure uploads and presigned URLs in the landing page, but actual presigned URL generation endpoint is not present in S3Service code (S3Service returns public URLs from PutObject).

High-level gaps vs EUDR requirements
------------------------------------
The system has a strong foundation (uploads, admin review, exporter entity), but to meet EUDR due diligence the system must implement additional capabilities in the categories below.

1) Data & provenance (critical)
  - Missing batch-level traceability model. EUDR requires traceability at lot/batch level (batch UUIDs linking product to parcel and harvest).
  - Parcel geometry precision: current Zone model uses circle (center + radius). EUDR expects production unit geometry (plot polygon / GeoJSON) and CRS handling.
  - Missing structured document metadata: uploaded documents are stored as URLs only (no structured issuer, issue_date, expiry_date, doc_type, checksum, uploader_id, geotag).

2) Document access & tamper-resistance (critical)
  - No presigned URL generator for short-lived access to private S3 objects (S3Service returns public object URLs after PutObject). We need private storage + presigned access.
  - No checksum or liveness verification stored for documents.
  - No immutable audit trail for document uploads and access events.

3) Risk assessment & mitigation (high)
  - No automated risk scoring (country/commodity/producer-based) or integration with external forest monitoring datasets (e.g., Global Forest Watch, Hansen/GLAD/Sentinel alerts).
  - No mitigation workflow (requests for additional evidence, on-site checks, conditional holds) surfaced programmatically.

4) Verifier & auditor access (high)
  - No dedicated auditor/verifier roles or endpoints to package dossiers for competent authorities.
  - No dossier/report endpoint that bundles presigned URLs and machine-readable metadata for a batch/exporter.

5) Storage, retention, and compliance (required)
  - No retention policy, scheduled archival, or defined retention period metadata (EUDR expects records to be kept for a number of years — implement >= 5 years by default).
  - S3 bucket usage: server-side encryption and strict IAM/bucket policy not enforced in codebase; presigned URL expiry needs to be short and configurable.

6) Spatial monitoring & UI (recommended)
  - Polygon capture, map editor/draw UI, and a parcel dashboard with alert overlays are not implemented.

7) Legal & operational (required)
  - Supplier questionnaires, mandatory supplier declarations, and standard mitigation templates are not present.

Concrete technical requirements (prioritized)
-------------------------------------------
The following are actionable requirements grouped by priority, each including acceptance criteria.

Priority: Critical (implement ASAP)
1. Private S3 storage + presigned URL generator
   - Change: Store uploaded documents and images in a private S3 bucket (do not return public URLs). Update `S3Service` to:
     - Upload objects with server-side encryption (SSE-S3 or SSE-KMS).
     - Return an internal S3 key (not a public URL) and store that in DB.
     - Implement `generatePresignedUrl(key, expirySeconds)` using AWS SDK S3Presigner with configurable default expiry (e.g., 300s).
   - API: `GET /api/documents/{documentId}/presigned?expirySeconds=300` (authz: auditors/admins/exporter-if-owner).
   - Acceptance: Presigned URLs are short-lived, use HTTPS, and the bucket is private; frontends must fetch content with presigned URL.

2. Documents table and metadata
   - DB: create `documents` table with fields: id (uuid), owner_id, batch_id (nullable), entity_type, doc_type (enum), issuer, issue_date, expiry_date, s3_key, file_name, mime_type, checksum (sha256), geotag (lat/long optional), exif_present (bool), uploader_id, uploaded_at, visibility, tags(json).
   - Backend: new service `DocumentService` to validate file types/sizes, extract EXIF geotags, compute checksum, store metadata, call `S3Service.uploadPrivateFile()`.
   - Acceptance: Every uploaded document has a DB record with checksum and uploader metadata.

3. Batch-level traceability model
   - DB: `produce_batches` (or `batch`) table: id (uuid), batch_code (human-readable), produce_listing_id, farm_id, parcel_geojson (or parcel_id foreign key), harvest_date, quantity, unit, created_at, created_by.
   - API: `POST /api/produce-service/batches` to create/assign batch; `GET /api/produce-service/batches/{id}` returns full provenance.
   - Acceptance: Every sale or export line item is tied to a batchId linking to parcel & harvest metadata.

4. Audit log & tamper-evidence
   - DB: `audit_logs` table (append-only): id, entity_type, entity_id, action, actor_id, actor_role, timestamp, details (json), record_hash.
   - Optionally implement periodic hashing of recent changes and store root hash for tamper-evidence (not necessarily blockchain initially).
   - Acceptance: Document uploads, edits, presigned URL generations, and license reviews are logged with actor & timestamp.

Priority: High
5. Dossier / regulator report endpoint
   - API: `GET /api/eudr/report?batchId={id}` returns a signed package (JSON + presigned URLs) or a ZIP containing:
     - Batch metadata (producer, parcel geometry, harvest_date, quantity), document metadata and presigned links, audit log entries for the batch.
   - AuthZ: Only auditor/admin roles or exporter owner (for their own batches) may call; record access in audit_logs.
   - Acceptance: Generated dossier contains all necessary documents and metadata and is accessible via short-lived links.

6. Risk Assessment service & mitigation workflows
   - Implement `RiskAssessmentService` that computes a risk level (LOW/MEDIUM/HIGH) using heuristics: country risk, commodity, land-use change alerts (GFW), supplier history.
   - API: `POST /api/eudr/assess?batchId={id}` or automatic event-based assessment when batch is created or parcel flagged.
   - Add DB fields to store `risk_score` and `risk_reason` and a `mitigation_actions` queue for admins.
   - Acceptance: Risk assessment returns consistent scores, and MEDIUM/HIGH produce triggers a mitigation workflow (manual review, request docs, hold batch).

7. Parcel geometry & spatial database
   - DB: switch to Postgres + PostGIS for spatial queries, or add GeoJSON columns and a PostGIS migration.
   - Store parcel polygons (GeoJSON) for farms/plots, not just a radius circle. Provide a shape editor in frontend zone/farm UI (map draw poly).
   - Acceptance: Parcels can be queried for intersection with forest-loss alerts and used in risk scoring.

Priority: Medium
8. Third-party verifier roles and certificate handling
   - Add Verifier role and API for verifier uploads/notes. Allow linking third-party certificate metadata to batches.
   - Acceptance: Verifier users can upload verification reports and those are reflected in the batch dossier.

9. External data integrations
   - Integrate Global Forest Watch alerts, Sentinel/Hansen datasets or a third-party provider to flag land-cover change for a parcel.
   - Acceptance: The system receives alerts and flags parcels; these feed the risk assessment.

10. Retention & archival
   - Define retention policy (default >= 5 years), implement scheduled archival job to move older data to cold storage and/or mark as archived; maintain audit trail entries.
   - Acceptance: Documents older than retention are archived and still retrievable by regulators per policy.

Security and infrastructure changes
---------------------------------
- AWS S3
  - Use private buckets and server-side encryption (SSE-S3 or SSE-KMS).
  - Generate presigned URLs with S3Presigner; configure short expiration defaults (e.g., 5 minutes) and allow override per endpoint with max cap.
  - Use least-privilege IAM roles for the app (restrict PutObject/GetObject/DeleteObject to specific prefixes).

- Database & storage
  - Prefer Postgres + PostGIS for spatial queries and indexing, or add spatial support via hstore/JSONB if PostGIS is not an option.
  - Use SHA-256 (or stronger) for file checksums and store them in the `documents` table.

- Encryption & privacy
  - Ensure HTTPS for all endpoints; confirm mailers/notifications do not leak confidential data.
  - Assess GDPR impact for personal data in audit logs and documents; add consent capture and data processing notes in the UI and privacy policy.

API and DB change summary (concrete)
-----------------------------------
DB Migrations (Liquibase changeSets):
- Add table `documents` (fields described above).
- Add table `produce_batches`.
- Add table `audit_logs`.
- Add columns to `zones` or `farms` for `parcel_geojson` (nullable) and `geo_precision`.

New/Changed backend endpoints (examples)
- POST /api/documents                (multipart + metadata) -> create Document record + store object privately
- GET  /api/documents/{id}           (metadata only)
- GET  /api/documents/{id}/presigned?expirySeconds=300
- POST /api/produce-service/batches  (create batch, link to parcel & docs)
- GET  /api/produce-service/batches/{id}
- GET  /api/eudr/report?batchId={id} (generate packaged dossier ZIP or JSON + presigned links)
- POST /api/eudr/assess?batchId={id} (run risk assessment)
- GET  /api/eudr/alerts?parcelId={id}

Dependencies & libraries to add or upgrade
-----------------------------------------
- AWS SDK: add S3Presigner usage (software.amazon.awssdk:s3) — already using AWS SDK v2; ensure S3Presigner is available and tested.
- Postgres + PostGIS support in backend: add `org.postgresql:postgresql` driver and consider `com.vladmihalcea:hibernate-types-52` for JSON/GeoJSON mapping.
- Add geospatial client libs on frontend: `leaflet`/`vue2-leaflet` is already present; add drawing plugin (leaflet-draw) for polygon capture.
- Add a scheduler (Spring @Scheduled) or Quartz for archival/retention jobs if not present.

Operational & process requirements
----------------------------------
- Operational SOPs for EUDR: define who in the organization acts on high-risk findings, timelines for mitigation, and how to notify competent authorities.
- Training for admin/verifier roles to interpret risk scores and review evidence.
- Logging & monitoring: ensure access logs, audit logs, and alerts for suspicious behavior; connect to a log aggregator and alerting system.

Acceptance criteria (per feature)
--------------------------------
- Documents: uploaded docs must be stored privately with checksum, metadata, and be retrievable via presigned URL; tests should verify checksum and that a generated presigned URL allows GET only for its expiry window.
- Batches: creating a batch links to a parcel geojson and harvest date; retrieving a batch returns full chain-of-custody.
- Dossier: calling the dossier endpoint returns a ZIP (or machine-readable JSON) containing metadata, presigned links, and audit trail entries, and calling it generates audit logs for access.
- Risk: risk assessment returns a score and a textual rationale. MEDIUM/HIGH must create an actionable mitigation entry in a review queue.

Implementation roadmap & estimates (rough)
----------------------------------------
Phase 1 (2-4 weeks) — Foundation
  - Implement Documents table + `DocumentService` and S3 private uploads + presigned URL generator. Add endpoint for presigned links.
  - Add `produce_batches` table and batch creation endpoint.

Phase 2 (3-6 weeks)
  - Implement audit_logs and presigned access logging.
  - Implement EUDR dossier generation endpoint and exporter/admin UI to download dossier.
  - Add retention/archival scheduler and initial retention policy.

Phase 3 (4-8 weeks)
  - Add risk assessment service (basic country/commodity heuristics), and integrate an external alert source (Global Forest Watch) for parcel monitoring.
  - Add verifier role and advanced mitigation workflow.

Longer-term (6+ weeks)
  - PostGIS migration and advanced spatial queries.
  - Tamper-evidence cryptographic chain (hashchain) and formal audit signing if required.

Immediate next steps I can take for you
--------------------------------------
1. Create Liquibase changeSet for the `documents` table and `produce_batches` table (fast, low-risk).
2. Implement `S3Service.generatePresignedUrl(...)` and a new documents controller endpoint for presigned access (small, testable change).
3. Scaffold `DocumentService` to extract EXIF and compute checksums on upload.

If you want me to start coding, tell me which of the immediate next steps (1..3) to implement first and I'll create the necessary files and tests.

Notes & assumptions
-------------------
- I inspected the backend `S3Service`, `ExporterController`, `AdminLicenseService`, exporter domain and `PROJECT_DOCUMENTATION.md` to build this list. The codebase already supports file uploads but returns public S3 URLs; for compliance we should make storage private and serve via presigned URLs.
- I recommend Postgres+PostGIS for robust spatial queries; an interim approach is to store GeoJSON in JSONB and use external tooling for spatial analysis until PostGIS migration is feasible.
- Legal/regulatory interpretation is context-dependent; this document lists technical and operational capabilities that implement typical EUDR due-diligence needs but does not substitute for legal counsel.

Contact
-------
If you'd like, I can implement the presigned URL endpoint and documents table now (low-risk). Which immediate step should I start with?
