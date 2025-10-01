package com.pkm.sahabatgula.core.utils


import android.view.View
import android.view.ViewGroup

fun View.setSize(dp: Int) {
    val newSizeInPixels = (dp * this.context.resources.displayMetrics.density).toInt()
    val params: ViewGroup.LayoutParams = this.layoutParams
    params.width = newSizeInPixels
    params.height = newSizeInPixels
    this.layoutParams = params
}