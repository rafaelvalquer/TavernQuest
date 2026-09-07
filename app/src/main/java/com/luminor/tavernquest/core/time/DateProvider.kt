package com.luminor.tavernquest.core.time
import java.time.LocalDate
interface DateProvider{fun today():LocalDate; fun nowMillis():Long}
