package com.shapun.apkaabconverter.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import com.android.tools.build.bundletool.model.Password
import com.android.tools.build.bundletool.model.SignerConfig
import com.android.tools.build.bundletool.model.exceptions.CommandExecutionException
import com.android.tools.build.bundletool.model.utils.files.FilePreconditions
import com.google.common.collect.ImmutableList
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import javax.security.auth.DestroyFailedException

object Utils {
    fun toast(context: Context?, obj: Any) {
        Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show()
    }

    fun queryName(resolver: ContentResolver, uri: Uri): String {
        val returnCursor = resolver.query(uri, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

    fun setPadding(view: View, value: Int) {
        view.setPadding(value, value, value, value)
    }

    fun copy(ctx: Context, uri: Uri, outputPath: Path) {
        try {
            ctx.contentResolver.openInputStream(uri)
                .use { `is` -> Files.copy(`is`, outputPath, StandardCopyOption.REPLACE_EXISTING) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun copy(ctx: Context, inputPath: Path, uri: Uri) {
        try {
            ctx.contentResolver.openOutputStream(uri).use { os -> Files.copy(inputPath, os) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun dpToPx(context: Context, input: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            input.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

}