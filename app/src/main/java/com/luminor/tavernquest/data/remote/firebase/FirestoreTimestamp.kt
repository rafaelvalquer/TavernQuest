package com.luminor.tavernquest.data.remote.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

/** Converts server Timestamp and legacy epoch-millis fields to the domain format. */
fun DocumentSnapshot.epochMillis(field: String): Long? = when (val value = get(field)) {
    is Timestamp -> value.toDate().time
    is Date -> value.time
    is Number -> value.toLong()
    else -> null
}
