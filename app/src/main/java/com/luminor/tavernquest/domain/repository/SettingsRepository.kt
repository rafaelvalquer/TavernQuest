package com.luminor.tavernquest.domain.repository
import kotlinx.coroutines.flow.Flow
data class GameSettings(val onboardingCompleted:Boolean=false,val soundEnabled:Boolean=true,val hapticsEnabled:Boolean=true,val lastSelectedTab:String="tavern")
interface SettingsRepository{fun observe():Flow<GameSettings>;suspend fun setOnboardingCompleted(v:Boolean);suspend fun setSound(v:Boolean);suspend fun setHaptics(v:Boolean);suspend fun reset()}
