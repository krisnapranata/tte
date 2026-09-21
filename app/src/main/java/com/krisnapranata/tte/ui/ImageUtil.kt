package com.krisnapranata.tte.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

fun base64ToBitmap(base64: String): Bitmap? = try {
    val bytes = Base64.decode(base64, Base64.DEFAULT)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
} catch (e: Exception) {
    null
}
