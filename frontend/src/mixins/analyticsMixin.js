/**
 * Analytics Mixin
 *
 * Provides reusable analytics tracking methods for Vue components.
 * Import and use in any component to track user interactions.
 */

export default {
  methods: {
    /**
     * Track a button or CTA click
     * @param {string} buttonName - Descriptive name of the button
     * @param {object} metadata - Additional context (optional)
     */
    trackButtonClick(buttonName, metadata = {}) {
      if (this.$trackButtonClick) {
        this.$trackButtonClick(buttonName, this.$route.path, {
          page_title: document.title,
          ...metadata,
        });
      }
    },

    /**
     * Track form start (user begins filling form)
     * @param {string} formName - Name of the form
     */
    trackFormStart(formName) {
      if (this.$trackFormInteraction) {
        this.$trackFormInteraction(formName, 'start', {
          page_path: this.$route.path,
        });
      }
    },

    /**
     * Track form submission
     * @param {string} formName - Name of the form
     * @param {boolean} success - Whether submission was successful
     * @param {object} metadata - Additional context
     */
    trackFormSubmit(formName, success = true, metadata = {}) {
      if (this.$trackFormInteraction) {
        this.$trackFormInteraction(formName, success ? 'submit_success' : 'submit_error', {
          page_path: this.$route.path,
          ...metadata,
        });
      }
    },

    /**
     * Track form error
     * @param {string} formName - Name of the form
     * @param {string} errorType - Type of error encountered
     */
    trackFormError(formName, errorType) {
      if (this.$trackFormInteraction) {
        this.$trackFormInteraction(formName, 'error', {
          error_type: errorType,
          page_path: this.$route.path,
        });
      }
    },

    /**
     * Track form abandonment (user leaves without completing)
     * @param {string} formName - Name of the form
     * @param {string} lastField - Last field user interacted with
     */
    trackFormAbandon(formName, lastField = '') {
      if (this.$trackFormInteraction) {
        this.$trackFormInteraction(formName, 'abandon', {
          last_field: lastField,
          page_path: this.$route.path,
        });
      }
    },

    /**
     * Track a general user action
     * @param {string} actionName - Name of the action
     * @param {object} metadata - Additional context
     */
    trackUserAction(actionName, metadata = {}) {
      if (this.$trackEvent) {
        this.$trackEvent('user_action', {
          action_name: actionName,
          page_path: this.$route.path,
          page_title: document.title,
          ...metadata,
        });
      }
    },

    /**
     * Track portal/role selection
     * @param {string} portalType - Type of portal selected (farmer, buyer, etc.)
     * @param {string} context - Where the selection happened
     */
    trackPortalSelection(portalType, context = 'signup') {
      if (this.$trackEvent) {
        this.$trackEvent('portal_selected', {
          portal_type: portalType,
          selection_context: context,
          page_path: this.$route.path,
        });
      }
    },

    /**
     * Track navigation/link clicks
     * @param {string} linkName - Name/description of the link
     * @param {string} destination - Where the link goes
     */
    trackNavigation(linkName, destination) {
      if (this.$trackEvent) {
        this.$trackEvent('navigation_click', {
          link_name: linkName,
          destination,
          from_page: this.$route.path,
        });
      }
    },

    /**
     * Track dialog/modal interactions
     * @param {string} dialogName - Name of the dialog
     * @param {string} action - Action taken (open, close, submit)
     */
    trackDialogInteraction(dialogName, action) {
      if (this.$trackEvent) {
        this.$trackEvent('dialog_interaction', {
          dialog_name: dialogName,
          dialog_action: action,
          page_path: this.$route.path,
        });
      }
    },
  },
};
