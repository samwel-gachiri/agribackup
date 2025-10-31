<template>
  <div>
    <v-app
        id="notification-modal"
        class="tw-modal tw-opacity-0 tw-pointer-events-none tw-fixed tw-w-full tw-h-full tw-overflow-hidden tw-bottom-0 tw-top-0 tw-left-0 tw-flex tw-items-start tw-justify-start tw-z-40"
    >
      <div
          class="modal-overlay-1 tw-absolute tw-w-full tw-h-full modal-bg tw-z-30 tw-overflow-hidden"
      />

      <section class="tw-w-full md:tw-px-56 tw-px-4 tw-mt-10 tw-z-40 tw-flex tw-flex-col tw-items-start tw-container tw-mx-auto">
        <v-card class="tw-bg-black-c tw-p-8 tw-justify-center tw-items-center">
          <p class="title-font tw-text-xl c-green-text">
            APA want to show notifications!
          </p>
          <p class="tw-text-sm tw-mt-3">
            Allow Notifications to get <br>regular notifications
          </p>
          <div class="tw-flex tw-flex-wrap tw-w-full tw-justify-between tw-items-center tw-mt-6">
            <v-btn color="success" class="tw-text-black tw-px-6" @click="promptNotifications">
              Allow
            </v-btn>
            <v-btn color="error" class="tw-text-black tw-px-6" @click="rejectPrompt">
              Block
            </v-btn>
          </div>
        </v-card>
      </section>
<!--      <new-policy-prompt :dialog="dialog" :policy="policy" />-->
    </v-app>
  </div>
</template>

<script>
/* eslint-disable no-unused-vars */

// import firebase from '@/services/firebase/firebase.js';
import axios from 'axios';
import { mapGetters } from 'vuex';
import { FB_PUSH_NOTIFICATION, FB_PUSH_NOTIFICATION_PAY, NOTIFICATIONS } from '@/utils/const.js';
// import NewPolicyPrompt from '@/components/shared/NewPolicyPrompt.js';

export default {
  name: 'NotificationPrompt',
  // components: { NewPolicyPrompt },
  computed: {
    ...mapGetters('auth', ['authenticatedUser']),
  },
  data() {
    return {
      policy: {},
      dialog: false,
    };
  },
  // mounted() {
  //   if (firebase.messaging.isSupported()) {
  //     const messaging = firebase.messaging();
  //     messaging.usePublicVapidKey(process.env.VUE_APP_FIREBASE_PUBLIC_KEY);
  //
  //     if (Notification.permission === 'granted') {
  //       messaging.getToken().then((token) => {
  //         this.sendTokenToServer(token);
  //       });
  //     } else if (Notification.permission === 'blocked' || Notification.permission === 'denied') {
  //       /* the user has previously denied push. Can't reprompt. */
  //     } else if (Notification.permission === 'default' && !this.isTokenSentToServer()) {
  //       this.toggleModal();
  //     }
  //     // Callback fired if Instance ID token is updated.
  //     messaging.onTokenRefresh(() => {
  //       messaging.getToken()
  //         .then((refreshedToken) => {
  //           // to the app server.
  //           this.setTokenSentToServer(false);
  //           // Send Instance ID token to app server.
  //           this.sendTokenToServer(refreshedToken);
  //         })
  //         .catch((err) => {
  //           this.setTokenSentToServer(false);
  //         });
  //     });
  //
  //     // eslint-disable-next-line arrow-body-style
  //     messaging.onMessage((payload) => {
  //       let title = '';
  //       let body = '';
  //       this.$analytics.logEvent(FB_PUSH_NOTIFICATION);
  //       if ('data' in payload) {
  //         try {
  //           title = payload.data.title;
  //           const data = JSON.parse(payload.data.payload);
  //           if (data.notification_type === 'quoteSuccessPayment') {
  //             this.policy = data.notification_body;
  //             this.dialog = true;
  //             body = `Your Payment for quote No. ${data.notification_body.quote_id} is successful, Your Policy No is ${data.notification_body.policy_number}`;
  //             this.$root.$emit('successful-payment');
  //             this.$analytics.logEvent(FB_PUSH_NOTIFICATION_PAY);
  //           } else if (data.notification_type === 'RESUME_QUOTE') {
  //             body = data.notification_body.title;
  //           } else {
  //             body = data.notification_body;
  //           }
  //         } catch (e) {
  //           body = payload.data.payload;
  //           this.$toast.info(body, 'Info');
  //           if (body.includes('Your transaction for quote')) {
  //             this.$root.$emit('failed-payment');
  //           }
  //         }
  //       } else {
  //         body = payload.notification.body;
  //         title = payload.notification.title || 'APA Insurance';
  //       }
  //       return new Notification(title, { body, icon: `${window.location.origin}/logo.svg` });
  //     });
  //   }
  //
  //   // request  notification from the MakePayment.vue
  //   this.$root.$on('request-notification', () => {
  //     this.promptNotifications(false);
  //   });
  //
  //   // retrieve notifications count
  //   this.$store.dispatch('getNotificationCount');
  // },
  methods: {
    toggleModal() {
      const body = document.querySelector('body');
      const modals = document.querySelectorAll('#notification-modal');
      // eslint-disable-next-line no-plusplus
      modals.forEach((modal) => {
        modal.classList.toggle('tw-opacity-0');
        modal.classList.toggle('tw-pointer-events-none');
      });

      body.classList.toggle('tw-modal-active');
    },
    isTokenSentToServer() {
      return localStorage.getItem(NOTIFICATIONS) === '1';
    },
    setTokenSentToServer(sent) {
      localStorage.setItem(NOTIFICATIONS, sent ? '1' : '0');
    },
    setTokenToTopic(token) {
      // Defensive guards: authenticatedUser may be null during early app bootstrap
      if (!this.authenticatedUser || !this.authenticatedUser.email) {
        // Silently abort; caller (FCM setup) will retry later when user is available
        return;
      }
      const data = {
        token,
        kraPin: this.authenticatedUser['custom:kra-pin'],
        appVersionCode: 263,
        deviceId: 'web',
        userName: this.authenticatedUser?.email || '',
      };
      axios.post('/customer/notification/fcm/updateUserDeviceFcmToken', data)
        .then((_response) => {
          this.setTokenSentToServer(true);
          this.$toast.success('You will receive regular notifications from APA');
        });
    },
    sendTokenToServer(currentToken) {
      if (!this.isTokenSentToServer()) {
        this.setTokenToTopic(currentToken);
      }
    },
    // promptNotifications(t = true) {
    //   const messaging = firebase.messaging();
    //   // messaging.usePublicVapidKey(process.env.FIREBASE_PUBLIC_KEY)
    //
    //   if (!('Notification' in window)) {
    //     this.toggleModal();
    //     this.setTokenSentToServer(true);
    //   } else {
    //     Notification.requestPermission().then((permission) => {
    //       if (t) this.toggleModal();
    //       // Get Token
    //       if (permission === 'granted') {
    //         messaging.getToken().then((token) => {
    //           this.sendTokenToServer(token);
    //         });
    //       } else if (permission === 'denied') {
    //         this.$toast.error('Notifications from APA won\'t be sent to you');
    //       }
    //     });
    //   }
    // },
    rejectPrompt() {
      this.toggleModal();
      this.setTokenSentToServer(true);
      this.$toast.error('Notifications from APA won\'t be sent to you');
    },
  },
};
</script>

<style>
.modal-bg {
  background-color: rgba(25, 29, 29, .6);
  -webkit-backdrop-filter: blur(50px);
  backdrop-filter: blur(50px);
}
</style>
