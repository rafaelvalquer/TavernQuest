package com.luminor.tavernquest.domain.rules
import java.time.LocalDate
class ContractExpirationRule{fun shouldExpire(contractDate:LocalDate,today:LocalDate=LocalDate.now())=contractDate.isBefore(today)}
