package com.pkm.sahabatgula.core.utils

import android.app.AlertDialog
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
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import com.pkm.sahabatgula.R

@RequiresApi(Build.VERSION_CODES.Q)
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

    val titleView = TextView(context).apply {
        text = spannable
        gravity = Gravity.CENTER
        setPadding(16, 48, 16, 12)
        textSize = 18f
    }

    val customFont = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_regular)
    val messageView = TextView(context).apply {
        text = suggestion
        setPadding(64, 24, 64, 16)
        textSize = 14f
        typeface = customFont
        textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
        justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
    }

    val dialog = AlertDialog.Builder(context)
        .setCustomTitle(titleView)
        .setView(messageView)
        .setPositiveButton("OK", null)
        .create()

    dialog.show()
    dialog.window?.setDimAmount(0.2f)

    dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
        setTypeface(typeface, Typeface.BOLD)
    }
}
