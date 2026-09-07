package com.luminor.tavernquest.di
import android.content.Context;import com.luminor.tavernquest.core.media.*;import dagger.Module;import dagger.Provides;import dagger.hilt.InstallIn;import dagger.hilt.android.qualifiers.ApplicationContext;import dagger.hilt.components.SingletonComponent
@Module @InstallIn(SingletonComponent::class) object MediaModule{@Provides fun photo(@ApplicationContext c:Context):PhotoManager=ImageFileManager(c)}
