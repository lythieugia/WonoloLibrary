package com.wonolodroid.wolibrary

import android.content.Context
import android.widget.Toast

class ToastMessage {
    companion object {
        fun show(context: Context, text: String?) {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        }
    }
}