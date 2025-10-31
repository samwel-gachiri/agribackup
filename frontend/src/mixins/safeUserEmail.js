/* eslint-disable */
export default {
    computed: {
        $safeUserEmail() {
            try {
                const user = this.$store?.state?.auth?.user;
                return user && user.email ? user.email : '';
            } catch (e) { return ''; }
        },
    },
};
