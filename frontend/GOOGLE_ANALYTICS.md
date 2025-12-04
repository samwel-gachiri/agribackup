# Google Analytics Configuration

## Environment Variable Setup

To enable Google Analytics tracking, add your GA4 Measurement ID to your `.env` file:

```env
VUE_APP_GA_MEASUREMENT_ID=G-XXXXXXXXXX
```

Replace `G-XXXXXXXXXX` with your actual Google Analytics 4 Measurement ID.

## Getting Your Measurement ID

1. Go to [Google Analytics](https://analytics.google.com/)
2. Select your property or create a new one
3. Navigate to **Admin** → **Data Streams**
4. Select your web data stream
5. Copy the **Measurement ID** (format: `G-XXXXXXXXXX`)

## Features

- **Automatic Page View Tracking**: Page views are automatically tracked when navigating between routes
- **Scroll Depth Tracking**: Automatically tracks when users scroll to 25%, 50%, 75%, and 100% of pages
- **Engagement Time Tracking**: Measures actual time users spend actively viewing pages
- **Button Click Tracking**: All major CTAs and buttons are tracked with context
- **Form Analytics**: Comprehensive tracking of form starts, submissions, errors, and abandonment
- **Portal Selection Tracking**: Tracks which user types (farmer, buyer, exporter) are most popular
- **Custom Event Tracking**: Use `this.$trackEvent(eventName, eventParams)` in components
- **Development Mode**: Analytics is disabled in development by default (set `enableInDev: true` in main.js to enable)
- **Production Ready**: Automatically enabled in production builds

## Enhanced Tracking Features

### Automatic Metrics
- **Page Views**: Every route change is tracked with page path and title
- **Scroll Depth**: Tracks user engagement at 25%, 50%, 75%, and 100% scroll points
- **Engagement Time**: Measures active time spent on each page
- **Exit Pages**: Identifies where users leave your application

### User Interaction Tracking
- **CTA Performance**: All call-to-action buttons tracked (Get Started, Join as Farmer/Buyer/Exporter, etc.)
- **Form Funnel**: Complete signup/signin flow tracking from start to completion
- **Portal Selection**: Tracks which user types are signing up
- **Navigation Patterns**: Tracks how users move between pages
- **Dialog Interactions**: Tracks modal/dialog opens and closes

### Conversion Tracking
- **Signup Conversions**: Tracks successful account creations by portal type
- **Login Success/Failure**: Monitors authentication success rates and error types
- **Form Abandonment**: Identifies where users drop off in forms

For a complete list of all tracked events and their parameters, see [GOOGLE_ANALYTICS_EVENTS.md](./GOOGLE_ANALYTICS_EVENTS.md).

## Usage Examples

### Track a Custom Event
```javascript
// In any Vue component
this.$trackEvent('button_click', {
  button_name: 'signup',
  page: 'landing'
});
```

### Track a Form Submission
```javascript
this.$trackEvent('form_submit', {
  form_name: 'contact_form',
  success: true
});
```

### Direct gtag Access
```javascript
// Access gtag directly if needed
this.$gtag('event', 'custom_event', {
  custom_param: 'value'
});
```

## Verification

1. Build and run the application in production mode
2. Open Google Analytics dashboard
3. Go to **Reports** → **Realtime**
4. Navigate through your app
5. Verify page views and events appear in real-time

## Notes

- Analytics tracking is only active in production builds by default
- The Measurement ID is read from environment variables for security
- Page titles are automatically captured from vue-meta or route meta
