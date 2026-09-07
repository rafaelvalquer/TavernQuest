package com.luminor.tavernquest.domain.rules
import java.time.LocalDate
class StreakCalculator { fun calculate(completedDates:Set<LocalDate>,today:LocalDate=LocalDate.now()):Int { if(completedDates.isEmpty()) return 0; var cursor=if(today in completedDates) today else today.minusDays(1); var n=0; while(cursor in completedDates){n++;cursor=cursor.minusDays(1)}; return n } }
