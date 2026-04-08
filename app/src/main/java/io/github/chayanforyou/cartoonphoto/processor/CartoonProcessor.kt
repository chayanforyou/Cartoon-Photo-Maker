package io.github.chayanforyou.cartoonphoto.processor

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class CartoonProcessor(context: Context, modelType: ModelType) {
    protected val ortEnv: OrtEnvironment = OrtEnvironment.getEnvironment()
    protected val ortSession: OrtSession
    protected val inputName: String
    protected val outputName: String

    companion object {
        const val INPUT_SIZE = 512
        const val CHANNEL_COUNT = 3
        const val PIXEL_COUNT = INPUT_SIZE * INPUT_SIZE
        const val TENSOR_SIZE = CHANNEL_COUNT * PIXEL_COUNT

        // Normalization constants (maps [0,255] -> [-1,1])
        const val NORM_SCALE = 2f / 255f
        const val NORM_OFFSET = -1f

        // De-normalization constants (maps [-1,1] -> [0,255])
        const val DENORM_SCALE = 0.5f
        const val DENORM_OFFSET = 0.5f

        fun create(context: Context, modelType: ModelType): CartoonProcessor {
            return when (modelType) {
                ModelType.HAYAO, ModelType.SHINKAI -> CartoonProcessorNHWC(context, modelType)
                ModelType.FACE_PAINT -> CartoonProcessorNCHW(context, modelType)
            }
        }
    }

    init {
        val modelBytes = context.assets.open(modelType.fileName).use { it.readBytes() }
        ortSession = ortEnv.createSession(modelBytes, OrtSession.SessionOptions())
        inputName = ortSession.inputNames.first()
        outputName = ortSession.outputNames.first()
    }

    suspend fun processImage(bitmap: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val resized = bitmap.scale(INPUT_SIZE, INPUT_SIZE)
        val inputTensor = buildInputTensor(resized)

        val outputs = ortSession.run(mapOf(inputName to inputTensor))
        val outputTensor = outputs[0] as OnnxTensor

        val result = decodeOutput(outputTensor)

        // Scale back to original dimensions if the input wasn't already 512×512
        if (bitmap.width == INPUT_SIZE && bitmap.height == INPUT_SIZE) result
        else result.scale(bitmap.width, bitmap.height)
    }

    // Pre-processing
    protected abstract fun buildInputTensor(bitmap: Bitmap): OnnxTensor

    // Post-processing
    protected abstract fun decodeOutput(tensor: OnnxTensor): Bitmap

    // Maps a [0, 1] float to a clamped [0, 255] integer.
    protected fun Float.toColorByte(): Int = (this * 255f).toInt().coerceIn(0, 255)

    fun close() {
        runCatching {
            ortSession.close()
            ortEnv.close()
        }
    }
}
