package com.kevin.receipttrackr.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.kevin.receipttrackr.debug.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis

@Singleton
class OcrEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tag = "OcrEngine"
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    data class OcrResult(
        val text: String,
        val blockCount: Int,
        val processingTimeMs: Long,
        val bitmap: Bitmap?
    )

    suspend fun processImage(uri: Uri): OcrResult = withContext(Dispatchers.Default) {
        Logger.d(tag, "Processing image: $uri")

        var bitmap: Bitmap? = null
        var thumbnailBitmap: Bitmap? = null
        var text = ""
        var blockCount = 0

        val timeMs = measureTimeMillis {
            try {
                // Load and downscale bitmap
                bitmap = loadAndDownscaleBitmap(uri)
                if (bitmap == null) {
                    Logger.e(tag, "Failed to load bitmap")
                    return@measureTimeMillis
                }

                Logger.d(tag, "Bitmap size: ${bitmap!!.width}x${bitmap!!.height}")

                // Create thumbnail for storage (to avoid memory issues)
                thumbnailBitmap = Bitmap.createScaledBitmap(
                    bitmap!!,
                    (bitmap!!.width * 0.3f).toInt(),
                    (bitmap!!.height * 0.3f).toInt(),
                    true
                )

                // Run OCR
                val image = InputImage.fromBitmap(bitmap!!, 0)
                val result = recognizer.process(image).await()

                text = result.text
                blockCount = result.textBlocks.size

                Logger.d(tag, "OCR complete: ${text.length} chars, $blockCount blocks")

            } catch (e: Exception) {
                Logger.e(tag, "OCR failed: ${e.message}")
                thumbnailBitmap?.recycle()
                thumbnailBitmap = null
            } finally {
                // Recycle the full-size bitmap to free memory
                bitmap?.recycle()
            }
        }

        Logger.d(tag, "Processing took ${timeMs}ms")

        OcrResult(
            text = text,
            blockCount = blockCount,
            processingTimeMs = timeMs,
            bitmap = thumbnailBitmap
        )
    }

    private fun loadAndDownscaleBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            // First decode bounds to check size
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Calculate sample size to downscale if needed
            val maxDimension = 3000
            val scale = maxOf(
                options.outWidth / maxDimension,
                options.outHeight / maxDimension
            ).coerceAtLeast(1)

            // Decode with sample size
            val finalOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
            }

            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, finalOptions)
            }
        } catch (e: Exception) {
            Logger.e(tag, "Failed to load bitmap: ${e.message}")
            null
        }
    }
}
