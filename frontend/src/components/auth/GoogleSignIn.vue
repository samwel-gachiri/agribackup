<template>
  <div>
    <div id="google-signin-button" class="tw-border"></div>
  </div>
</template>

<script>
export default {
  name: 'Google-Sign-In',
  data() {
    return {
      user: null,
    };
  },
  mounted() {
    const clientId = process.env.VUE_APP_GOOGLE_CLIENT_ID;
    if (!clientId) {
      console.error('Google Client ID is missing. Please set VUE_APP_GOOGLE_CLIENT_ID in your .env file.');
    }
    window.google.accounts.id.initialize({
      client_id: clientId,
      callback: this.handleCredentialResponse, // Callback function
    });

    window.google.accounts.id.renderButton(
      document.getElementById('google-signin-button'),
      { theme: 'outline', size: 'large' }, // Customize button
    );

    window.google.accounts.id.prompt(); // Display the One Tap prompt
  },
  methods: {
    handleCredentialResponse(response) {
      if (response.credential) {
        // Decode the JWT token to get user info
        const userInfo = this.decodeJWT(response.credential);
        this.$emit('google-login-success', {
          fullName: userInfo.name,
          email: userInfo.email,
          picture: userInfo.picture,
          credential: response.credential,
        });
        this.$toast.success('Google Sign-In successful');
      }
    },
    decodeJWT(token) {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => `%${(`00${c.charCodeAt(0).toString(16)}`).slice(-2)}`)
          .join(''),
      );
      return JSON.parse(jsonPayload);
    },
    signOut() {
      this.user = null;
      window.google.accounts.id.disableAutoSelect();
      this.$toast.show('User signed out');
    },
  },
};
</script>

<style scoped>
#google-signin-button {
  margin: 20px 0;
}
</style>
