package com.luminor.tavernquest.domain.rules
import com.luminor.tavernquest.domain.model.*
import kotlin.random.Random
class DailyBoardGenerator { fun generate(templates:List<ContractTemplate>,date:String):List<ContractTemplate>{ val r=Random(date.hashCode()); return ContractCategory.entries.flatMap{cat->templates.filter{it.enabled&&it.category==cat}.shuffled(r).take(4)} } }
