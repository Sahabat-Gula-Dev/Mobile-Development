package com.pkm.sahabatgula.ui.state

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import com.pkm.sahabatgula.R

class StateDialogFragment : DialogFragment() {

    private var handler: Handler? = null
    private var loadingRunnable: Runnable? = null
    private var currentDotCount = 0
    private var minLoadingDuration = 1500L
    private var loadingStartTime = 0L
    private lateinit var title: TextView
    private lateinit var message: TextView
    private lateinit var img: ImageView
    private lateinit var btnAction: Button
    private lateinit var btnClose: ImageView
    private lateinit var cardCalorie: CardView
    private lateinit var tvCalorieValue: TextView

    private var state: GlobalUiState = GlobalUiState.None
    var dismissListener: (() -> Unit)? = null

    companion object {
        private const val ARG_STATE = "arg_state"

        fun newInstance(state: GlobalUiState): StateDialogFragment {
            val fragment = StateDialogFragment()
            fragment.arguments = Bundle().apply {
                putBundle(ARG_STATE, state.toBundle())
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        return inflater.inflate(R.layout.dialog_state, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        title = view.findViewById(R.id.tv_title)
        message = view.findViewById(R.id.tv_message)
        img = view.findViewById(R.id.img_glubby)
        btnAction = view.findViewById(R.id.btnAction)
        btnClose = view.findViewById(R.id.btn_close)
        btnClose.setOnClickListener { dismiss() }
        cardCalorie = view.findViewById(R.id.card_calorie)
        tvCalorieValue = view.findViewById(R.id.tv_calorie_value)

        state = arguments?.getBundle(ARG_STATE)?.toState() ?: GlobalUiState.None
        renderState(state)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.invoke()
    }

    private fun renderState(state: GlobalUiState) {
        when (state) {
            is GlobalUiState.Loading -> {
                loadingStartTime = System.currentTimeMillis()

                title.text = "Tunggu Sebentar"
                message.text = state.message ?: ""
                img.setImageResource(state.imageRes ?: R.drawable.glubby_read)
                btnAction.visibility = View.GONE

                startLoadingDotsAnimation()
            }

            is GlobalUiState.Success -> {

                btnClose.visibility = View.GONE
                stopLoadingDotsAnimation()

                title.text = state.title
                message.text = state.message ?: ""
                img.setImageResource(state.imageRes ?: R.drawable.glubby_success)

                if (state.calorieValue != null) {
                    cardCalorie.visibility = View.VISIBLE
                    tvCalorieValue.text = "${state.calorieValue} "
                } else {
                    cardCalorie.visibility = View.GONE
                }

                btnAction.visibility = View.VISIBLE
                btnAction.text = "Lanjutkan"
                btnAction.setOnClickListener { dismiss() }
            }

            is GlobalUiState.Error -> {
                title.text = state.title
                message.text = state.message
                img.setImageResource(state.imageRes ?: R.drawable.glubby_error)
                btnAction.visibility = View.VISIBLE
                btnAction.text = "Tutup"
                stopLoadingDotsAnimation()
                btnAction.setOnClickListener { dismiss() }
            }
            GlobalUiState.None -> dismiss()
        }
    }

    private fun startLoadingDotsAnimation() {
        handler = Handler(Looper.getMainLooper())
        loadingRunnable = object : Runnable {
            override fun run() {
                currentDotCount = (currentDotCount + 1) % 4
                val dots = ".".repeat(currentDotCount)
                title.text = "Tunggu Sebentar$dots"
                handler?.postDelayed(this, 500)
            }
        }
        handler?.post(loadingRunnable!!)
    }

    private fun stopLoadingDotsAnimation() {
        handler?.removeCallbacksAndMessages(null)
        handler = null
    }

    fun updateState(newState: GlobalUiState) {
        if (state is GlobalUiState.Loading) {
            val elapsed = System.currentTimeMillis() - loadingStartTime
            val remaining = minLoadingDuration - elapsed
            if (remaining > 0) {
                view?.postDelayed({ renderState(newState) }, remaining)
                return
            }
        }
        renderState(newState)
    }


    override fun onDestroyView() {
        stopLoadingDotsAnimation()
        super.onDestroyView()
    }
}



