package com.luminor.tavernquest.core.time
object GreetingResolver{fun resolve(hour:Int)=when(hour){in 5..11->"Bom dia";in 12..17->"Boa tarde";else->"Boa noite"}}
