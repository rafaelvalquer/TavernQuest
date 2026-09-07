package com.luminor.tavernquest.feature.onboarding.tavern

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luminor.tavernquest.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TavernChoiceViewModel @Inject constructor(private val settings: SettingsRepository) : ViewModel() {
    private val _completed = MutableStateFlow(false)
    val completed = _completed.asStateFlow()

    fun skip() = viewModelScope.launch {
        settings.setOnboardingCompleted(true)
        _completed.value = true
    }
}
