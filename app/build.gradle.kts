plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

import java.util.Properties

val releaseProperties = Properties().apply {
    val source = file("release.properties")
    if (source.isFile) source.inputStream().use(::load)
}
fun releaseProperty(name: String): String? = providers.gradleProperty(name).orNull
    ?: System.getenv("TAVERNQUEST_${name.uppercase()}")
    ?: releaseProperties.getProperty(name)
val releaseStoreFile = releaseProperty("storeFile")
val releaseStorePassword = releaseProperty("storePassword")
val releaseKeyAlias = releaseProperty("keyAlias")
val releaseKeyPassword = releaseProperty("keyPassword")
val useFirebaseEmulators = providers.gradleProperty("useFirebaseEmulators").orNull == "true"

// Keep offline/local development working until the Firebase project is created.
if (file("google-services.json").exists()) apply(plugin = "com.google.gms.google-services")
if (file("google-services.json").exists()) apply(plugin = "com.google.firebase.crashlytics")

// A production artifact without Firebase would silently disable login and sync.
// Debug remains optional for local UI work, while every release task fails early.
tasks.configureEach {
    if (name.contains("Release", ignoreCase = true)) {
        doFirst {
            check(file("google-services.json").isFile) {
                "app/google-services.json é obrigatório para gerar uma versão release."
            }
            check(listOf(releaseStoreFile, releaseStorePassword, releaseKeyAlias, releaseKeyPassword).all { !it.isNullOrBlank() }) {
                "Configure a assinatura release em app/release.properties ou nas variáveis TAVERNQUEST_STORE_FILE, TAVERNQUEST_STORE_PASSWORD, TAVERNQUEST_KEY_ALIAS e TAVERNQUEST_KEY_PASSWORD."
            }
        }
    }
}

android {
    namespace = "com.luminor.tavernquest"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.luminor.tavernquest"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "com.luminor.tavernquest.HiltTestRunner"
    }
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("boolean", "USE_FIREBASE_EMULATORS", useFirebaseEmulators.toString())
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("boolean", "USE_FIREBASE_EMULATORS", "false")
        }
    }
    signingConfigs {
        if (listOf(releaseStoreFile, releaseStorePassword, releaseKeyAlias, releaseKeyPassword).all { !it.isNullOrBlank() }) {
            create("release") {
                storeFile = file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }
    buildTypes {
        getByName("release") { signingConfig = signingConfigs.findByName("release") }
    }
    buildFeatures { compose = true; buildConfig = true }
    sourceSets.getByName("androidTest").assets.srcDir("$projectDir/schemas")
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.datastore.preferences)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    implementation(libs.work.runtime.ktx)
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.work.compiler)
    implementation(libs.coroutines.android)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.appcheck.debug)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.room.testing)
    debugImplementation(libs.compose.ui.test.manifest)
}
