package com.luminor.tavernquest.core.time
import java.time.LocalDate; import java.time.format.DateTimeFormatter
object DateFormatter{fun display(iso:String)=runCatching{LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}.getOrDefault(iso)}
