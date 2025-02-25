package com.example.scan.view.access

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor
import androidx.biometric.BiometricPrompt

fun FingertipsDeteccion(
    context: Context,
    activity: FragmentActivity,
    executor: Executor,
    response: () -> Unit
) {

    val biometricPrompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(context, "✅ Huella reconocida", Toast.LENGTH_SHORT).show()
                response()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(context, "❌ Error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(context, "⚠ Huella no reconocida", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Autenticación Biométrica")
        .setSubtitle("Coloca tu dedo en el sensor para continuar")
        .setNegativeButtonText("Cancelar")
        .build()

    biometricPrompt.authenticate(promptInfo)
}
