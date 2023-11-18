package com.owusu.userdemo

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.io.InputStream

class QRCodeDecoder {

    private val TAG = "YourTag"

    fun decodeQRCode(contentResolver: ContentResolver, uri: Uri?): String? {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri!!)
            val bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)

            if (bitmap == null) {
                Log.e(TAG, "URI is not a bitmap: $uri")
                return null
            }

            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            bitmap.recycle()

            val source = RGBLuminanceSource(width, height, pixels)
            val bBitmap = BinaryBitmap(HybridBinarizer(source))
            val reader = MultiFormatReader()

            try {
                return reader.decode(bBitmap).text
            } catch (e: NotFoundException) {
                Log.e(TAG, "Decode exception", e)
                return null
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception decoding QR code", e)
            return null
        }
    }
}