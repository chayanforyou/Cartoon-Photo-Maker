package io.github.chayanforyou.cartoonphoto

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.FloatBuffer

class CartoonProcessor(context: Context) {
    private val ortEnv: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val ortSession: OrtSession
    private val inputName: String
    private val outputName: String
    
    companion object {
        private const val MODEL_FILE = "hayao.onnx"
        private const val INPUT_SIZE = 512
        private const val CHANNEL_COUNT = 3
        private const val PIXEL_COUNT = INPUT_SIZE * INPUT_SIZE
        private const val TENSOR_SIZE = CHANNEL_COUNT * PIXEL_COUNT
        
        // Normalization constants (maps [0,255] -> [-1,1])
        private const val NORM_SCALE = 2f / 255f
        private const val NORM_OFFSET = -1f
        
        // De-normalization constants (maps [-1,1] -> [0,255])
        private const val DENORM_SCALE = 0.5f
        private const val DENORM_OFFSET = 0.5f
    }
    
    init {
        val modelBytes = context.assets.open(MODEL_FILE).use { it.readBytes() }
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
        
        // Scale back to original dimensions if needed
        if (bitmap.width == INPUT_SIZE && bitmap.height == INPUT_SIZE) {
            result
        } else {
            result.scale(bitmap.width, bitmap.height)
        }
    }
    
    private fun buildInputTensor(bitmap: Bitmap): OnnxTensor {
        val pixels = IntArray(PIXEL_COUNT).also {
            bitmap.getPixels(it, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        }
        
        val buffer = FloatBuffer.allocate(TENSOR_SIZE)
        
        // Write interleaved RGB per pixel (HWC layout)
        for (pixel in pixels) {
            buffer.put(Color.red(pixel) * NORM_SCALE + NORM_OFFSET)
            buffer.put(Color.green(pixel) * NORM_SCALE + NORM_OFFSET)
            buffer.put(Color.blue(pixel) * NORM_SCALE + NORM_OFFSET)
        }
        
        buffer.rewind()
        return OnnxTensor.createTensor(
            ortEnv,
            buffer,
            longArrayOf(1, INPUT_SIZE.toLong(), INPUT_SIZE.toLong(), CHANNEL_COUNT.toLong())
        )
    }
    
    private fun decodeOutput(tensor: OnnxTensor): Bitmap {
        val buf = tensor.floatBuffer
        val pixels = IntArray(PIXEL_COUNT)
        
        for (i in 0 until PIXEL_COUNT) {
            val r = (buf[i * CHANNEL_COUNT] * DENORM_SCALE + DENORM_OFFSET).toColorByte()
            val g = (buf[i * CHANNEL_COUNT + 1] * DENORM_SCALE + DENORM_OFFSET).toColorByte()
            val b = (buf[i * CHANNEL_COUNT + 2] * DENORM_SCALE + DENORM_OFFSET).toColorByte()
            pixels[i] = Color.rgb(r, g, b)
        }
        
        return createBitmap(INPUT_SIZE, INPUT_SIZE).apply {
            setPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        }
    }
    
    private fun Float.toColorByte(): Int = (this * 255f).toInt().coerceIn(0, 255)
    
    fun close() {
        runCatching {
            ortSession.close()
            ortEnv.close()
        }
    }
}
