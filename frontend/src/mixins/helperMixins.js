import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime.js';

// Extend dayjs with relativeTime
dayjs.extend(relativeTime);

const helperMixins = {
  methods: {
    is_null: (value) => value == null,

    scanObject(nestedObj, pathArr, defaultValue = null) {
      if (!this.isArray(pathArr)) {
        // eslint-disable-next-line no-param-reassign
        pathArr = pathArr.split('.');
      }

      const retrievedValue = pathArr.reduce(
        (obj, key) => (obj && obj[key] !== 'undefined' ? obj[key] : undefined),
        nestedObj,
      );
      return !this.is_null(retrievedValue) ? retrievedValue : defaultValue;
    },

    isArray(value) {
      return Array.isArray(value);
    },
  },
  computed: {
    vuetifyMoneyOptions() {
      return {
        locale: 'en-US',
        prefix: 'Ksh. ',
        suffix: '',
        length: 11,
        precision: 2,
      };
    },
  },
  filters: {
    formatDate(date) {
      return dayjs(date).fromNow(); // Converts raw date to "x time ago"
    },
  },
};

export default helperMixins;
