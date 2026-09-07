package com.luminor.tavernquest.data.repository

import com.luminor.tavernquest.data.local.preferences.SettingsDataSource
import com.luminor.tavernquest.domain.repository.SettingsRepository

class SettingsRepositoryImpl(private val ds: SettingsDataSource) : SettingsRepository {
    override fun observe() = ds.settings

    override suspend fun setOnboardingCompleted(v: Boolean) {
        ds.onboarding(v)
    }

    override suspend fun setSound(v: Boolean) {
        ds.sound(v)
    }

    override suspend fun setHaptics(v: Boolean) {
        ds.haptics(v)
    }

    override suspend fun reset() {
        ds.reset()
    }
}
