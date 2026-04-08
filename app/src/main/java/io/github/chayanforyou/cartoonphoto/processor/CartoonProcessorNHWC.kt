package io.github.chayanforyou.cartoonphoto.processor

import ai.onnxruntime.OnnxTensor
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import java.nio.FloatBuffer

class CartoonProcessorNHWC(context: Context, modelType: ModelType) :
    CartoonProcessor(context, modelType) {

    /**
     * Converts an ARGB bitmap to an HWC float tensor normalised to [-1, 1].
     * Layout: interleaved R, G, B per pixel — shape [1, H, W, 3] (NHWC).
     */
    override fun buildInputTensor(bitmap: Bitmap): OnnxTensor {
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

    /**
     * Converts an HWC [OnnxTensor] (values in [-1, 1]) back to an ARGB [Bitmap].
     * shinkai output layout: interleaved R, G, B per pixel.
     */
    override fun decodeOutput(tensor: OnnxTensor): Bitmap {
        val buf = tensor.floatBuffer
        val pixels = IntArray(PIXEL_COUNT)

        // Read interleaved RGB per pixel (HWC layout)
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
}
