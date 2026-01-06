# Contact Form with Resend Integration

## Overview
The contact form now uses Resend API to send emails when users submit the contact form. This provides a reliable email delivery service with professional HTML templates.

## Features
- ✅ Sends contact form submissions to `contact@agribackup.com`
- ✅ Sends automatic confirmation email to the user
- ✅ Professional HTML email templates with AgriBackup branding
- ✅ Real-time form validation
- ✅ Success/Error feedback messages
- ✅ Loading states during submission
- ✅ Reply-to header set to user's email for easy responses

## Configuration

### Environment Variable
The Resend API key is already configured in `.env`:
```env
VUE_APP_RESEND_API_KEY="re_7cUozv5K_5GQtwA1kL6YnCgbMr5dEMs7L"
```

### Email Settings
To use your custom domain instead of `onboarding@resend.dev`:

1. Verify your domain in Resend dashboard
2. Update the `from` address in `contactService.js`:
   ```javascript
   from: 'AgriBackup Contact <noreply@yourdomain.com>'
   ```

### Receiving Email
Update the `to` address in `contactService.js` line 26:
```javascript
to: ['contact@agribackup.com'], // Change to your email
```

## Files Created/Modified

### New Files
- `frontend/src/services/contactService.js` - Service for handling Resend API calls

### Modified Files
- `frontend/src/views/Contact.vue` - Updated to use contactService

## Email Templates

### 1. Admin Notification Email
Sent to `contact@agribackup.com` when a user submits the form:
- Contact details (name, email, subject)
- Full message content
- Reply button linking to user's email
- Timestamp of submission

### 2. User Confirmation Email
Sent to the user after successful submission:
- Personalized greeting
- Confirmation of receipt
- Expected response time (24 hours)
- Contact information for urgent matters
- Professional branding

## Usage

Users can submit messages through the contact form at `/contact`. The form includes:
- Full Name (required)
- Email Address (required)
- Subject dropdown with options:
  - General Inquiry
  - Technical Support
  - Sales & Pricing
  - EUDR Compliance
- Message (required)

## Error Handling
- Network errors are caught and displayed to users
- Failed submissions show error message with fallback contact info
- Success/Error messages auto-dismiss after 8-10 seconds

## Testing

To test the integration:
1. Navigate to the Contact page
2. Fill out the form
3. Submit and verify:
   - Success message appears
   - Admin receives notification email
   - User receives confirmation email

## API Limits
Free tier Resend limits:
- 100 emails per day
- 3,000 emails per month

For production, consider upgrading to a paid plan.

## Security Notes
- API key is stored in environment variables (not committed to git)
- Email validation on frontend
- Rate limiting should be added for production
- Consider adding CAPTCHA to prevent spam

## Future Enhancements
- Add attachment support
- Implement rate limiting
- Add reCAPTCHA or similar spam protection
- Create email templates for different subjects
- Add email notification preferences
- Implement email queue for failed deliveries
