package io.github.chayanforyou.cartoonphoto.processor

import ai.onnxruntime.OnnxTensor
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import java.nio.FloatBuffer

class CartoonProcessorNCHW(context: Context, modelType: ModelType) :
    CartoonProcessor(context, modelType) {

    /**
     * Converts an ARGB bitmap to a CHW float tensor normalised to [-1, 1].
     * Layout: interleaved R, G, B per pixel — shape [1, 3, H, W] (NHWC).
     */
    override fun buildInputTensor(bitmap: Bitmap): OnnxTensor {
        val pixels = IntArray(PIXEL_COUNT).also {
            bitmap.getPixels(it, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        }

        val buffer = FloatBuffer.allocate(TENSOR_SIZE)

        // Write all R values, then all G values, then all B values (CHW layout)
        for (pixel in pixels) {
            buffer.put(Color.red(pixel) * NORM_SCALE + NORM_OFFSET)
        }
        for (pixel in pixels) {
            buffer.put(Color.green(pixel) * NORM_SCALE + NORM_OFFSET)
        }
        for (pixel in pixels) {
            buffer.put(Color.blue(pixel) * NORM_SCALE + NORM_OFFSET)
        }

        buffer.rewind()
        return OnnxTensor.createTensor(
            ortEnv,
            buffer,
            longArrayOf(1, CHANNEL_COUNT.toLong(), INPUT_SIZE.toLong(), INPUT_SIZE.toLong())
        )
    }

    /**
     * Converts a CHW [OnnxTensor] (values in [-1, 1]) back to an ARGB [Bitmap].
     * face_paint output layout: interleaved R, G, B per pixel.
     */
    override fun decodeOutput(tensor: OnnxTensor): Bitmap {
        val buf = tensor.floatBuffer
        val pixels = IntArray(PIXEL_COUNT)

        // Read all R values, then all G values, then all B values (CHW layout)
        for (i in 0 until PIXEL_COUNT) {
            val r = (buf[i] * DENORM_SCALE + DENORM_OFFSET).toColorByte()
            val g = (buf[i + PIXEL_COUNT] * DENORM_SCALE + DENORM_OFFSET).toColorByte()
            val b = (buf[i + PIXEL_COUNT * 2] * DENORM_SCALE + DENORM_OFFSET).toColorByte()
            pixels[i] = Color.rgb(r, g, b)
        }

        return createBitmap(INPUT_SIZE, INPUT_SIZE).apply {
            setPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        }
    }
}
