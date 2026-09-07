package com.luminor.tavernquest.core.util

object TavernInviteCode {
    private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    fun fromId(id: String): String {
        var value = id.fold(7L) { acc, char -> (acc * 31 + char.code) and Long.MAX_VALUE }
        return buildString(6) { repeat(6) { append(ALPHABET[(value % ALPHABET.length).toInt()]); value /= ALPHABET.length } }
    }
}
