package com.luminor.tavernquest.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luminor.tavernquest.data.local.database.TavernQuestDatabase
import com.luminor.tavernquest.domain.repository.AuthRepository
import com.luminor.tavernquest.domain.repository.SettingsRepository
import com.luminor.tavernquest.domain.usecase.settings.ResetGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel class SettingsViewModel @Inject constructor(private val repo: SettingsRepository, private val auth: AuthRepository, private val db: TavernQuestDatabase, private val reset: ResetGameUseCase): ViewModel() {
 val ui=repo.observe().map{SettingsUiState(it.soundEnabled,it.hapticsEnabled)}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),SettingsUiState())
 fun sound(v:Boolean)=viewModelScope.launch{repo.setSound(v)}
 fun haptics(v:Boolean)=viewModelScope.launch{repo.setHaptics(v)}
 fun reset(onComplete:()->Unit)=viewModelScope.launch{reset();onComplete()}
 fun requestLogout(onSafe:()->Unit,onPending:(Int)->Unit)=viewModelScope.launch{val pending=db.activityDao().countPending();if(pending==0)onSafe()else onPending(pending)}
 fun logoutDiscardingPending(onComplete:()->Unit)=viewModelScope.launch{db.clearAllTables();auth.logout();onComplete()}
}