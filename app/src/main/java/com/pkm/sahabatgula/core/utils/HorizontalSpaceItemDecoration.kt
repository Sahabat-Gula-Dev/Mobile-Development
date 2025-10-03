package com.pkm.sahabatgula.core.utils

import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpaceItemDecoration(private val spaceInDp: Int) : RecyclerView.ItemDecoration() {

    // Konversi DP ke Pixel sekali saja saat inisialisasi
    private val spaceInPx: Int = (spaceInDp * Resources.getSystem().displayMetrics.density).toInt()

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: 0

        // Hanya tambahkan jarak jika bukan item terakhir
        if (position < itemCount - 1) {
            outRect.right = spaceInPx
        }
    }
}