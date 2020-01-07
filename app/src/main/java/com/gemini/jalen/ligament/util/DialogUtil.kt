package com.gemini.jalen.ligament.util

import android.app.AlertDialog
import android.content.Context

object DialogUtil {
    fun alert(context: Context, title: String, content: String, func: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(content)
        builder.setPositiveButton("确定") { _, _ ->
            func.invoke()
        }
        builder.setNeutralButton("取消") { _, _ -> }
        builder.show()
    }

    fun confirm(context: Context, title: String, content: String, func1: () -> Unit, func2: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(content)
        builder.setPositiveButton("通过") { _, _ ->
            func1.invoke()
        }
        builder.setNeutralButton("取消") { _, _ -> }
        builder.setNegativeButton("拒绝") { _, _ ->
            func2.invoke()
        }
        builder.show()
    }
}