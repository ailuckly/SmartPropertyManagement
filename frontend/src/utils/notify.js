export const notify = (message, type = 'info', duration = 3000) => {
  window.dispatchEvent(new CustomEvent('app:notify', { detail: { message, type, duration } }));
};
