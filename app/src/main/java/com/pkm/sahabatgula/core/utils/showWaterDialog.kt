package com.pkm.sahabatgula.core.utils

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
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

fun showWaterDialog(
    context: Context,
    imageRes: Int,
    title: String,
    subtitle: String,
    positiveText: String,
    onPositiveClick: () -> Unit
) {
    val titleText = "$title\n\n$subtitle"
    val spannable = SpannableString(titleText)

    val firstLineEnd = titleText.indexOf("\n")
    val secondLineStart = firstLineEnd + 2
    val secondLineEnd = titleText.length

    spannable.setSpan(
        StyleSpan(Typeface.BOLD),
        0,
        firstLineEnd,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    spannable.setSpan(
        AbsoluteSizeSpan(14, true),
        secondLineStart,
        secondLineEnd,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannable.setSpan(
        ForegroundColorSpan(ContextCompat.getColor(context, R.color.md_theme_onSurface)),
        secondLineStart,
        secondLineEnd,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    val imageView = ImageView(context).apply {
        setImageResource(imageRes)
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
        typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_regular)
        textAlignment = TextView.TEXT_ALIGNMENT_CENTER
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
    }

    val dialog = MaterialAlertDialogBuilder(context)
        .setView(container)
        .setPositiveButton(positiveText) { d, _ ->
            onPositiveClick()
            d.dismiss()
        }
        .setNegativeButton("Batal") { d, _ -> d.dismiss() }
        .create()

    dialog.show()

    val positiveButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
    val buttonPanel = positiveButton.parent as? ViewGroup
    buttonPanel?.setBackgroundColor(
        ContextCompat.getColor(context, R.color.md_theme_onPrimary)
    )

    dialog.window?.setDimAmount(0.2f)

    positiveButton.setTypeface(ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_semibold), Typeface.BOLD)
    dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.apply {
        setTypeface(ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_semibold), Typeface.BOLD)
    }
}
