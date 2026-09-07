package com.luminor.tavernquest.di
import com.luminor.tavernquest.core.time.*;import com.luminor.tavernquest.core.util.*;import com.luminor.tavernquest.domain.rules.*;import dagger.Module;import dagger.Provides;import dagger.hilt.InstallIn;import dagger.hilt.components.SingletonComponent
@Module @InstallIn(SingletonComponent::class) object TimeModule{@Provides fun time():DateProvider=SystemDateProvider();@Provides fun ids():UuidProvider=DefaultUuidProvider();@Provides fun level()=LevelCalculator();@Provides fun streak()=StreakCalculator()}
