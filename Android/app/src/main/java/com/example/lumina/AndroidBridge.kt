package com.example.lumina

import android.content.Context
import android.os.Environment
import android.util.Base64
import android.webkit.JavascriptInterface
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

class AndroidBridge(private val context: Context) {

    @JavascriptInterface
    fun saveFile(base64Data: String, fileName: String) {
        try {
            val base64Parts = base64Data.split(",")
            val pureBase64 = if (base64Parts.size > 1) base64Parts[1] else base64Parts[0]
            val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)

            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            FileOutputStream(file).use { it.write(decodedBytes) }

            Toast.makeText(context, "저장 완료: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "저장 실패: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
