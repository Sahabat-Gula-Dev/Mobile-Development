package com.pkm.sahabatgula.ui.state

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.DialogLogFoodBinding

class LogFoodStateDialogFragment : DialogFragment() {

    private var _binding: DialogLogFoodBinding? = null
    private val binding get() = _binding!!

    private lateinit var title: TextView
    private lateinit var message: TextView
    private lateinit var img: ImageView
    private lateinit var btnClose: ImageView
    private lateinit var cardNutrient: CardView
    private lateinit var tvCalorieValue: TextView
    private lateinit var tvCarbo: TextView
    private lateinit var tvProtein: TextView
    private lateinit var tvFat: TextView
    private lateinit var tvSugar: TextView
    private lateinit var tvSodium: TextView
    private lateinit var tvFiber: TextView
    private lateinit var tvPotassium: TextView

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

                if (state.calorieValue != null) {
                    binding.cardCalorie.visibility = View.VISIBLE
                    binding.tvCalorieValue.text = "${state.calorieValue}"
                    binding.tvPlusSign.visibility = View.GONE
                    binding.icGraphicOfProgress.visibility = View.GONE
                    binding.tvNumberOfPercentage.visibility = View.GONE
                    binding.icFood.setImageResource(R.drawable.ic_calories)
                } else {
                    binding.cardCalorie.visibility = View.GONE
                    binding.tvPlusSign.visibility = View.VISIBLE
                    binding.icGraphicOfProgress.visibility = View.VISIBLE
                    binding.tvNumberOfPercentage.visibility = View.VISIBLE
                    binding.icFood.setImageResource(R.drawable.ic_food_salad)
                }

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
                binding.cardCalorie.visibility = View.GONE
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
