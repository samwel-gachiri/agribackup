const AuthMixins = {
  methods: {
    async signInUser(phoneNumber, password) {
      const formData = {
        username: phoneNumber,
        password,
      };

      try {
        return await this.$store.dispatch('auth/signIn', formData); // Return the user if sign-in is successful
      } catch (err) {
        this.$toast.error(err.message);
        return null; // Return null or handle error case as needed
      }
    },

  },
};

export default AuthMixins;
