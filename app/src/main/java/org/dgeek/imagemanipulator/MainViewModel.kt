package org.dgeek.imagemanipulator

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class MainViewModel : ViewModel() {

    private val _outputImage = MutableLiveData<ImageData>()
    val outputImage: LiveData<ImageData> = _outputImage
    private val _inputImage = MutableLiveData<ImageData>()
    val inputImage: LiveData<ImageData> = _inputImage
    private val scope = CoroutineScope(Dispatchers.IO)
    private var imageExt = "jpeg"

    val sliderValue = MutableLiveData<Int>().apply {
        value = 1
    }

    fun compressImage(cr: ContentResolver, compressQuality: Int) {
        val input = inputImage.value?.image
        input?.let {
            val startTime = Calendar.getInstance().timeInMillis
            val path = compressAndSave(cr, input, compressQuality)
            println("compress completed in time  ${Calendar.getInstance().timeInMillis - startTime}ms")
            _outputImage.postValue(ImageData(imagePath = path))
        }
    }

    private fun compressAndSave(cr: ContentResolver, bitmap: Bitmap, compressQuality: Int): Uri? {
        val url = createImageUriForGallery(cr)
        try {
            var outputStream: OutputStream? = null
            try {
                outputStream = cr.openOutputStream(url!!)
                val isCompressed = bitmap.compress(
                    getCompressionFormat(),
                    compressQuality,
                    outputStream
                )
                println("isCompressed $isCompressed")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                try {
                    outputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        return url
    }

    private fun createImageUriForGallery(cr: ContentResolver): Uri? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(Date())
        val values = ContentValues()
        values.put(
            MediaStore.Images.Media.TITLE,
            "ImageManipulator ${File.separator + "IMG_" + timeStamp}"
        )
        values.put(
            MediaStore.Images.Media.DISPLAY_NAME,
            "ImageManipulator ${File.separator + "IMG_" + timeStamp}"
        )
        values.put(
            MediaStore.Images.Media.DESCRIPTION,
            File.separator + "IMG_" + timeStamp + ".$imageExt"
        )
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/$imageExt")
        // Add the date meta data to ensure the image is added at the front of the gallery
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        return cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    private fun getCompressionFormat(): Bitmap.CompressFormat {
        return when (imageExt) {
            "jpeg", "jpg" -> Bitmap.CompressFormat.JPEG
            "png" -> Bitmap.CompressFormat.PNG
            else -> Bitmap.CompressFormat.JPEG
        }
    }

    fun resizeImage(cr: ContentResolver, outHeight: Int, outWidth: Int) {
        val input = inputImage.value?.image
        scope.launch {
            input?.let {
                val startTime = Calendar.getInstance().timeInMillis
                val out = Bitmap.createScaledBitmap(
                    input,
                    outWidth,
                    outHeight,
                    false
                )
                val path = compressAndSave(cr, out, 100)
                println("resize completed in time  ${Calendar.getInstance().timeInMillis - startTime}ms")
                _outputImage.postValue(ImageData(imagePath = path))
            }
        }
    }

    fun updateImageExt(type: String?) {
        imageExt = if (type.isNullOrEmpty()) "jpeg" else type
    }

    fun updateInputImage(imageData: ImageData) {
        _inputImage.value = imageData
    }

    fun updateOutputImage(imageData: ImageData) {
        _outputImage.value = imageData
    }

    fun extractImageInfo(bitmap: Bitmap): String {
        val buffer = StringBuffer()
        buffer.append("Memory Size=> ${bitmap.byteCount / 1024 / 1024}MB\n")
        buffer.append("width=> ${bitmap.width}\n")
        buffer.append("height=> ${bitmap.height}\n")
        return buffer.toString()
    }

    fun updateSliderValue(slideValue: Float) {
        sliderValue.postValue(slideValue.toInt())
    }

}