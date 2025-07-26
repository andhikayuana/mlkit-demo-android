package id.yuana.mlkit.demo.ui.components

import android.content.Context
import android.util.Log
import android.util.LruCache
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import id.yuana.mlkit.demo.analyzer.TextAnalyzer
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CameraViewModel : ViewModel() {

    private val _state = MutableStateFlow(CameraState())
    val state: StateFlow<CameraState> = _state

    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _state.update { it.copy(surfaceRequest = newSurfaceRequest) }
        }
    }

    private val translators = object : LruCache<TranslatorOptions, Translator>(1) {
        override fun create(options: TranslatorOptions): Translator {
            return Translation.getClient(options)
        }

        override fun entryRemoved(
            evicted: Boolean,
            key: TranslatorOptions,
            oldValue: Translator,
            newValue: Translator?
        ) {
            oldValue.close()
        }
    }

    private val executroAnalyzer = Executors.newSingleThreadExecutor()
    private val imageAnalysisUseCase = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    init {
        state.debounce(700)
            .onEach { handleTranslate() }
            .launchIn(viewModelScope)
    }

    suspend fun bindToCamera(appContext: Context, lifecycleOwner: LifecycleOwner) {
        val processCameraProvider = ProcessCameraProvider.awaitInstance(appContext)

        imageAnalysisUseCase.clearAnalyzer()
        imageAnalysisUseCase.setAnalyzer(
            executroAnalyzer,
            TextAnalyzer(viewModelScope) { result ->
                _state.update {
                    it.copy(
                        recognizedText = result.recognizedText,
                        recognizedLanguage = result.recognizedLanguage
                    )
                }
            }
        )

        val useCases = listOf<UseCase>(
            cameraPreviewUseCase,
            imageAnalysisUseCase
        )

        processCameraProvider.bindToLifecycle(
            lifecycleOwner, DEFAULT_BACK_CAMERA, *useCases.toTypedArray()
        )

        // Cancellation signals we're done with the camera
        try {
            awaitCancellation()
        } finally {
            processCameraProvider.unbindAll()
        }
    }

    private fun handleTranslate() = viewModelScope.launch {
        try {
            val currentState = state.value
            if (currentState.isTranslationNeeded().not()) {
                return@launch
            }

            _state.update {
                it.copy(
                    lastRecognizedText = currentState.recognizedText,
                    lastRecognizedLanguage = currentState.recognizedLanguage
                )
            }

            if (currentState.canTranslate().not()) {
                Log.d("CameraViewModel", "Cannot translate: ${currentState.canTranslate()}")
                return@launch
            }

            val options = TranslatorOptions.Builder()
                .setSourceLanguage(currentState.recognizedLanguage!!)
                .setTargetLanguage(currentState.targetLanguage)
                .build()

            val translator = translators[options]

            _state.update { it.copy(isTranslating = true) }
            val success = downloadModel(translator)

            if (success.not()) {
                throw IllegalStateException("Failed to download model for ${currentState.targetLanguage}")
            }

            val translatedText = translateText(translator, currentState.recognizedText)
            _state.update { it.copy(translatedText = translatedText) }

        } catch (e: Exception) {
            e.printStackTrace()
            _state.update {
                it.copy(
                    translatedText = "",
                    lastRecognizedText = null,
                    lastRecognizedLanguage = null
                )
            }
        } finally {
            _state.update { it.copy(isTranslating = false) }
        }
    }

    private suspend fun downloadModel(
        translator: Translator
    ): Boolean = suspendCoroutine { continuation ->
        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                continuation.resume(true)
            }
            .addOnFailureListener {
                continuation.resume(false) //error
            }
    }

    private suspend fun translateText(
        translator: Translator,
        text: String
    ): String = suspendCoroutine { continuation ->
        translator.translate(text)
            .addOnSuccessListener {
                continuation.resume(it)
            }
            .addOnFailureListener {
                continuation.resumeWithException(it)
            }
    }


}