<template>
  <div>
    <div id="google-signin-button" class="tw-border"></div>
    <div v-if="user">
      <h2>Welcome, {{ user.userProfile.fullName }}!</h2>
      <img :src="user.picture" alt="Profile Picture" />
      <p>Email: {{ user.email }}</p>
      <button @click="signOut">Sign Out</button>
    </div>
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
    window.google.accounts.id.initialize({
      client_id: 'YOUR_GOOGLE_CLIENT_ID', // Replace with your Client ID
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
        this.user = {
          name: userInfo.name,
          email: userInfo.email,
          picture: userInfo.picture,
        };
        this.$toast.success('User signed in:', userInfo);
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
