package org.dgeek.imagemanipulator

import android.graphics.Bitmap
import android.net.Uri

data class ImageData(val image: Bitmap? = null, val info: String? = "", val imagePath: Uri? = null)