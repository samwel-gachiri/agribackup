# Google Analytics Events Documentation

This document lists all custom events tracked in the AgriBackup application and how to analyze them in Google Analytics 4.

## Table of Contents
- [Automatic Tracking](#automatic-tracking)
- [Button Click Events](#button-click-events)
- [Form Events](#form-events)
- [Portal Selection Events](#portal-selection-events)
- [Navigation Events](#navigation-events)
- [Dialog Events](#dialog-events)
- [Engagement Events](#engagement-events)
- [How to View in GA4](#how-to-view-in-ga4)
- [Custom Reports](#custom-reports)

## Automatic Tracking

These events are tracked automatically without any code changes:

### Page Views
- **Event**: `page_view`
- **Triggered**: On every route change
- **Parameters**:
  - `page_path`: URL path (e.g., `/landing`, `/signup`)
  - `page_title`: Page title from vue-meta

### Scroll Depth
- **Event**: `scroll_depth`
- **Triggered**: When user scrolls to 25%, 50%, 75%, or 100% of page
- **Parameters**:
  - `scroll_percentage`: 25, 50, 75, or 100
  - `page_path`: Current page path

### Engagement Time
- **Event**: `engagement_time`
- **Triggered**: When user leaves page or tab becomes inactive
- **Parameters**:
  - `engagement_seconds`: Time spent actively viewing the page
  - `page_path`: Page where time was spent

## Button Click Events

### Landing Page CTAs

#### Get Started
- **Event**: `button_click`
- **Location**: Landing page hero section
- **Parameters**:
  - `button_name`: `get_started`
  - `button_location`: `/landing`
  - `source`: `hero_section`
  - `section`: `above_fold`

#### Learn More
- **Event**: `button_click`
- **Location**: Landing page hero section
- **Parameters**:
  - `button_name`: `learn_more`
  - `button_location`: `/landing`
  - `source`: `hero_section`
  - `section`: `above_fold`

#### Login (Navbar)
- **Event**: `button_click`
- **Location**: Landing page navbar
- **Parameters**:
  - `button_name`: `login_navbar`
  - `button_location`: `/landing`
  - `source`: `navbar`

#### Create Account (Navbar)
- **Event**: `button_click`
- **Location**: Landing page navbar
- **Parameters**:
  - `button_name`: `create_account_navbar`
  - `button_location`: `/landing`
  - `source`: `navbar`

#### Join as Farmer
- **Event**: `button_click`
- **Location**: Landing page "For Farmers" section
- **Parameters**:
  - `button_name`: `join_as_farmer`
  - `button_location`: `/landing`
  - `source`: `landing_page`
  - `section`: `for_farmers`

#### Join as Buyer
- **Event**: `button_click`
- **Location**: Landing page "For Buyers" section
- **Parameters**:
  - `button_name`: `join_as_buyer`
  - `button_location`: `/landing`
  - `source`: `landing_page`
  - `section`: `for_buyers`

#### Join EUDR Platform (Exporter)
- **Event**: `button_click`
- **Location**: Landing page "For Exporters" section
- **Parameters**:
  - `button_name`: `join_eudr_platform`
  - `button_location`: `/landing`
  - `source`: `landing_page`
  - `section`: `for_exporters`

## Form Events

### Sign Up Form

#### Form Start
- **Event**: `form_start`
- **Triggered**: When user begins filling signup form (first input focus)
- **Parameters**:
  - `form_name`: `signup_{portal_type}` (e.g., `signup_farmer`)
  - `page_path`: `/signup`

#### Form Submit Success
- **Event**: `form_submit_success`
- **Triggered**: When signup is completed successfully
- **Parameters**:
  - `form_name`: `signup_{portal_type}`
  - `portal_type`: farmer, buyer, exporter, etc.
  - `role_context`: Specific role if applicable

#### Form Submit Error
- **Event**: `form_submit_error`
- **Triggered**: When signup fails
- **Parameters**:
  - `form_name`: `signup_{portal_type}`
  - `error_type`: Type of error encountered

### Sign In Form

#### Login Attempt
- **Event**: `form_attempt`
- **Triggered**: When user clicks Sign In button
- **Parameters**:
  - `form_name`: `signin`
  - `portal_type`: farmer, buyer, exporter, etc.
  - `input_type`: email or phone

#### Login Success
- **Event**: `form_submit_success`
- **Triggered**: When login is successful
- **Parameters**:
  - `form_name`: `signin`
  - `portal_type`: farmer, buyer, exporter, etc.
  - `role_type`: FARMER, BUYER, EXPORTER, etc.
  - `input_type`: email or phone

#### Login Failure
- **Event**: `form_submit_error`
- **Triggered**: When login fails
- **Parameters**:
  - `form_name`: `signin`
  - `portal_type`: farmer, buyer, exporter, etc.
  - `error_type`: 
    - `user_not_found`: User doesn't exist
    - `missing_role`: User exists but doesn't have required role
    - `auth_failed`: Wrong password or other auth error

## Portal Selection Events

### Portal Selected
- **Event**: `portal_selected`
- **Triggered**: When user selects a portal type
- **Parameters**:
  - `portal_type`: farmer, buyer, exporter, aggregator, processor, importer
  - `selection_context`: Where selection happened (e.g., `signup_portal_selection`, `landing_page`)
  - `page_path`: Current page

## Navigation Events

### Go to Sign In
- **Event**: `navigation_click`
- **Triggered**: When user clicks "Sign In" link from signup page
- **Parameters**:
  - `link_name`: `go_to_signin`
  - `destination`: `signin_page`
  - `from_page`: `/signup`

### Create Account from Sign In
- **Event**: `navigation_click`
- **Triggered**: When user clicks "Create Account" from signin page
- **Parameters**:
  - `link_name`: `create_account_from_signin`
  - `destination`: `signup_page`
  - `from_page`: `/signin`

## Dialog Events

### How It Works Dialog

#### Dialog Open
- **Event**: `dialog_interaction`
- **Triggered**: When "Learn More" dialog opens
- **Parameters**:
  - `dialog_name`: `how_it_works`
  - `dialog_action`: `open`
  - `page_path`: `/landing`

#### Dialog Close
- **Event**: `dialog_interaction`
- **Triggered**: When dialog is closed
- **Parameters**:
  - `dialog_name`: `how_it_works`
  - `dialog_action`: `close`
  - `page_path`: `/landing`

## Engagement Events

### User Action
- **Event**: `user_action`
- **Triggered**: For custom user actions
- **Parameters**:
  - `action_name`: Name of the action
  - `page_path`: Current page
  - `page_title`: Current page title
  - Additional custom parameters

## How to View in GA4

### Real-time Events
1. Go to **Reports** → **Realtime**
2. Scroll to "Event count by Event name"
3. See all events firing in real-time

### All Events
1. Go to **Reports** → **Engagement** → **Events**
2. View all events with counts and parameters
3. Click any event to see detailed breakdown

### Event Parameters
1. Go to **Configure** → **Custom definitions**
2. Create custom dimensions for parameters:
   - `button_name`
   - `portal_type`
   - `error_type`
   - `section`
   - etc.

### DebugView (Development)
1. Go to **Configure** → **DebugView**
2. Enable analytics in development mode
3. See events in real-time with full parameter details

## Custom Reports

### Signup Funnel
Create a funnel exploration to track:
1. **Landing Page View** → `page_view` (path: `/landing`)
2. **Portal Selected** → `portal_selected`
3. **Signup Page View** → `page_view` (path: `/signup`)
4. **Form Start** → `form_start`
5. **Form Submit** → `form_submit_success`

**How to create**:
1. Go to **Explore** → Create new exploration
2. Select **Funnel exploration** template
3. Add steps as listed above
4. Add breakdown by `portal_type` to see which portal has best conversion

### CTA Performance
Create a report to compare button clicks:
1. Go to **Explore** → Create new exploration
2. Select **Free form** template
3. Add dimension: `button_name`
4. Add metrics: Event count, Users
5. Filter: Event name = `button_click`

### Bounce Analysis
1. Go to **Reports** → **Engagement** → **Pages and screens**
2. Add secondary dimension: **Exit rate**
3. Sort by exit rate to find where users leave
4. Cross-reference with scroll depth events to see engagement

### Login Success Rate
Create a calculated metric:
1. Numerator: `form_submit_success` (form_name = signin)
2. Denominator: `form_attempt` (form_name = signin)
3. Formula: (Success / Attempts) * 100

### Portal Popularity
1. Go to **Explore** → Create new exploration
2. Add dimension: `portal_type`
3. Add metric: Event count
4. Filter: Event name = `portal_selected`
5. Visualize as pie chart

## Conversions

Mark these events as conversions in GA4:

1. **Signup Success**: `form_submit_success` (form_name contains "signup")
2. **Login Success**: `form_submit_success` (form_name = "signin")
3. **Portal Selection**: `portal_selected`

**How to mark as conversion**:
1. Go to **Configure** → **Events**
2. Find the event
3. Toggle "Mark as conversion"

## Tips for Analysis

### Find Bounce Points
1. Check **Pages and screens** report for exit rates
2. Look at scroll depth events - if users don't scroll past 25%, content may not be engaging
3. Check engagement time - low engagement time indicates quick exits

### Optimize CTAs
1. Compare click rates of different CTAs
2. A/B test button text by tracking different `button_name` values
3. See which sections get most engagement

### Improve Signup Flow
1. Track form abandonment (form_start without form_submit)
2. Identify error types causing signup failures
3. See which portal types have best conversion rates

### Monitor User Journey
1. Use **Path exploration** to see common user flows
2. Identify unexpected navigation patterns
3. Find pages where users get stuck
