package com.wonolodroid.wolibrary.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wonolodroid.R
import com.wonolodroid.base.CustomTypefaceSpan

fun String?.isNotNullOrEmpty(): Boolean = !this.isNullOrBlank() && this.trim() != "null" && this.trim() != "NULL"

fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>, isUnderlineText: Boolean = true) {
    val spannableString = SpannableString(this.text)
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                link.second.onClick(view)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = isUnderlineText
            }
        }
        val startIndexOfLink = this.text.toString().indexOf(link.first)
        if (startIndexOfLink != -1) {
            spannableString.setSpan(clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
    this.movementMethod = LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}

fun TextView.makeBold(vararg items: Pair<String, Int>, firstIndex: Boolean = true, withoutBoldFirstEndChar: Boolean = false) {
    val spannableString = SpannableString(this.text)
    for (item in items) {
        val startIndexOfLink = if (firstIndex) {
            this.text.toString().indexOf(item.first)
        } else {
            this.text.toString().lastIndexOf(item.first)
        }
        if (startIndexOfLink != -1) {
            val realStartIndex = if (withoutBoldFirstEndChar) {
                startIndexOfLink + 1
            } else {
                startIndexOfLink
            }

            val endIndex = if (withoutBoldFirstEndChar) {
                startIndexOfLink + item.first.length - 1
            } else {
                startIndexOfLink + item.first.length
            }

            ResourcesCompat.getFont(context, item.second)?.let {
                val typefaceSpan = CustomTypefaceSpan("", it)
                spannableString.setSpan(typefaceSpan, realStartIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }
    }
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}

@JvmOverloads fun FragmentActivity.getCurrentActiveFragment(@IdRes frameId: Int = R.id.content_frame): Fragment? {
    return supportFragmentManager.findFragmentById(frameId)
}

@JvmOverloads fun FragmentActivity.addFragment(fragment: Fragment,
                                 frameId: Int = R.id.content_frame,
                                 tagName: String? = fragment.javaClass.simpleName,
                                 useAnimation: Boolean = false,
                                 addBackStack: Boolean = false,
                                 @AnimatorRes @AnimRes enter: Int = R.anim.wo_slide_in_right,
                                 @AnimatorRes @AnimRes exit: Int = R.anim.wo_slide_out_left) {
    supportFragmentManager.commit(true, body = {
        if (useAnimation) {
            setCustomAnimations(enter, exit)
        }
        if (addBackStack) {
            addToBackStack(tagName)
        }
        add(frameId, fragment, tagName)
    })
}

@JvmOverloads fun FragmentActivity.replaceFragment(fragment: Fragment,
                                     frameId: Int = R.id.content_frame,
                                     tagName: String? = fragment.javaClass.simpleName,
                                     useAnimation: Boolean = false,
                                     addBackStack: Boolean = false,
                                     @AnimatorRes @AnimRes enter: Int = R.anim.wo_slide_in_right,
                                     @AnimatorRes @AnimRes exit: Int = R.anim.wo_slide_out_left) {
    supportFragmentManager.commit(true) {
        if (useAnimation) {
            setCustomAnimations(enter, exit)
        }
        if (addBackStack) {
            addToBackStack(tagName)
        }
        replace(frameId, fragment, tagName)
    }
}

fun Activity?.isAlive(): Boolean {
    return this?.isFinishing == false && !this.isDestroyed
}

/**
 * Kotlin Extensions for simpler, easier and fun way
 * of launching of Activities
 */
inline fun <reified T : Any> Activity.launchActivity(
        requestCode: Int = -1,
        options: Bundle? = null,
        noinline init: Intent.() -> Unit = {}) {
    val intent = newIntent<T>(this)
    intent.init()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        startActivityForResult(intent, requestCode, options)
    } else {
        startActivityForResult(intent, requestCode)
    }
}

inline fun <reified T : Any> Context.launchActivity(
        options: Bundle? = null,
        noinline init: Intent.() -> Unit = {}) {
    val intent = newIntent<T>(this)
    intent.init()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        startActivity(intent, options)
    } else {
        startActivity(intent)
    }
}

inline fun <reified T : Any> newIntent(context: Context): Intent =
        Intent(context, T::class.java)

val Int.toDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.toPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun View.showKeyboard() {
    context?.let { viewContext ->
        val inputMethodManager = viewContext.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun Context.showForcedKeyboard() {
    val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun String?.isAlphaAndSpace(): Boolean {
    if (this?.isNotNullOrEmpty() == true) {
        for (i in this.indices) {
            if (!Character.isLetter(this[i]) && this[i] != ' ') {
                return false
            }
        }
        return true
    } else {
        return false
    }
}

fun Context.findColor(id: Int): Int = ContextCompat.getColor(this, id)

fun View.setMargins(
        leftMarginDp: Int? = null,
        topMarginDp: Int? = null,
        rightMarginDp: Int? = null,
        bottomMarginDp: Int? = null) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        leftMarginDp?.run { params.leftMargin = this.toPx }
        topMarginDp?.run { params.topMargin = this.toPx }
        rightMarginDp?.run { params.rightMargin = this.toPx }
        bottomMarginDp?.run { params.bottomMargin = this.toPx }
        requestLayout()
    }
}

fun RecyclerView.setVerticalOrientation(context: Context) {
    val layoutManager = LinearLayoutManager(context)
    layoutManager.orientation = LinearLayoutManager.VERTICAL
    this.layoutManager = layoutManager
}

fun Activity.hideSoftKeyboard() {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(this)
    }
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

@JvmOverloads fun FragmentActivity.showsDialogFragment(dialogFragment: DialogFragment, tagName: String? = dialogFragment.javaClass.simpleName) {
    supportFragmentManager.let { fragmentManager ->
        dialogFragment.show(fragmentManager, tagName)
    }
}