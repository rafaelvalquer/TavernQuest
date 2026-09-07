package com.luminor.tavernquest.domain
import com.luminor.tavernquest.data.seed.ContractSeed;import com.luminor.tavernquest.domain.rules.DailyBoardGenerator;import org.junit.Assert.*;import org.junit.Test
class DailyBoardGeneratorTest{@Test fun generatesTwenty(){assertEquals(20,DailyBoardGenerator().generate(ContractSeed.all,"2026-09-06").size)}}
