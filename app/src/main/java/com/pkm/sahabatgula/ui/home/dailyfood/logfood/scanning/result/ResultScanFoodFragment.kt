package com.pkm.sahabatgula.ui.home.dailyfood.logfood.scanning.result

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pkm.sahabatgula.databinding.FragmentResultScanFoodBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.ui.state.GlobalUiState
import com.pkm.sahabatgula.ui.state.StateDialogFragment

@AndroidEntryPoint
class ResultScanFoodFragment : Fragment() {

    private var _binding: FragmentResultScanFoodBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ResultFoodScanViewModel by viewModels()

    private lateinit var resultScanAdapter: ResultScanAdapter

    private var stateDialog: StateDialogFragment? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentResultScanFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = binding.topAppBar
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()

        val uri = arguments?.getString("uri")?.toUri()
        viewModel.predictImage(uri)
        observeUiState()

    }

    private fun setupRecyclerView() {
        resultScanAdapter = ResultScanAdapter { foodItem ->
            val action = ResultScanFoodFragmentDirections.actionResultFoodScanToDetailFoodFragment(
                foodItemManual = null,
                foodItem = foodItem)
            view?.findNavController()?.navigate(action)

        }
        binding.rvResult.apply {
            adapter = resultScanAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.rvResult.isVisible = state is ScanUiState.Success

                when (state) {
                    is ScanUiState.Loading -> {
                        if (stateDialog == null) {
                            stateDialog = StateDialogFragment.newInstance(
                                GlobalUiState.Loading(
                                    message = "Gluby sedang menganalisis makanan kamu, tunggu beberapa detik",
                                    imageRes = R.drawable.glubby_read
                                )
                            )
                            stateDialog?.show(parentFragmentManager, "ScanLoadingDialog")
                        } else {
                            stateDialog?.updateState(
                                GlobalUiState.Loading(
                                    message = "Gluby sedang menganalisis makanan kamu, tunggu beberapa detik",
                                    imageRes = R.drawable.glubby_read
                                )
                            )
                        }
                    }

                    is ScanUiState.Error -> {
                        if (stateDialog != null) {
                            stateDialog?.updateState(
                                GlobalUiState.Error(
                                    title = "Oops, Ada Masalah",
                                    message = "Hasil scan ini kurang jelas. Coba ambil foto yang lebih jelas atau pilih dari daftar",
                                    imageRes = R.drawable.glubby_error
                                )
                            )
                        } else {
                            stateDialog = StateDialogFragment.newInstance(
                                GlobalUiState.Error(
                                    title = "Oops, Ada Masalah",
                                    message = "Hasil scan ini kurang jelas. Coba ambil foto yang lebih jelas atau pilih dari daftar",
                                    imageRes = R.drawable.glubby_error
                                )
                            )
                            stateDialog?.show(parentFragmentManager, "ScanErrorDialog")
                        }
                        stateDialog?.dismissListener = { stateDialog = null }
                    }

                    is ScanUiState.Success -> {
                        stateDialog?.dismiss()
                        stateDialog = null

                        if (state.foodItems.isEmpty()) {
                            binding.tvHeaderResultScanFood.visibility = View.GONE
                            binding.tvSubtitleResultScanFood.visibility = View.GONE
                            binding.rvResult.visibility = View.GONE
                            binding.layoutEmptyScan.root.visibility = View.VISIBLE
                            binding.layoutEmptyScan.tvTitle.text = "Oops, Ada Masalah"
                            binding.layoutEmptyScan.tvMessage.text = "Hasil scan ini kurang jelas. Coba ambil foto yang lebih jelas atau pilih dari daftar"
                            binding.layoutEmptyScan.imgGlubby.setImageResource(R.drawable.glubby_error)
                            binding.layoutEmptyScan.cardFoodSuggestion.apply {
                                icAction.setImageResource(R.drawable.ic_suggestion_food)
                                tvTitleAction.text = "Ada Saran Makanan Baru? "
                                tvSubtitleAction.text = "Yuk, kasih tahu kami agar bisa kami tambahkan ke daftar"
                                val recipient = "info@sahabatgula.com"
                                val subject = Uri.encode("Saran Data Makanan Baru")
                                val body = Uri.encode("Halo tim Sahabat Gula, saya ingin memberikan saran terkait data makanan baru")

                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = "mailto:$recipient?subject=$subject&body=$body".toUri()
                                }
                                root.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green_suggestion_food_background))
                                root.setOnClickListener {
                                    try {
                                        startActivity(intent)
                                    } catch (e: ActivityNotFoundException) {
                                        Toast.makeText(requireContext(), "Tidak ada aplikasi email yang terinstall", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            binding.layoutEmptyScan.cardCustomLogFood.apply {
                                icAction.setImageResource(R.drawable.ic_docs_add_log)
                                tvTitleAction.text = "Gak Nemu Menu Yang Pas?"
                                tvSubtitleAction.text = "Yuk, susun sendiri makanannya dari bahan yang tersedia"
                                root.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green_card_action)
                                root.setOnClickListener {
                                    findNavController().navigate(R.id.action_result_food_scan_to_log_manual_custom_food_fragment)
                                }
                            }

                        } else {
                            Log.d("ResultScanFragment", "Mengirim data ke adapter. Jumlah: ${state.foodItems.size}")
                            resultScanAdapter.submitList(state.foodItems)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        stateDialog?.dismiss()
        stateDialog = null
        super.onDestroyView()
    }

}