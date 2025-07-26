package id.yuana.mlkit.demo.ui.components

import androidx.camera.core.SurfaceRequest
import com.google.mlkit.nl.translate.TranslateLanguage

data class CameraState(
    val surfaceRequest: SurfaceRequest? = null,
    val detectedLanguage: String = TranslateLanguage.ENGLISH,
    val detectedText: String = "",
    val targetLanguage: String = TranslateLanguage.INDONESIAN,
    val translatedText: String = "",
    val isTranslating: Boolean = false,
)
