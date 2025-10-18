package com.pkm.sahabatgula.core.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.text.LineBreaker
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pkm.sahabatgula.R

fun showNutrientExceededDialog(
    context: Context,
    title: String,
    consumed: Int,
    max: Int,
    suggestion: String
) {
    val titleText = "$title\n\n$consumed g  dari  $max g"
    val spannable = SpannableString(titleText)

    val firstLineEnd = titleText.indexOf("\n")
    val secondLineStart = firstLineEnd + 1
    val secondLineEnd = titleText.length

    spannable.setSpan(
        StyleSpan(Typeface.BOLD),
        0,
        firstLineEnd,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannable.setSpan(
        ForegroundColorSpan(Color.RED),
        secondLineStart,
        secondLineEnd,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannable.setSpan(
        RelativeSizeSpan(0.85f),
        secondLineStart,
        secondLineEnd,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    val imageView = ImageView(context).apply {
        setImageResource(R.drawable.glubby_error)
        adjustViewBounds = true
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        val size = context.resources.getDimensionPixelSize(R.dimen.dialog_image_size)
        layoutParams = LinearLayout.LayoutParams(size, size).apply {
            gravity = Gravity.CENTER
            bottomMargin = 16
            topMargin = 32
        }
    }

    val titleView = TextView(context).apply {
        text = spannable
        gravity = Gravity.CENTER
        setPadding(16, 0, 16, 8)
        textSize = 18f
        typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_semibold)
        textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    val customFont = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_regular)
    val messageView = TextView(context).apply {
        text = suggestion
        setPadding(32, 16, 32, 0)
        textSize = 14f
        typeface = customFont
        textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
        gravity = Gravity.START
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            breakStrategy = LineBreaker.BREAK_STRATEGY_SIMPLE
        }
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    val container = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        setPadding(24, 24, 24, 16)
        setBackgroundColor(ContextCompat.getColor(context, R.color.md_theme_onPrimary))
        addView(imageView)
        addView(titleView)
        addView(messageView)
    }

    val dialog = MaterialAlertDialogBuilder(context)
        .setView(container)
        .setPositiveButton("OK", null)
        .create()

    dialog.show()

    val positiveButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)

    val buttonPanel = positiveButton.parent as? ViewGroup
    buttonPanel?.setBackgroundColor(
        ContextCompat.getColor(context, R.color.md_theme_onPrimary)
    )

    dialog.window?.setDimAmount(0.2f)

    dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.apply {
        setTypeface(typeface, Typeface.BOLD)
        setBackgroundColor(ContextCompat.getColor(context, R.color.md_theme_onPrimary))
    }
}
