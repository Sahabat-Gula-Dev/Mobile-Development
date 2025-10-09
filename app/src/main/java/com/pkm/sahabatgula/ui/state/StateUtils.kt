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


fun GlobalUiState.Companion.NoInternet(): GlobalUiState.Error {
    return GlobalUiState.Error(
        title = "Koneksi Internet Terputus",
        message = "Pastikan kamu terhubung ke internet untuk melanjutkan penggunaan aplikasi.",
        imageRes = R.drawable.glubby_error // ilustrasi glubby sedih/error
    )
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
