module.exports = {
  prefix: 'tw-',
  content: [
    './src/**/*.{vue,js,ts,jsx,tsx}',
    './public/index.html',
  ],
  theme: {
    extend: {
      screens: {
        '2xl': '1536px',
      },
      colors: {
        'agri-green': '#3d5a2a',
        'agri-brown': '#6b4f2c',
        'agri-light': '#f7f5ee',
        'agri-yellow': '#f7e9b0',
        'agri-border': '#e6e1c3',
        'agri-accent': '#b6a16b',
        'agri-softgreen': '#b6e4a3',
      },
    },
  },
  plugins: [],
};
