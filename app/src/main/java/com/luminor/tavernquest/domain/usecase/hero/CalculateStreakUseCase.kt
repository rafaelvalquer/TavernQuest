package com.luminor.tavernquest.domain.usecase.hero
import javax.inject.Inject
import com.luminor.tavernquest.domain.repository.JournalRepository;import com.luminor.tavernquest.domain.rules.StreakCalculator;import java.time.LocalDate
class CalculateStreakUseCase @Inject constructor(private val journal:JournalRepository,private val calc:StreakCalculator){suspend operator fun invoke()=calc.calculate(journal.getCompletedDates().mapNotNull{runCatching{LocalDate.parse(it)}.getOrNull()}.toSet())}
