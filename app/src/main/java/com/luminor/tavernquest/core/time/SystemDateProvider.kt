package com.luminor.tavernquest.core.time
import java.time.LocalDate
class SystemDateProvider:DateProvider{override fun today()=LocalDate.now();override fun nowMillis()=System.currentTimeMillis()}
