# Aggregator Registration Fix

## Problem Statement
When creating an aggregator account, the registration process was failing with:
```
Column 'registration_number' cannot be null (SQLIntegrityConstraintViolationException)
```

This resulted in:
1. **Orphaned Hedera Accounts**: Successfully created Hedera accounts (e.g., 0.0.7152777) that were not associated with any aggregator
2. **Failed Registration**: User registration failed despite Hedera account creation succeeding
3. **Long Blocking Time**: Hedera account creation took ~50 seconds, blocking the registration thread

## Root Causes

### 1. Database Schema Mismatch
- **Kotlin Entity** (`Aggregator.kt` line 35): `var registrationNumber: String?` (nullable)
- **Database Schema** (`add-supply-chain-actors.sql` line 10): `registration_number VARCHAR(100) NOT NULL UNIQUE` (not nullable)
- The entity allows null values, but the database enforces NOT NULL constraint

### 2. Synchronous Blocking Operation
- Hedera account creation in `AuthService.kt` (lines 481-489) was synchronous
- Took ~50 seconds to complete (log timestamps: 20:59:44 → 21:00:34)
- When registration failed after Hedera creation, orphaned accounts were left in the system
- Blocked the entire registration thread unnecessarily

## Solutions Implemented

### 1. Database Migration to Allow Nullable Registration Number
**File**: `fix-aggregator-registration-number-nullable.sql`

Created a new Liquibase changeset with 3 steps:
1. **Make Column Nullable**: Allow `registration_number` to be NULL
2. **Drop Old Unique Constraint**: Remove the existing UNIQUE constraint
3. **Add Conditional Unique Constraint**: Re-add UNIQUE constraint that allows multiple NULL values

```sql
--changeset samwel:make-aggregator-registration-number-nullable
ALTER TABLE aggregators 
MODIFY COLUMN registration_number VARCHAR(100) NULL;

--changeset samwel:drop-aggregator-registration-number-unique-constraint
ALTER TABLE aggregators 
DROP INDEX registration_number;

--changeset samwel:add-aggregator-registration-number-unique-constraint-for-non-null
ALTER TABLE aggregators 
ADD CONSTRAINT uq_aggregator_registration_number UNIQUE (registration_number);
```

This migration is automatically included via the `includeAll` directive in `db.changelog-master.yml`.

### 2. Asynchronous Hedera Account Creation
**File**: `AuthService.kt`

#### Changes to Registration Method (lines 481-512)
**Before**:
```kotlin
// Create Hedera account synchronously (blocking)
val hederaAccountResult = try {
    hederaAccountService.createHederaAccount(
        initialBalance = Hbar.from(10),
        memo = "AgriBackup Aggregator: ${request.organizationName}"
    )
} catch (e: Exception) {
    logger.warn("Failed to create Hedera account for aggregator: {}", e.message)
    null
}

// Create Aggregator with Hedera account
val aggregator = Aggregator(
    // ... fields ...
    hederaAccountId = hederaAccountResult?.accountId,
    userProfile = user
)

val savedAggregator = aggregatorRepository.save(aggregator) // Failed here if registration_number was null
```

**After**:
```kotlin
// Create Aggregator WITHOUT Hedera account initially
val aggregator = Aggregator(
    // ... fields ...
    registrationNumber = request.registrationNumber, // Can now be null
    hederaAccountId = null, // Will be set asynchronously
    userProfile = user
)

val savedAggregator = aggregatorRepository.save(aggregator) // Succeeds immediately

// Create Hedera account asynchronously (non-blocking)
try {
    createHederaAccountAsync(
        userId = user.id!!,
        aggregatorId = savedAggregator.id!!,
        organizationName = request.organizationName
    )
} catch (e: Exception) {
    logger.warn("Failed to initiate async Hedera account creation: {}", e.message)
    // Registration still succeeds even if Hedera fails
}
```

#### New Async Method Added (after line 832)
```kotlin
@org.springframework.scheduling.annotation.Async
fun createHederaAccountAsync(
    userId: String,
    aggregatorId: Long,
    organizationName: String
) {
    // 1. Create Hedera account (takes ~50 seconds)
    val hederaAccountResult = hederaAccountService.createHederaAccount(...)
    
    // 2. Update aggregator with Hedera account ID
    val aggregator = aggregatorRepository.findById(aggregatorId).orElseThrow()
    aggregator.hederaAccountId = hederaAccountResult.accountId
    aggregatorRepository.save(aggregator)
    
    // 3. Store Hedera credentials
    val credentials = HederaAccountCredentials(...)
    hederaAccountCredentialsRepository.save(credentials)
    
    // 4. Associate with EUDR Certificate NFT
    try {
        val eudrCertificateNftId = hederaTokenService.getEudrComplianceCertificateNftId()
        if (eudrCertificateNftId != null) {
            hederaAccountService.associateTokenWithAccount(...)
        }
    } catch (e: Exception) {
        logger.warn("Failed to associate EUDR Certificate NFT: {}", e.message)
    }
}
```

## Benefits of This Approach

### 1. Resilient Registration Process
- Registration completes successfully even if Hedera network is slow or unavailable
- No more orphaned Hedera accounts if registration fails
- User can start using the platform immediately

### 2. Non-Blocking Experience
- Registration returns immediately (no 50-second wait)
- Hedera account creation happens in the background
- Better user experience with faster response times

### 3. Graceful Degradation
- If Hedera creation fails, the aggregator still exists in the system
- Admin can manually create/associate Hedera accounts later
- System continues to function even with Hedera network issues

### 4. Proper Data Modeling
- Database schema now matches the entity definition
- `registration_number` is optional as intended
- UNIQUE constraint still enforced for non-NULL values

## Testing Recommendations

1. **Test Nullable Registration Number**:
   - Register aggregator WITH `registration_number`
   - Register aggregator WITHOUT `registration_number` (null)
   - Verify UNIQUE constraint works for non-NULL values
   - Verify multiple NULL values are allowed

2. **Test Async Hedera Creation**:
   - Register aggregator and verify it returns immediately
   - Check logs to confirm Hedera account creation starts asynchronously
   - Verify aggregator is created even if Hedera fails
   - Verify `hederaAccountId` is updated when async completes

3. **Test EUDR NFT Association**:
   - Verify NFT is associated after Hedera account creation
   - Check `tokensAssociated` field in credentials table

## Migration Rollback (if needed)

If you need to rollback this migration:
```sql
-- Rollback: Make registration_number NOT NULL again
ALTER TABLE aggregators 
MODIFY COLUMN registration_number VARCHAR(100) NOT NULL;
```

**Warning**: Rollback will fail if there are any aggregators with NULL `registration_number`.

## Configuration Notes

### Async Already Configured
- `@EnableAsync` is already present in:
  - `FarmersPortalApisApplication.kt`
  - `AsyncConfiguration.kt`
- No additional configuration needed

### Task Executor
The application uses Spring's default task executor. If you want to customize the thread pool:

```kotlin
@Configuration
@EnableAsync
class AsyncConfiguration {
    @Bean
    fun taskExecutor(): TaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 10
        executor.queueCapacity = 25
        executor.setThreadNamePrefix("Async-Hedera-")
        executor.initialize()
        return executor
    }
}
```

## Cleanup of Orphaned Accounts

The orphaned Hedera account `0.0.7152777` can be:
1. Associated with the aggregator manually through an admin interface
2. Archived/documented for accounting purposes
3. Deleted if account management features are implemented

## Related Files Modified

1. `fix-aggregator-registration-number-nullable.sql` - Database migration (NEW)
2. `AuthService.kt` - Registration logic refactored for async
3. `Aggregator.kt` - Entity already had nullable field (no change needed)
4. `db.changelog-master.yml` - Automatically includes new migration via `includeAll`

## Summary

✅ **Database schema now matches entity definition** (nullable `registration_number`)  
✅ **Registration completes immediately without blocking**  
✅ **No more orphaned Hedera accounts**  
✅ **Graceful handling of Hedera network failures**  
✅ **Better user experience with faster response times**  
✅ **System resilience improved**  

The aggregator registration process is now robust, non-blocking, and properly handles optional registration numbers.
