package com.luminor.tavernquest.feature.auth

import android.os.Build
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.luminor.tavernquest.BuildConfig
import kotlinx.coroutines.CancellationException

internal fun sanitizedAuthFailure(error: Throwable, stage: LoginFailureStage): Exception {
    val code = when (error) {
        is FirebaseAuthException -> error.errorCode
        is FirebaseFunctionsException -> error.code.name
        is FirebaseFirestoreException -> error.code.name
        else -> "UNCLASSIFIED"
    }
    // Never attach the original cause: SDK messages may contain credentials or account details.
    return Exception("${stage.name}:${error.javaClass.simpleName}:$code").apply {
        stackTrace = error.stackTrace
    }
}

internal fun reportAuthFailure(error: Throwable, stage: LoginFailureStage) {
    if (error is CancellationException) throw error
    val sanitized = sanitizedAuthFailure(error, stage)
    runCatching {
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("auth_stage", stage.name)
            setCustomKey("credential_exception_type", error.javaClass.name)
            setCustomKey("firebase_error_code", sanitized.message!!.substringAfterLast(':'))
            setCustomKey("version_code", BuildConfig.VERSION_CODE)
            setCustomKey("version_name", BuildConfig.VERSION_NAME)
            setCustomKey("build_type", BuildConfig.BUILD_TYPE)
            setCustomKey("distribution_channel", if (BuildConfig.DEBUG) "debug" else "direct_apk")
            setCustomKey("device_api", Build.VERSION.SDK_INT)
            // Enforcement is server configuration; do not invent its state from the build type.
            setCustomKey("app_check_status", "unknown")
            recordException(sanitized)
        }
    }
}
