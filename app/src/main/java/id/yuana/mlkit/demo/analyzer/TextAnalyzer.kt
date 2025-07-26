package id.yuana.mlkit.demo.analyzer

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class TextAnalyzer(
    private val scope: CoroutineScope,
    private val onResult: (Result) -> Unit,
) : ImageAnalysis.Analyzer {

    val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val languageIdentifier = LanguageIdentification.getClient()

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return

        scope.launch {
            try {
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                val recognizedText = recognizeText(image)
                val recognizedLanguage = recognizedLanguage(recognizedText)

                onResult(
                    Result(
                        recognizedText = recognizedText,
                        recognizedLanguage = if (recognizedLanguage == "und") null else recognizedLanguage,
                    )
                )
            } catch (e: Exception) {
                Log.d("TextAnalyzer", "Failed with ${e.localizedMessage} - ${e.javaClass}")
                e.printStackTrace()
            } finally {
                imageProxy.close()
            }
        }
    }

    private suspend fun recognizedLanguage(text: String): String =
        suspendCoroutine { continuation ->
            languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener { languageCode ->
                    Log.d("TextAnalyzer", "Language recognized: $languageCode")
                    continuation.resume(languageCode)
                }
                .addOnFailureListener { e ->
                    Log.d("TextAnalyzer", "Failed to recognize language: ${e.localizedMessage}")
                    continuation.resumeWithException(e)
                }
        }

    private suspend fun recognizeText(image: InputImage): String =
        suspendCoroutine { continuation ->
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    Log.d("TextAnalyzer", "Text recognized: ${visionText.text}")
                    continuation.resume(visionText.text)
                }
                .addOnFailureListener { e ->
                    Log.d("TextAnalyzer", "Failed to recognize text: ${e.localizedMessage}")
                    continuation.resumeWithException(e)
                }
        }

    data class Result(
        val recognizedText: String,
        val recognizedLanguage: String? = null,
    )
}