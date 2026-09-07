package com.luminor.tavernquest.data.local.preferences
import android.content.Context;import androidx.datastore.preferences.preferencesDataStore
val Context.tavernDataStore by preferencesDataStore("tavern_settings")
