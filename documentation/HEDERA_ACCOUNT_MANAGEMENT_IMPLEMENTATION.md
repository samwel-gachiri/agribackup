# Hedera Account Management Implementation

## ✅ Completed Implementation

### 1. **HederaAccountService** (Platform-Managed Accounts)

**File**: `service/hedera/HederaAccountService.kt`

**Key Features**:
- ✅ Automatic Hedera account creation for new users
- ✅ Secure private key encryption (AES-GCM)
- ✅ Token association automation
- ✅ Account balance queries
- ✅ Token transfer functionality
- ✅ Credential export for user backup

**Methods Implemented**:
```kotlin
// Create new Hedera account with encrypted key storage
createHederaAccount(initialBalance, memo): HederaAccountCreationResult

// Associate tokens with account (required before receiving tokens)
associateTokenWithAccount(accountId, encryptedPrivateKey, tokenId): Boolean

// Associate multiple tokens at once
associateMultipleTokens(accountId, encryptedPrivateKey, tokenIds): Boolean

// Get account HBAR balance
getAccountBalance(accountId): AccountBalance?

// Get specific token balance
getTokenBalance(accountId, tokenId): Long

// Transfer HBAR between accounts
transferHbar(fromAccountId, fromKey, toAccountId, amount): Boolean

// Export credentials for user backup (WARNING: Sensitive operation)
exportAccountCredentials(accountId, encryptedKey, userId): AccountCredentialsExport
```

**Security Features**:
- ✅ Private keys encrypted with AES-256-GCM
- ✅ Encryption key from environment variable
- ✅ Secure random IV generation
- ✅ Base64 encoding for storage
- ✅ Audit logging for sensitive operations

---

### 2. **HederaAccountCredentials** Entity

**File**: `domain/hedera/HederaAccountCredentials.kt`

**Database Table**: `hedera_account_credentials`

**Fields**:
- `id`: UUID primary key
- `userId`: Link to user profile (unique)
- `entityType`: AGGREGATOR, PROCESSOR, IMPORTER, EXPORTER, FARMER
- `entityId`: ID of the supply chain entity
- `hederaAccountId`: Hedera account ID (e.g., 0.0.123456)
- `publicKey`: Public key (can be shared)
- `encryptedPrivateKey`: AES-encrypted private key (sensitive!)
- `initialBalanceHbar`: Initial HBAR balance
- `accountMemo`: Account description
- `creationTransactionId`: Hedera transaction ID of account creation
- `tokensAssociated`: JSON array of associated token IDs
- `isActive`: Account status flag
- `createdAt`: Creation timestamp
- `lastUsedAt`: Last activity timestamp
- `notes`: Additional notes

**Security**:
- Private keys NEVER stored in plain text
- Foreign key to user_profiles with CASCADE delete
- Unique constraints on user_id and hedera_account_id

---

### 3. **HederaAccountCredentialsRepository**

**File**: `infrastructure/repositories/HederaAccountCredentialsRepository.kt`

**Query Methods**:
```kotlin
findByUserId(userId): Optional<HederaAccountCredentials>
findByHederaAccountId(hederaAccountId): Optional<HederaAccountCredentials>
findByEntityTypeAndEntityId(entityType, entityId): Optional<HederaAccountCredentials>
findByEntityType(entityType): List<HederaAccountCredentials>
findByIsActive(isActive): List<HederaAccountCredentials>
existsByHederaAccountId(hederaAccountId): Boolean
```

---

### 4. **Updated HederaConsensusServices**

**New Methods Added**:
```kotlin
// Record account creation on consensus service
recordAccountCreation(accountId, memo): String

// Record token association event
recordTokenAssociation(accountId, tokenId): String
```

**Result**: All account operations now have blockchain audit trail

---

### 5. **Updated AggregatorService** (Automatic Account Creation)

**File**: `service/supplychain/AggregatorService.kt`

**Changes**:
- Added dependencies: `HederaAccountService`, `HederaAccountCredentialsRepository`, `HederaTokenService`
- Modified `createAggregator()` method to:
  1. Create user profile
  2. **Automatically create Hedera account** (10 HBAR initial balance)
  3. Create aggregator entity with Hedera account ID
  4. **Store encrypted credentials** in database
  5. **Automatically associate with EUDR Compliance Token**
  6. Return aggregator DTO

**Workflow**:
```
Register Aggregator
    ↓
Create User Profile
    ↓
Create Hedera Account → Record on blockchain
    ↓
Encrypt & Store Private Key → Database
    ↓
Create Aggregator Entity → Link Hedera Account ID
    ↓
Associate with EUDR Token → Record on blockchain
    ↓
Return Success (Hedera account ready to receive tokens)
```

---

### 6. **Database Migration**

**File**: `db/changelog/hedera-account-credentials-changelog.yml`

**Migration Details**:
- Creates `hedera_account_credentials` table
- Adds indexes on `user_id`, `entity_type + entity_id`, `hedera_account_id`
- Foreign key constraint to `user_profiles` with CASCADE delete
- Default values for `is_active` and `created_at`

**Added to Master Changelog**: `db.changelog-master.yml`

---

## 🔄 Registration Flow (Example: Aggregator)

### Before (Manual Hedera Account)
```
POST /api/v1/aggregators
{
  "email": "coop@example.com",
  "organizationName": "Farm Cooperative",
  "hederaAccountId": "0.0.123456"  // User must provide this
}
```
❌ User needs to create Hedera account manually
❌ User needs to associate tokens manually
❌ Complex onboarding

### After (Automatic Hedera Account) ✅
```
POST /api/v1/aggregators
{
  "email": "coop@example.com",
  "organizationName": "Farm Cooperative"
  // No hederaAccountId needed!
}

Response:
{
  "id": "AGG-123",
  "organizationName": "Farm Cooperative",
  "hederaAccountId": "0.0.789012",  // Automatically created!
  "verificationStatus": "PENDING"
}
```
✅ Hedera account created automatically
✅ EUDR token associated automatically
✅ Ready to receive compliance tokens
✅ Simple onboarding

---

## 🎯 Next Steps to Complete Token Integration

### For Processor Registration:
Update `ProcessorService.createProcessor()` with same pattern as AggregatorService

### For Importer Registration:
Update `ImporterService.createImporter()` with same pattern as AggregatorService

### For Farmer Registration:
Consider if farmers need Hedera accounts (probably yes, for carbon credit tokens)

### For Exporter:
Update exporter registration flow

### Token Minting Flow:
```kotlin
// When shipment passes EUDR compliance
fun mintComplianceToken(shipmentId: String, exporterId: String) {
    // Get exporter's Hedera credentials
    val credentials = hederaAccountCredentialsRepository
        .findByEntityTypeAndEntityId("EXPORTER", exporterId)
        .orElseThrow { ... }
    
    // Mint token to exporter's account
    val accountId = AccountId.fromString(credentials.hederaAccountId)
    hederaTokenService.mintComplianceTokens(
        recipientAccountId = accountId,
        amount = 1,
        reason = "EUDR_COMPLIANT_SHIPMENT",
        batchId = shipmentId
    )
}
```

---

## 🔐 Security Considerations

### ✅ Implemented:
- Private keys encrypted with AES-256-GCM
- Encryption key from environment variable
- Secure random IV for each encryption
- Foreign key constraints with CASCADE delete
- Audit logging on blockchain

### ⚠️ Production Requirements:
1. **Encryption Key Management**:
   - Store `HEDERA_KEY_ENCRYPTION_SECRET` in secure vault (AWS Secrets Manager, Azure Key Vault)
   - Rotate encryption keys periodically
   - Use 32+ byte random key

2. **Access Control**:
   - Restrict access to `hedera_account_credentials` table
   - Audit all credential access
   - Never expose encrypted keys in API responses

3. **Backup Strategy**:
   - Encrypted database backups
   - Secure key backup separate from data
   - User credential export feature (one-time use)

4. **Account Recovery**:
   - Implement secure recovery flow
   - Multi-factor authentication
   - Consider multi-signature accounts for high-value entities

---

## 📊 Database Schema

```sql
CREATE TABLE hedera_account_credentials (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) UNIQUE NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(36) NOT NULL,
    hedera_account_id VARCHAR(50) UNIQUE NOT NULL,
    public_key VARCHAR(255) NOT NULL,
    encrypted_private_key TEXT NOT NULL,
    initial_balance_hbar VARCHAR(20),
    account_memo VARCHAR(255),
    creation_transaction_id VARCHAR(100),
    tokens_associated TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    notes TEXT,
    FOREIGN KEY (user_id) REFERENCES user_profiles(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_hedera_credentials_user_id ON hedera_account_credentials(user_id);
CREATE INDEX idx_hedera_credentials_entity ON hedera_account_credentials(entity_type, entity_id);
CREATE INDEX idx_hedera_credentials_account_id ON hedera_account_credentials(hedera_account_id);
```

---

## 🚀 Testing the Implementation

### 1. Start Application
```bash
cd farmers-portal-apis
./mvnw spring-boot:run
```

### 2. Register Aggregator with Auto Account Creation
```bash
curl -X POST http://localhost:8080/api/v1/aggregators \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "fullName": "Test Aggregator",
    "phoneNumber": "+1234567890",
    "organizationName": "Test Cooperative",
    "organizationType": "COOPERATIVE",
    "facilityAddress": "123 Farm St",
    "primaryCommodities": ["coffee", "cocoa"]
  }'
```

### 3. Check Database
```sql
SELECT * FROM hedera_account_credentials WHERE entity_type = 'AGGREGATOR';
```

### 4. Verify on Hedera
```bash
# Get account info (if network is configured)
curl https://testnet.mirrornode.hedera.com/api/v1/accounts/{ACCOUNT_ID}
```

---

## 📝 Environment Variables Required

```bash
# Add to application.yml or environment
HEDERA_KEY_ENCRYPTION_SECRET=your_32_byte_random_key_here_change_in_production

# Existing Hedera config
HEDERA_ACCOUNT_ID=0.0.YOUR_OPERATOR_ACCOUNT
HEDERA_PRIVATE_KEY=your_operator_private_key
HEDERA_NETWORK_TYPE=testnet
```

---

**Status**: ✅ Core account management implemented
**Next**: Apply same pattern to Processor, Importer, Exporter registration flows
**Goal**: All supply chain actors automatically get Hedera accounts + EUDR token association
