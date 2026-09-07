package com.luminor.tavernquest.domain.rules

import java.time.LocalDate

class StreakCalculator {
    fun calculate(completedDates: Set<LocalDate>, today: LocalDate = LocalDate.now()): Int {
        if (completedDates.isEmpty()) return 0
        var cursor = if (today in completedDates) today else today.minusDays(1)
        var count = 0
        while (cursor in completedDates) { count++; cursor = cursor.minusDays(1) }
        return count
    }

    fun longest(completedDates: Set<LocalDate>): Int {
        var best = 0
        var run = 0
        var previous: LocalDate? = null
        for (date in completedDates.sorted()) {
            run = if (previous != null && date == previous!!.plusDays(1)) run + 1 else 1
            if (run > best) best = run
            previous = date
        }
        return best
    }
}
