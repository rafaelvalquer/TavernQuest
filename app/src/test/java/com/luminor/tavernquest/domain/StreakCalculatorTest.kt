package com.luminor.tavernquest.domain
import com.luminor.tavernquest.domain.rules.StreakCalculator;import java.time.LocalDate;import org.junit.Assert.*;import org.junit.Test
class StreakCalculatorTest{@Test fun countsConsecutiveDays(){val d=LocalDate.of(2026,9,6);assertEquals(3,StreakCalculator().calculate(setOf(d,d.minusDays(1),d.minusDays(2)),d))}@Test fun findsLongestHistoricalSequence(){val d=LocalDate.of(2026,9,6);assertEquals(3,StreakCalculator().longest(setOf(d,d.minusDays(1),d.minusDays(2),d.minusDays(5))))}}
