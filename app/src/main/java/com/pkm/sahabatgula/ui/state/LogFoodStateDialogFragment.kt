package com.pkm.sahabatgula.ui.state

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.DialogLogFoodBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LogFoodStateDialogFragment : DialogFragment() {

    private var _binding: DialogLogFoodBinding? = null
    private val binding get() = _binding!!

    private var state: DialogFoodUiState = DialogFoodUiState.None
    var dismissListener: (() -> Unit)? = null

    companion object {
        private const val ARG_STATE_FOOD = "arg_state_food"

        fun newInstance(state: DialogFoodUiState): LogFoodStateDialogFragment {
            val fragment = LogFoodStateDialogFragment()
            fragment.arguments = Bundle().apply {
                putBundle(ARG_STATE_FOOD, state.toBundleDetail())
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = DialogLogFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        state = (arguments?.getBundle(ARG_STATE_FOOD)?.toDialogFoodUiState() ?: DialogFoodUiState.None) as DialogFoodUiState
        binding.btnClose.setOnClickListener { dismiss() }
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.invoke()
    }

    private fun renderState(state: DialogFoodUiState) {
        when (state) {
            is DialogFoodUiState.Success -> {
                binding.btnClose.visibility = View.VISIBLE
                binding.tvTitle.text = state.title
                binding.tvMessage.text = state.message ?: ""
                binding.imgGlubby.setImageResource(state.imageRes ?: R.drawable.glubby_success)

                val hasNutrientData = listOf(
                    state.carbo, state.protein, state.fat,
                    state.sugar, state.sodium, state.fiber, state.kalium
                ).any { it != null && it != 0 && it != 0.0 }

                if (hasNutrientData) {
                    binding.layoutNutrient.root.visibility = View.VISIBLE
                    binding.layoutNutrient.apply {
                        tvNumberCarbo.text = state.carbo?.toString() ?: "-"
                        tvNumberProtein.text = state.protein?.toString() ?: "-"
                        tvNumberFat.text = state.fat?.toString() ?: "-"
                        tvNumberOfSugar.text = state.sugar?.toString() ?: "-"
                        tvNumberOfSalt.text = state.sodium?.toString() ?: "-"
                        tvNumberOfFiber.text = state.fiber?.toString() ?: "-"
                        tvNumberOfPotasium.text = state.kalium?.toString() ?: "-"
                    }
                } else {
                    binding.layoutNutrient.root.visibility = View.GONE
                }
            }

            is DialogFoodUiState.Error -> {
                binding.tvTitle.text = state.title
                binding.tvMessage.text = state.message
                binding.imgGlubby.setImageResource(state.imageRes ?: R.drawable.glubby_error)
                binding.layoutNutrient.root.visibility = View.GONE
            }

            DialogFoodUiState.None -> dismiss()
            else -> {}
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
