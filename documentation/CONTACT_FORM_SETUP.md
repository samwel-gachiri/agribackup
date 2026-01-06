# Contact Form with Resend Integration

This document describes how the contact form works with Resend email service integration.

## Architecture

The contact form uses a client-server architecture:

1. **Frontend (Vue.js)**: Collects user input and sends to backend API
2. **Backend (Kotlin/Spring Boot)**: Processes requests and sends emails via Resend
3. **Resend API**: Third-party email delivery service

## Why Backend Integration?

The Resend SDK cannot be used directly in the browser because:
- API keys should never be exposed in frontend code (security risk)
- The Resend SDK requires Node.js/server-side environment
- CORS policies prevent direct browser-to-Resend API calls with auth headers

## Setup Instructions

### 1. Get Resend API Key

1. Sign up at [resend.com](https://resend.com)
2. Create an API key in your dashboard
3. Verify your domain (or use the development `onboarding@resend.dev` address)

### 2. Configure Backend

Add the following environment variables:

```bash
# Required
RESEND_API_KEY=re_your_api_key_here

# Optional (defaults provided)
CONTACT_EMAIL=contact@agribackup.com
```

The configuration is defined in `application.yml`:

```yaml
resend:
  api:
    key: ${RESEND_API_KEY}
    url: https://api.resend.com/emails

contact:
  email: ${CONTACT_EMAIL:contact@agribackup.com}
```

### 3. Configure Frontend

The frontend is already configured to use the backend API. Make sure `VUE_APP_API_BASE_URL` in your `.env` file points to your backend:

```env
VUE_APP_API_BASE_URL=http://localhost:8080/
```

## API Endpoint

### POST /api/contact/send

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "subject": "General Inquiry",
  "message": "I would like to know more about your platform."
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Your message has been sent successfully. We'll get back to you soon!",
  "data": {
    "emailId": "xxx-xxx-xxx"
  }
}
```

## Testing

### Backend Test
```bash
curl -X POST http://localhost:8080/api/contact/send \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "subject": "Test Subject",
    "message": "This is a test message"
  }'
```

## Files Created

### Backend
- `ContactController.kt` - REST controller
- `ContactService.kt` - Resend email integration
- `ContactFormRequest.kt` & `ContactFormResponse.kt` - DTOs
- Updated `application.yml` with Resend config

### Frontend
- Updated `contactService.js` to use backend API
