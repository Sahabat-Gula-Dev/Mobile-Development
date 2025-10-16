package com.pkm.sahabatgula.onboardingscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentOnboardingItemBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingItemFragment : Fragment() {

    private var _binding: FragmentOnboardingItemBinding? = null
    private val binding get() = _binding!!

    private var imageResId: Int = 0
    private var title: String? = null
    private var desc: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageResId = it.getInt(ARG_IMAGE)
            title = it.getString(ARG_TITLE)
            desc = it.getString(ARG_DESC)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingItemBinding.inflate(inflater, container, false)
        Glide.with(this)
            .load(imageResId)
            .placeholder(R.drawable.image_placeholder)
            .into(binding.imgSlide)

        binding.tvTitle.text = title
        binding.tvDescription.text = desc

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_IMAGE = "arg_image"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_DESC = "arg_desc"

        fun newInstance(imageResId: Int, title: String, desc: String) =
            OnboardingItemFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_IMAGE, imageResId)
                    putString(ARG_TITLE, title)
                    putString(ARG_DESC, desc)
                }
            }
    }
}

