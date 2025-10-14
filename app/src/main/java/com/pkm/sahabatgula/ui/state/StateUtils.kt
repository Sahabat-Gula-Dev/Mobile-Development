package com.pkm.sahabatgula.ui.state

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.pkm.sahabatgula.R

fun GlobalUiState.toBundle(): Bundle {
    val b = Bundle()
    when (this) {
        is GlobalUiState.Loading -> {
            b.putString("type", "loading")
            b.putString("message", message)
            b.putInt("imageRes", imageRes ?: 0)
        }
        is GlobalUiState.Success -> {
            b.putString("type", "success")
            b.putString("title", title)
            b.putString("message", message)
            b.putInt("imageRes", imageRes ?: 0)
        }
        is GlobalUiState.Error -> {
            b.putString("type", "error")
            b.putString("title", title)
            b.putString("message", message)
            b.putInt("imageRes", imageRes ?: 0)
        }
        GlobalUiState.None -> b.putString("type", "none")
        else -> {}
    }
    return b
}

fun Bundle.toState(): GlobalUiState {
    return when (getString("type")) {
        "loading" -> GlobalUiState.Loading(getString("message"), getInt("imageRes").takeIf { it != 0 })
        "success" -> GlobalUiState.Success(
            getString("title").orEmpty(),
            getString("message"),
            getInt("imageRes").takeIf { it != 0 }
        )
        "error" -> GlobalUiState.Error(
            getString("title").orEmpty(),
            getString("message").orEmpty(),
            getInt("imageRes").takeIf { it != 0 }
        )
        else -> GlobalUiState.None
    }
}


fun Fragment.showNoInternetDialogAndExit() {
    val dialog = StateDialogFragment.newInstance(
        GlobalUiState.Error(
            title = "Koneksi Internet Terputus",
            message = "Pastikan kamu terhubung ke internet untuk melanjutkan.",
            imageRes = R.drawable.glubby_error
        )
    )
    dialog.dismissListener = { requireActivity().finishAffinity() }
    dialog.show(parentFragmentManager, "NoInternetDialog")
}


fun DialogFoodUiState.toBundleDetail(): Bundle {
    val b = Bundle()
    when (this) {
        is DialogFoodUiState.Success -> {
            b.putString("type", "success")
            b.putString("title", title)
            b.putString("message", message)
            b.putInt("imageRes", imageRes ?: 0)
            b.putInt("calorieValue", calorieValue ?: 0)
            b.putInt("carbo", carbo ?: 0)
            b.putInt("protein", protein ?: 0)
            b.putInt("fat", fat ?: 0)
            b.putDouble("sugar", sugar ?: 0.0)
            b.putDouble("sodium", sodium ?: 0.0)
            b.putDouble("fiber", fiber ?: 0.0)
            b.putDouble("kalium", kalium ?: 0.0)
        }
        is DialogFoodUiState.Error -> {
            b.putString("type", "error")
            b.putString("title", title)
            b.putString("message", message)
            b.putInt("imageRes", imageRes ?: 0)
        }
        DialogFoodUiState.None -> b.putString("type", "none")
        else -> {}
    }
    return b
}

fun Bundle.toDialogFoodUiState(): DialogFoodUiState {
    return when (getString("type")) {
        "success" -> DialogFoodUiState.Success(
            title = getString("title").orEmpty(),
            message = getString("message"),
            imageRes = getInt("imageRes").takeIf { it != 0 },
            calorieValue = getInt("calorieValue").takeIf { it != 0 },
            carbo = getInt("carbo").takeIf { it != 0 },
            protein = getInt("protein").takeIf { it != 0 },
            fat = getInt("fat").takeIf { it != 0 },
            sugar = getDouble("sugar").takeIf { it != 0.0 },
            sodium = getDouble("sodium").takeIf { it != 0.0 },
            fiber = getDouble("fiber").takeIf { it != 0.0 },
            kalium = getDouble("kalium").takeIf { it != 0.0 }
        )
        "error" -> DialogFoodUiState.Error(
            title = getString("title").orEmpty(),
            message = getString("message").orEmpty(),
            imageRes = getInt("imageRes").takeIf { it != 0 }
        )
        else -> DialogFoodUiState.None
    }
}