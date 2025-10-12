package com.pkm.sahabatgula.core.utils

import android.content.Context
import android.graphics.Typeface
import android.graphics.text.LineBreaker
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pkm.sahabatgula.R

@RequiresApi(Build.VERSION_CODES.Q)
fun showLogoutConfirmationDialog(
    context: Context,
    onConfirm: () -> Unit
) {
    // 🌟 Gambar Glubby di atas
    val imageView = ImageView(context).apply {
        setImageResource(R.drawable.glubby_error) // ganti dengan asset glubby-mu
        adjustViewBounds = true
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        val size = context.resources.getDimensionPixelSize(R.dimen.dialog_image_size)
        layoutParams = LinearLayout.LayoutParams(size, size).apply {
            gravity = Gravity.CENTER
            bottomMargin = 16
            topMargin = 24
        }
    }

    // 📝 Title pakai spannable biar bisa bold
    val titleText = "Konfirmasi Logout"
    val spannableTitle = SpannableString(titleText).apply {
        setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    val titleView = TextView(context).apply {
        text = spannableTitle
        gravity = Gravity.CENTER
        textSize = 18f
        setTextColor(ContextCompat.getColor(context, R.color.md_theme_primary))
        setPadding(16, 0, 16, 8)
        typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_semibold)
    }

    // 🧠 Message di bawahnya
    val messageView = TextView(context).apply {
        text = "Apakah kamu yakin ingin logout dari akun ini?"
        gravity = Gravity.CENTER
        textSize = 14f
        setTextColor(ContextCompat.getColor(context, R.color.md_theme_primary))
        setPadding(32, 8, 32, 0)
        typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_regular)
        justificationMode = LineBreaker.JUSTIFICATION_MODE_NONE
    }

    // 📦 Container
    val container = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setBackgroundColor(ContextCompat.getColor(context, R.color.md_theme_onPrimary))
        setPadding(24, 24, 24, 16)
        addView(imageView)
        addView(titleView)
        addView(messageView)
    }

    // 🪄 Material Alert Dialog
    val dialog = MaterialAlertDialogBuilder(context)
        .setView(container)
        .setPositiveButton("Ya") { d, _ ->
            onConfirm()
            d.dismiss()
        }
        .setNegativeButton("Batal") { d, _ ->
            d.dismiss()
        }
        .create()

    dialog.show()

    // Styling tombol (optional)
    dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.apply {
        setTypeface(typeface, Typeface.BOLD)
    }
    dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.apply {
        setTypeface(typeface, Typeface.BOLD)
    }
}
