package com.luminor.tavernquest.feature.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthDiagnosticsTest {
    @Test fun `report omits SDK message and nested causes`() {
        val source = IllegalStateException("user@example.com token=secret", Exception("credential=private"))
        val report = sanitizedAuthFailure(source, LoginFailureStage.ACCOUNT_SETUP)
        assertNull(report.cause)
        assertFalse(report.toString().contains("secret"))
        assertFalse(report.toString().contains("user@example.com"))
        assertTrue(report.message!!.contains("ACCOUNT_SETUP:IllegalStateException:UNCLASSIFIED"))
        assertTrue(report.stackTrace.contentEquals(source.stackTrace))
    }
}
