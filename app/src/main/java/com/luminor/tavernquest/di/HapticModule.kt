package com.luminor.tavernquest.di
import com.luminor.tavernquest.core.haptics.*;import dagger.Module;import dagger.Provides;import dagger.hilt.InstallIn;import dagger.hilt.components.SingletonComponent
@Module @InstallIn(SingletonComponent::class) object HapticModule{@Provides fun haptic():HapticManager=AndroidHapticManager()}
