package com.luminor.tavernquest.domain
import com.luminor.tavernquest.domain.rules.ContractExpirationRule;import java.time.LocalDate;import org.junit.Assert.*;import org.junit.Test
class ContractExpirationRuleTest{@Test fun expiresPast(){assertTrue(ContractExpirationRule().shouldExpire(LocalDate.of(2026,9,5),LocalDate.of(2026,9,6)))}}
