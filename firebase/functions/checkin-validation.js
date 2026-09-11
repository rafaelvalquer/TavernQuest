const DAY_MS = 24 * 60 * 60 * 1000;
const CLOCK_TOLERANCE_MS = 5 * 60 * 1000;

function validCheckInTiming(checkIn, now = Date.now()) {
  const { completedAt, durationSeconds } = checkIn;
  const startedAt = checkIn.startedAt ?? completedAt;
  return Number.isSafeInteger(completedAt) && completedAt > 0
    && Number.isSafeInteger(startedAt) && startedAt > 0
    && Number.isSafeInteger(durationSeconds) && durationSeconds >= 0
    && completedAt <= now + CLOCK_TOLERANCE_MS
    && completedAt >= startedAt && completedAt - startedAt <= DAY_MS
    && Math.abs(durationSeconds - Math.floor((completedAt - startedAt) / 1000)) <= 2;
}

module.exports = { validCheckInTiming };
