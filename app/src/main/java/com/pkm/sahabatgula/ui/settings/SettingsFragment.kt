package com.pkm.sahabatgula.ui.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardUserProfile.apply {
            icCardSetting.setImageResource(R.drawable.ic_person)
            tvTitleSettingCard.text = "Profil Pengguna"
            tvSubtitleSettingCard.text = "Kelola informasi pribadi"

            root.setOnClickListener {
                // ke user profile
            }
        }

        binding.cardNotification.apply {
            icCardSetting.setImageResource(R.drawable.ic_notifications)
            icCardSetting.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.gray_icon),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            tvTitleSettingCard.text = "Notifikasi"
            tvSubtitleSettingCard.text = "Kelola pengingat dan informasi penting"

            root.setOnClickListener {
                // ke gluby dalam pengembangan
            }
        }

        binding.cardLogHistory.apply {
            icCardSetting.setImageResource(R.drawable.ic_history_outline)
            tvTitleSettingCard.text = "Riwayat Catatan"
            tvSubtitleSettingCard.text = "Lihat kembali catatan harianmu"

            root.setOnClickListener {
                // ke history konsumsi dan aktivitas
            }
        }

        binding.cardHelpCenter.apply {
            icCardSetting.setImageResource(R.drawable.ic_description)
            tvTitleSettingCard.text = "Pusat Bantuan"
            tvSubtitleSettingCard.text = "Panduan umum untuk pengguna"

            root.setOnClickListener {
                // ke help center
            }

        }



    }

}