package com.luminor.tavernquest.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.luminor.tavernquest.data.local.database.dao.HeroDao
import com.luminor.tavernquest.data.mapper.toDomain
import com.luminor.tavernquest.data.mapper.toEntity
import com.luminor.tavernquest.data.remote.firebase.epochMillis
import com.luminor.tavernquest.domain.model.Hero
import com.luminor.tavernquest.domain.model.HeroAppearance
import com.luminor.tavernquest.domain.model.HeroClass
import com.luminor.tavernquest.domain.repository.HeroRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Firestore is authoritative; HeroDao only keeps a disposable, per-session cache. */
class FirebaseHeroRepository(
    private val dao: HeroDao,
    private val firestore: FirebaseFirestore?,
    private val functions: FirebaseFunctions?,
    private val auth: FirebaseAuth?,
) : HeroRepository {
    override suspend fun create(hero: Hero) {
        val user = auth?.currentUser ?: error("Entre com Google antes de criar seu herói.")
        require(hero.userId == user.uid && hero.id == user.uid) { "Perfil inválido para a conta autenticada." }
        val callable = functions ?: error("Firebase Functions não configurado.")
        await(callable.getHttpsCallable("bootstrapProfile").call(mapOf(
            "name" to hero.name,
            "heroClass" to hero.heroClass.name,
            "appearance" to hero.appearance.name,
        )))
        dao.insert(hero.toEntity())
    }

    override suspend fun get(): Hero? {
        val user = auth?.currentUser ?: return null
        val db = firestore ?: return null
        val stats = await(db.collection("userStats").document(user.uid).get()).getLong("totalXp") ?: 0L
        val hero = await(db.collection("users").document(user.uid).get()).toHeroOrNull(user.uid, stats)
        if (hero != null) dao.insert(hero.toEntity())
        return hero
    }

    override fun observe(): Flow<Hero?> = callbackFlow {
        val user = auth?.currentUser ?: run { dao.deleteAll(); trySend(null); close(); return@callbackFlow }
        val db = firestore ?: run { trySend(null); close(); return@callbackFlow }
        var profile: com.google.firebase.firestore.DocumentSnapshot? = null
        var statsXp = 0L
        fun publish() {
            val hero = profile?.toHeroOrNull(user.uid, statsXp)
            if (hero != null) launch { dao.insert(hero.toEntity()) }
            trySend(hero)
        }
        val profileListener = db.collection("users").document(user.uid).addSnapshotListener { value, error ->
            if (error != null) { trySend(null); return@addSnapshotListener }
            profile = value
            publish()
        }
        val statsListener = db.collection("userStats").document(user.uid).addSnapshotListener { value, _ ->
            statsXp = value?.getLong("totalXp") ?: 0L
            if (profile != null) publish()
        }
        awaitClose { profileListener.remove(); statsListener.remove() }
    }

    override suspend fun addXp(heroId: String, amount: Int) {
        // XP is validated and calculated by Cloud Functions. The local value is cache-only.
        dao.updateXp(heroId, amount)
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toHeroOrNull(uid: String, totalXp: Long): Hero? {
        if (!exists()) return null
        val name = getString("name")?.trim().orEmpty()
        if (name.isBlank()) return null
        return Hero(
            id = uid,
            userId = uid,
            name = name,
            heroClass = getString("heroClass")?.let { runCatching { HeroClass.valueOf(it) }.getOrNull() } ?: HeroClass.WARRIOR,
            appearance = getString("appearance")?.let { runCatching { HeroAppearance.valueOf(it) }.getOrNull() } ?: HeroAppearance.MASCULINE,
            totalXp = totalXp.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt(),
            createdAt = epochMillis("createdAt") ?: 0L,
        )
    }

    private suspend fun <T> await(task: com.google.android.gms.tasks.Task<T>): T = suspendCancellableCoroutine { continuation ->
        task.addOnCompleteListener { result ->
            if (result.isSuccessful) continuation.resume(result.result)
            else continuation.resumeWith(Result.failure(result.exception ?: IllegalStateException("Falha no Firebase.")))
        }
    }
}
