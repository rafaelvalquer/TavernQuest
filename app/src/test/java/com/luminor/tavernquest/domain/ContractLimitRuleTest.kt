package com.luminor.tavernquest.domain
import com.luminor.tavernquest.domain.rules.ContractLimitRule;import org.junit.Assert.*;import org.junit.Test
class ContractLimitRuleTest{@Test fun blocksSixth(){assertFalse(ContractLimitRule().canAccept(5));assertTrue(ContractLimitRule().canAccept(4))}}
