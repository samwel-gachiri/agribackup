import moment from 'moment';

export const formatDate = (date) => {
  if (!date) return null;

  const [year, month, day] = date.split('-');
  return `${day}/${month}/${year}`;
};

export const formatToHumanWithTime = (date) => {
  if (!date || date === '~') return '~';
  return moment(date).format('DD/MM/YYYY hh:mma');
};
export const formatDate2 = (date) => {
  if (!date) return null;

  const [year, month, day] = date.split('-');
  return `${year}-${month}-${day}`;
};

export const parseDate = (date) => {
  if (!date) return null;

  const [month, day, year] = date.split('/');
  return `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`;
};

export const changeFormat = (date) => {
  if (date !== null) {
    const d = date.split('-');
    return `${d[2]}/${d[1]}/${d[0]}`;
  }
  return '';
};

export const reverseFormat = (date) => {
  if (date !== null) {
    const d = date.split('/');
    return `${d[2]}-${d[1]}-${d[0]}`;
  }
  return '';
};

export const addMonths = (m) => moment().add(m, 'months').format('Do MMMM YYYY');

export const getMonth = (m) => moment().add(m, 'months').format('MMM');

export const diffDate = (date) => {
  const now = moment(new Date());
  return moment(date, 'YYYY-MM-DD').isBefore(now);
};

export const formatDateToMonth = (date) => moment(date).format('MMM');

export const diffDate2 = (date) => {
  const now = moment(new Date());
  return moment(date, 'Do MMM YYYY').isBefore(now);
};

export const timeNow = (i = null) => {
  if (moment(new Date(i)).isAfter()) {
    return '08:00';
  }
  const d = new Date();
  const h = (d.getHours() < 10 ? '0' : '') + d.getHours();
  const m = (d.getMinutes() < 10 ? '0' : '') + d.getMinutes();
  // eslint-disable-next-line no-param-reassign
  return `${h}:${m}`;
};
