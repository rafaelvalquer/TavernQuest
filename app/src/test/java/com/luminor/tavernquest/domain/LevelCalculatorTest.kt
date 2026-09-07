package com.luminor.tavernquest.domain
import com.luminor.tavernquest.domain.rules.LevelCalculator;import org.junit.Assert.*;import org.junit.Test
class LevelCalculatorTest{@Test fun levelOneAtZero(){val p=LevelCalculator().calculate(0);assertEquals(1,p.level);assertEquals(0,p.xpInLevel)};@Test fun advancesLevel(){assertEquals(2,LevelCalculator().calculate(100).level)}}
