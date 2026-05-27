package hazem.nurmontage.videoquran.Utils

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.request.RequestOptions
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.concurrent.ExecutionException

object ImageLoader {

    @Throws(ExecutionException::class, InterruptedException::class)
    fun loadAndCropAndBlur(context: Context, source: Any, width: Int, height: Int): Bitmap {
        return Glide.with(context)
            .asBitmap()
            .load(source)
            .apply(
                RequestOptions.bitmapTransform(StoryCropTransformation(width, height))
                    .override(width, height)
            )
            .submit()
            .get()
    }

    class StoryCropTransformation(
        private val targetWidth: Int,
        private val targetHeight: Int
    ) : Transformation<Bitmap> {

        override fun transform(
            context: Context, resource: Resource<Bitmap>, outWidth: Int, outHeight: Int
        ): Resource<Bitmap> {
            val bitmap = resource.get()
            val bw = bitmap.width
            val bh = bitmap.height
            val currentRatio = bw.toFloat() / bh.toFloat()
            val targetRatio = targetWidth.toFloat() / targetHeight.toFloat()

            val cropped = when {
                currentRatio > targetRatio -> {
                    val cropWidth = (bh * targetRatio).toInt()
                    Bitmap.createBitmap(bitmap, (bw - cropWidth) / 2, 0, cropWidth, bh)
                }
                currentRatio < targetRatio -> {
                    val cropHeight = (bw / targetRatio).toInt()
                    Bitmap.createBitmap(bitmap, 0, (bh - cropHeight) / 2, bw, cropHeight)
                }
                else -> bitmap
            }
            return BitmapResource.obtain(cropped, Glide.get(context).bitmapPool)!!
        }

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
            messageDigest.update(
                "storyCrop(targetWidth=$targetWidth, targetHeight=$targetHeight)"
                    .toByteArray(StandardCharsets.UTF_8)
            )
        }

        override fun equals(other: Any?): Boolean {
            if (other !is StoryCropTransformation) return false
            return targetWidth == other.targetWidth && targetHeight == other.targetHeight
        }

        override fun hashCode(): Int {
            return targetWidth * 31 + targetHeight
        }
    }
}
