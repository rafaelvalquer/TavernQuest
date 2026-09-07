package com.luminor.tavernquest.data
import com.luminor.tavernquest.data.seed.ContractSeed;import org.junit.Assert.*;import org.junit.Test
class ContractSeedTest{@Test fun hasFiftyTemplates(){assertEquals(50,ContractSeed.all.size)}}
