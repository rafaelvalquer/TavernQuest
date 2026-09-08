package com.luminor.tavernquest.app
import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.luminor.tavernquest.data.sync.PendingCheckInWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class TavernQuestApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        val request = OneTimeWorkRequestBuilder<PendingCheckInWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(this).enqueueUniqueWork("pending-checkins", ExistingWorkPolicy.KEEP, request)
        val periodic = PeriodicWorkRequestBuilder<PendingCheckInWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("pending-checkins-periodic", ExistingPeriodicWorkPolicy.KEEP, periodic)
        runCatching {
            FirebaseAuth.getInstance().addAuthStateListener { auth ->
                val user = auth.currentUser ?: return@addAuthStateListener
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                    FirebaseFirestore.getInstance().collection("users").document(user.uid)
                        .set(mapOf("fcmTokens" to FieldValue.arrayUnion(token)), SetOptions.merge())
                }
            }
        }
    }
}
