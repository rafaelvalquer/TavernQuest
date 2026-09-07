package com.luminor.tavernquest.navigation
import androidx.navigation.NavController
fun NavController.navigateAndClear(route:String){ navigate(route){ popUpTo(graph.id){ inclusive=true } } }
