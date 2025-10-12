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

//        title = binding.tvTitle
//        message = binding.tvMessage
//        img = binding.imgGlubby
//        btnClose = binding.btnClose
//        cardNutrient = binding.cardCalorie
//        tvCalorieValue = binding.tvCalorieValue
//        tvCarbo = binding.layoutNutrient.tvNumberCarbo
//        tvProtein = binding.layoutNutrient.tvNumberProtein
//        tvFat = binding.layoutNutrient.tvNumberFat
//        tvSugar = binding.layoutNutrient.tvNumberOfSugar
//        tvSodium = binding.layoutNutrient.tvNumberOfSalt
//        tvFiber = binding.layoutNutrient.tvNumberOfFiber
//        tvPotassium = binding.layoutNutrient.tvNumberOfPotasium

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

                // tampilkan summary nutrisi
                binding.layoutNutrient.apply {
                    tvNumberCarbo.text = "${state.carbo?.toString()} gr"
                    tvNumberProtein.text = "${state.protein?.toString()} gr"
                    tvNumberFat.text = "${state.fat?.toString()} gr"
                    tvNumberOfSugar.text = state.sugar?.toString() ?: "0"
                    tvNumberOfSalt.text = state.sodium?.toString() ?: "0"
                    tvNumberOfFiber.text = state.fiber?.toString() ?: "0"
                    tvNumberOfPotasium.text = state.kalium?.toString() ?: "0"
                }

                if (state.calorieValue != null) {
                    binding.cardCalorie.visibility = View.VISIBLE
                    binding.tvCalorieValue.text = "${state.calorieValue}"
                } else {
                    binding.cardCalorie.visibility = View.GONE
                }
            }

            is DialogFoodUiState.Error -> {
                binding.tvTitle.text = state.title
                binding.tvMessage.text = state.message
                binding.imgGlubby.setImageResource(state.imageRes ?: R.drawable.glubby_error)
                binding.cardCalorie.visibility = View.GONE
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
