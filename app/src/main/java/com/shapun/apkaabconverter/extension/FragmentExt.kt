package com.shapun.apkaabconverter.extension

import android.content.ContentResolver
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

val Fragment.contentResolver: ContentResolver
    get() = context!!.contentResolver

fun Fragment.runOnUiThread(runnable: () -> Unit) {
    ContextCompat.getMainExecutor(context!!).execute(runnable)
}

fun Fragment.toast(message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.shortToast(message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.longToast(message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}
