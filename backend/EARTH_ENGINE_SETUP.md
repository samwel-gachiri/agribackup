# Earth Engine Setup Guide

This guide explains how to set up Google Earth Engine for satellite imagery analysis (Sentinel-2 and Landsat).

## Prerequisites

- Google Cloud Platform (GCP) account
- gcloud CLI installed
- A GCP project

## Setup Steps

### 1. Create or Select a GCP Project

```bash
# Use your existing project that's registered with Earth Engine
gcloud config set project agribackup

# Or if you need to create a new one:
# gcloud projects create agribackup --name="AgriBackup"
```

**IMPORTANT**: Use the same project ID that you registered with Earth Engine at https://code.earthengine.google.com/register

### 2. Enable Earth Engine API

**For Windows CMD:**
```cmd
REM Make sure you're using the correct project
gcloud config set project agribackup

REM Enable the Earth Engine API
gcloud services enable earthengine.googleapis.com

REM Verify it's enabled
gcloud services list --enabled | findstr earthengine
```

**For Linux/Mac/PowerShell:**
```bash
# Enable the Earth Engine API
gcloud services enable earthengine.googleapis.com

# Verify it's enabled
gcloud services list --enabled | grep earthengine
```

### 3. Create a Service Account

**For Windows CMD:**
```cmd
REM Create service account
gcloud iam service-accounts create earth-engine-sa --display-name="Earth Engine Service Account" --description="Service account for Earth Engine API access"

REM Set the service account email variable
SET SERVICE_ACCOUNT_EMAIL=earth-engine-sa@agribackup.iam.gserviceaccount.com
```

**For Linux/Mac/PowerShell:**
```bash
# Create service account
gcloud iam service-accounts create earth-engine-sa \
  --display-name="Earth Engine Service Account" \
  --description="Service account for Earth Engine API access"

# Get the service account email
export SERVICE_ACCOUNT_EMAIL=earth-engine-sa@agribackup.iam.gserviceaccount.com
```

### 4. Grant Earth Engine Permissions

**For Windows CMD:**
```cmd
REM Grant Earth Engine viewer permission
gcloud projects add-iam-policy-binding agribackup --member="serviceAccount:earth-engine-sa@agribackup.iam.gserviceaccount.com" --role="roles/earthengine.viewer"

REM Grant storage permissions
gcloud projects add-iam-policy-binding agribackup --member="serviceAccount:earth-engine-sa@agribackup.iam.gserviceaccount.com" --role="roles/storage.objectViewer"
```

**For Linux/Mac/PowerShell:**
```bash
# Grant Earth Engine viewer permission
gcloud projects add-iam-policy-binding agribackup \
  --member="serviceAccount:${SERVICE_ACCOUNT_EMAIL}" \
  --role="roles/earthengine.viewer"

# Grant storage permissions (for accessing Earth Engine data)
gcloud projects add-iam-policy-binding agribackup \
  --member="serviceAccount:${SERVICE_ACCOUNT_EMAIL}" \
  --role="roles/storage.objectViewer"
```

### 5. Generate Service Account Key

**For Windows CMD:**
```cmd
REM Create key file in Downloads folder
gcloud iam service-accounts keys create %USERPROFILE%\Downloads\agribackup-cloud.json --iam-account=earth-engine-sa@agribackup.iam.gserviceaccount.com

REM Move to secure location (adjust path as needed)
move %USERPROFILE%\Downloads\agribackup-cloud.json G:\source\key\agribackup-cloud.json
```

**For Linux/Mac/PowerShell:**
```bash
# Create key file
gcloud iam service-accounts keys create ~/agribackup-cloud.json \
  --iam-account=${SERVICE_ACCOUNT_EMAIL}

# Move to secure location
mv ~/agribackup-cloud.json G:/source/key/agribackup-cloud.json
```

### 6. Register for Earth Engine

**IMPORTANT**: You must register your Google account for Earth Engine access:

1. Visit: https://code.earthengine.google.com/register
2. Sign in with your Google account
3. Fill out the registration form
4. Wait for approval (usually takes 1-2 days)

### 7. Configure Application

Set environment variables in your IDE or terminal:

**Windows PowerShell:**
```powershell
$env:EARTH_ENGINE_ENABLED="true"
$env:EARTH_ENGINE_PROJECT_ID="agribackup"
$env:EARTH_ENGINE_CREDENTIALS_PATH="G:/source/key/agribackup-cloud.json"
```

**Linux/Mac:**
```bash
export EARTH_ENGINE_ENABLED=true
export EARTH_ENGINE_PROJECT_ID=agribackup
export EARTH_ENGINE_CREDENTIALS_PATH=/path/to/agribackup-cloud.json
```

**Or update application.yml:**
```yaml
earth-engine:
  enabled: true
  project-id: agribackup
  credentials-path: G:/source/key/agribackup-cloud.json
```

## Verification

### Test Earth Engine Access

Run the application and check logs for:

```
✅ SUCCESS: Satellite imagery service is available
✅ SUCCESS: Earth Engine connection validated
```

### Troubleshooting

#### Error: 404 - Project not found

```
❌ Earth Engine project not found or API not enabled
```

**Solution:**
1. Verify project ID is correct
2. Ensure Earth Engine API is enabled: `gcloud services list --enabled | grep earthengine`
3. Check service account has correct permissions

#### Error: 401/403 - Authentication failed

```
❌ Authentication failed. Please check service account credentials
```

**Solution:**
1. Verify credentials file path is correct
2. Check service account has `roles/earthengine.viewer` permission
3. Ensure credentials file is valid JSON

#### Error: Service account not registered

```
❌ Service account email not registered for Earth Engine
```

**Solution:**
1. Register at https://code.earthengine.google.com/register
2. Wait for approval (1-2 days)
3. Link service account to Earth Engine project

## Fallback Mode

If Earth Engine setup fails, the system will automatically fallback to:
1. **Landsat** (if Sentinel-2 fails)
2. **Global Forest Watch API** (if both satellite sources fail)

To disable satellite imagery and use only GFW:

```yaml
deforestation:
  use-satellite-imagery: false
```

## Cost Considerations

- **Earth Engine API**: Free tier available (limited requests/day)
- **Storage**: Minimal (only query results stored)
- **Compute**: Serverless, pay-per-use

### Free Tier Limits:
- 250,000 requests per day
- 1,000 concurrent requests
- 100,000 compute units per day

For production usage exceeding free tier, consider:
- Enabling billing in GCP console
- Setting up quotas and alerts
- Implementing request caching

## Best Practices

1. **Cache Results**: Store analysis results to minimize API calls
2. **Batch Processing**: Process multiple units together
3. **Error Handling**: Always have GFW fallback enabled
4. **Monitoring**: Track API usage in GCP console
5. **Security**: Keep credentials file secure, never commit to git

## Support

For Earth Engine issues:
- Earth Engine Forum: https://groups.google.com/g/google-earth-engine-developers
- GCP Support: https://cloud.google.com/support
- Documentation: https://developers.google.com/earth-engine

## Current Status

Check application startup logs:

```
INFO  c.a.f.config.EarthEngineConfig : Earth Engine enabled: [status]
INFO  c.a.f.config.EarthEngineConfig : Project ID: [project-id]
INFO  c.a.f.service.satellite.SatelliteImageryService : Satellite imagery service is available
```
