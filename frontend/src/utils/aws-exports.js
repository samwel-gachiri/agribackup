const config = {
  FarmerAuth: {
    // identityPoolId: process.env.VUE_APP_IDENTITY_POOL_ID,
    region: process.env.VUE_APP_AWS_REGION,
    userPoolId: process.env.VUE_APP_USER_POOL_ID_FARMER,
    userPoolWebClientId: process.env.VUE_APP_USER_POOL_WEB_CLIENT_ID_FARMER,
    mandatorySignIn: false,
  },
  BuyerAuth: {
    // identityPoolId: process.env.VUE_APP_IDENTITY_POOL_ID,
    region: process.env.VUE_APP_AWS_REGION,
    userPoolId: process.env.VUE_APP_USER_POOL_ID_BUYER,
    userPoolWebClientId: process.env.VUE_APP_USER_POOL_WEB_CLIENT_ID_BUYER,
    mandatorySignIn: false,
  },
  AdminAuth: {
    // identityPoolId: process.env.VUE_APP_IDENTITY_POOL_ID,
    region: process.env.VUE_APP_AWS_REGION,
    userPoolId: process.env.VUE_APP_USER_POOL_ID_ADMIN,
    userPoolWebClientId: process.env.VUE_APP_USER_POOL_WEB_CLIENT_ID_ADMIN,
    mandatorySignIn: false,
  },
};
export default config;
