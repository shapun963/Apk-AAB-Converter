/**
 * view extension
 *
 * @author qingyu
 * <p>
 * Create on 2023/10/09 16:08
 */

@file:Suppress("unused")

package com.shapun.apkaabconverter.extension

import android.view.View
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun EditText.clearText() {
    text = null
}

fun TextInputLayout.error(msg: String?) {
    if (msg.isNullOrBlank()) clearError() else error = msg
}

fun TextInputLayout.clearError() {
    isErrorEnabled = false
}
