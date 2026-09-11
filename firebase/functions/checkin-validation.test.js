const { test } = require('node:test');
const assert = require('node:assert/strict');
const { validCheckInTiming } = require('./checkin-validation');

const now = 1_800_000_000_000;
const valid = { startedAt: now - 60_000, completedAt: now, durationSeconds: 60 };

test('accepts measured activity and an older offline upload', () => {
  assert.equal(validCheckInTiming(valid, now), true);
  assert.equal(validCheckInTiming(valid, now + 30 * 86400_000), true);
  assert.equal(validCheckInTiming({ ...valid, startedAt: null, durationSeconds: 0 }, now), true);
});

test('rejects nonnumeric, nonfinite and fractional timing fields', () => {
  for (const field of ['startedAt', 'completedAt', 'durationSeconds']) {
    for (const value of ['60', NaN, Infinity, -Infinity, 1.5, {}, []]) {
      assert.equal(validCheckInTiming({ ...valid, [field]: value }, now), false, `${field}: ${value}`);
    }
  }
});

test('rejects reversed, excessive and inconsistent durations', () => {
  assert.equal(validCheckInTiming({ ...valid, startedAt: now + 1 }, now), false);
  assert.equal(validCheckInTiming({ ...valid, startedAt: now - 86400_001, durationSeconds: 86400 }, now), false);
  assert.equal(validCheckInTiming({ ...valid, durationSeconds: 600 }, now), false);
  assert.equal(validCheckInTiming({ ...valid, durationSeconds: -1 }, now), false);
});

test('allows bounded clock skew but rejects future activity', () => {
  assert.equal(validCheckInTiming(valid, now - 300_000), true);
  assert.equal(validCheckInTiming(valid, now - 300_001), false);
});
