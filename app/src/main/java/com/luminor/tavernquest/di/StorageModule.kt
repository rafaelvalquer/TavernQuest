package com.luminor.tavernquest.di
import android.content.Context;import com.luminor.tavernquest.data.local.preferences.SettingsDataSource;import dagger.Module;import dagger.Provides;import dagger.hilt.InstallIn;import dagger.hilt.android.qualifiers.ApplicationContext;import dagger.hilt.components.SingletonComponent
@Module @InstallIn(SingletonComponent::class) object StorageModule{@Provides fun settings(@ApplicationContext c:Context)=SettingsDataSource(c)}
