package id.yuana.mlkit.demo.ui.components

import androidx.camera.core.SurfaceRequest
import com.google.mlkit.nl.translate.TranslateLanguage
import java.util.Locale

data class CameraState(
    val surfaceRequest: SurfaceRequest? = null,
    val recognizedText: String = "",
    val recognizedLanguage: String? = null,
    val targetLanguage: String = TranslateLanguage.INDONESIAN,
    val translatedText: String = "",
    val isTranslating: Boolean = false,
)

fun CameraState.recognizedLanguageDisplayName(): String {
    return runCatching { Locale(recognizedLanguage).displayName }.getOrDefault("")
}