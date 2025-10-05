package com.pkm.sahabatgula.core.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.pkm.sahabatgula.R

data class RiskCategory(
    val title: String,
    val subtitle: String,
    val colorRes: Int
)

fun getRiskCategory(context: Context, riskIndex: Int?): RiskCategory {
    return when (riskIndex) {
        in 0..3 -> RiskCategory(
            title = "Risiko Sangat Rendah",
            subtitle = "Pertahankan gaya hidup aktif dan pola makan seimbang",
            colorRes = ContextCompat.getColor(context, R.color.green_dark_low)
        )
        in 4..8 -> RiskCategory(
            title = "Risiko Diabetes Rendah",
            subtitle = "Kondisi cukup baik, jaga pola makan dan aktivitas harian",
            colorRes = ContextCompat.getColor(context, R.color.green_dark_low)
        )
        in 9..12 -> RiskCategory(
            title = "Risiko Diabetes Sedang",
            subtitle = "Waktunya lebih aktif dan evaluasi kebiasaan makan",
            colorRes = ContextCompat.getColor(context, R.color.yellow_moderate)
        )
        in 13..20 -> RiskCategory(
            title = "Risiko Diabetes Tinggi",
            subtitle = "Gaya hidup dan riwayat kesehatan menunjukkan risiko tinggi",
            colorRes = ContextCompat.getColor(context, R.color.red_high)
        )
        else -> RiskCategory(
            title = "Risiko Sangat Tinggi",
            subtitle = "Segera konsultasi dan ubah gaya hidup secara drastis",
            colorRes = ContextCompat.getColor(context, R.color.red_high)
        )
    }
}