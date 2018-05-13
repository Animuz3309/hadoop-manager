export default class TimeUtils {
  static format(dt) {
    let type = typeof dt;
    let date = dt;
    if (type === "string") {
      date = new Date(dt);
    }
    if (type === "number") {
      date = new Date(dt);
    }
    if (!date) {
      return null;
    }
    return date.toLocaleString();
  }

  static timeDifference(current, previous) {
    const msPerMinute = 60 * 1000;
    const msPerHour = 60 * msPerMinute;
    const msPerDay = 24 * msPerHour;
    const msPerMonth = 30 * msPerDay;
    const msPerYear = 365 * msPerDay;

    let elapsed = current - previous;

    if (elapsed < msPerMinute) {
      return Math.round(elapsed / 1000) + ' seconds ago';
    } else if (elapsed < msPerHour) {
      return Math.round(elapsed / msPerMinute) + ' minutes ago';
    } else if (elapsed < msPerDay) {
      return Math.round(elapsed / msPerHour) + ' hours ago';
    } else if (elapsed < msPerMonth) {
      return Math.round(elapsed / msPerDay) + ' days ago';
    } else if (elapsed < msPerYear) {
      return Math.round(elapsed / msPerMonth) + ' months ago';
    } else if (elapsed >= msPerYear) {
      return Math.round(elapsed / msPerYear) + ' years ago';
    }
  }
}
