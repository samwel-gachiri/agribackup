/* eslint-disable */
/**
 * Google Analytics 4 Integration Plugin
 * 
 * This plugin initializes Google Analytics 4 tracking for the application.
 * It loads the gtag.js script dynamically and configures page view tracking.
 */

const GA_MEASUREMENT_ID = process.env.VUE_APP_GA_MEASUREMENT_ID;
const IS_PRODUCTION = process.env.NODE_ENV === 'production';

/**
 * Load Google Analytics gtag.js script
 */
function loadGtagScript() {
  if (!GA_MEASUREMENT_ID) {
    console.warn('Google Analytics: Measurement ID not configured');
    return;
  }

  // Create and inject the gtag.js script
  const script = document.createElement('script');
  script.async = true;
  script.src = `https://www.googletagmanager.com/gtag/js?id=${GA_MEASUREMENT_ID}`;
  document.head.appendChild(script);

  // Initialize gtag
  window.dataLayer = window.dataLayer || [];
  window.gtag = function() {
    window.dataLayer.push(arguments);
  };
  
  window.gtag('js', new Date());
  window.gtag('config', GA_MEASUREMENT_ID, {
    send_page_view: false, // We'll handle page views manually via router
  });

  console.log('Google Analytics initialized:', GA_MEASUREMENT_ID);
}

/**
 * Track a page view
 * @param {string} path - The page path
 * @param {string} title - The page title
 */
export function trackPageView(path, title) {
  if (!GA_MEASUREMENT_ID || !window.gtag) return;
  
  window.gtag('config', GA_MEASUREMENT_ID, {
    page_path: path,
    page_title: title,
  });
}

/**
 * Track a custom event
 * @param {string} eventName - The event name
 * @param {object} eventParams - Event parameters
 */
export function trackEvent(eventName, eventParams = {}) {
  if (!GA_MEASUREMENT_ID || !window.gtag) return;
  
  window.gtag('event', eventName, eventParams);
}

/**
 * Track scroll depth
 * @param {number} percentage - Scroll percentage (25, 50, 75, 100)
 */
export function trackScrollDepth(percentage) {
  trackEvent('scroll_depth', {
    scroll_percentage: percentage,
    page_path: window.location.pathname,
  });
}

/**
 * Track button/CTA click
 * @param {string} buttonName - Name of the button
 * @param {string} location - Where the button is located
 * @param {object} metadata - Additional metadata
 */
export function trackButtonClick(buttonName, location, metadata = {}) {
  trackEvent('button_click', {
    button_name: buttonName,
    button_location: location,
    ...metadata,
  });
}

/**
 * Track form interaction
 * @param {string} formName - Name of the form
 * @param {string} action - Action type (start, submit, error, abandon)
 * @param {object} metadata - Additional metadata
 */
export function trackFormInteraction(formName, action, metadata = {}) {
  trackEvent(`form_${action}`, {
    form_name: formName,
    ...metadata,
  });
}

/**
 * Track user engagement time
 * @param {number} seconds - Time spent in seconds
 * @param {string} pagePath - Page path
 */
export function trackEngagementTime(seconds, pagePath) {
  trackEvent('engagement_time', {
    engagement_seconds: seconds,
    page_path: pagePath,
  });
}

/**
 * Setup scroll depth tracking for a page
 */
function setupScrollTracking() {
  const scrollThresholds = [25, 50, 75, 100];
  const trackedThresholds = new Set();

  const checkScroll = () => {
    const scrollPercentage = Math.round(
      ((window.scrollY + window.innerHeight) / document.documentElement.scrollHeight) * 100
    );

    scrollThresholds.forEach((threshold) => {
      if (scrollPercentage >= threshold && !trackedThresholds.has(threshold)) {
        trackedThresholds.add(threshold);
        trackScrollDepth(threshold);
      }
    });
  };

  window.addEventListener('scroll', checkScroll, { passive: true });
}

/**
 * Setup engagement time tracking
 */
function setupEngagementTracking() {
  let startTime = Date.now();
  let isActive = true;

  // Track when user becomes inactive
  const handleVisibilityChange = () => {
    if (document.hidden) {
      isActive = false;
      const engagementTime = Math.round((Date.now() - startTime) / 1000);
      if (engagementTime > 0) {
        trackEngagementTime(engagementTime, window.location.pathname);
      }
    } else {
      isActive = true;
      startTime = Date.now();
    }
  };

  document.addEventListener('visibilitychange', handleVisibilityChange);

  // Track on page unload
  window.addEventListener('beforeunload', () => {
    if (isActive) {
      const engagementTime = Math.round((Date.now() - startTime) / 1000);
      if (engagementTime > 0) {
        trackEngagementTime(engagementTime, window.location.pathname);
      }
    }
  });
}

/**
 * Vue plugin installation
 */
export default {
  install(Vue, options = {}) {
    // Only initialize in production or if explicitly enabled
    const shouldInitialize = IS_PRODUCTION || options.enableInDev;
    
    if (!shouldInitialize) {
      console.log('Google Analytics: Disabled in development mode');
      return;
    }

    // Load gtag script
    loadGtagScript();

    // Setup enhanced tracking
    if (typeof window !== 'undefined') {
      setupScrollTracking();
      setupEngagementTracking();
    }

    // Add helper methods to Vue prototype
    Vue.prototype.$gtag = window.gtag;
    Vue.prototype.$trackPageView = trackPageView;
    Vue.prototype.$trackEvent = trackEvent;
    Vue.prototype.$trackButtonClick = trackButtonClick;
    Vue.prototype.$trackFormInteraction = trackFormInteraction;
    Vue.prototype.$trackScrollDepth = trackScrollDepth;

    // Setup router tracking if router is provided
    if (options.router) {
      options.router.afterEach((to, from) => {
        // Use vue-meta title if available, otherwise use route meta title or path
        const title = document.title || to.meta.title || to.path;
        trackPageView(to.path, title);
      });
    }
  },
};
