package com.luminor.tavernquest.feature.splash
import androidx.lifecycle.ViewModel;import androidx.lifecycle.viewModelScope;import com.luminor.tavernquest.domain.repository.SettingsRepository;import dagger.hilt.android.lifecycle.HiltViewModel;import javax.inject.Inject;import kotlinx.coroutines.flow.*
@HiltViewModel class SplashViewModel @Inject constructor(settings:SettingsRepository):ViewModel(){val completed=settings.observe().map{it.onboardingCompleted}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),null)}
