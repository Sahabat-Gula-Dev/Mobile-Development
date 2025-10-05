package com.pkm.sahabatgula.ui.settings

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
                findNavController().navigate(R.id.action_settings_fragment_to_user_profile_fragment)
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

        binding.cardUpgradePremium.apply {
            icCardSetting.setImageResource(R.drawable.ic_upgrade_premium)
            tvTitleSettingCard.text = "Upgrade Premium"
            tvSubtitleSettingCard.text = "Nikmati fitur premium Sahabat Gula"

            root.setOnClickListener {
                // gluby dalam pengembangan
            }
        }

        binding.cardNewFoodSuggestions.apply {
            icAction.setImageResource(R.drawable.ic_suggestion_food)
            tvTitleAction.text = "Ada Saran Makanan Baru? "
            tvSubtitleAction.text = "Yuk, kasih tahu kami agar bisa kami tambahkan ke daftar"

            root.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green_suggestion_food_background))
            root.setOnClickListener {
                // email untuk suggestion food
            }
        }

        binding.cardPrivacyPolicy.apply {
            icCardSetting.setImageResource(R.drawable.ic_privacy_policy)
            tvTitleSettingCard.text = "Kebijakan Privasi"
            tvSubtitleSettingCard.text = "Pelajari perizinan dan akses aplikasi"
            root.setOnClickListener {
                // ke privacy policy
            }

        }

        binding.cardTermsAndConditions.apply {
            icCardSetting.setImageResource(R.drawable.ic_term_condition)
            tvTitleSettingCard.text = "Syarat dan Ketentuan"
            tvSubtitleSettingCard.text = "Pelajari hak dan kewajiban pengguna"

            root.setOnClickListener {
                // ke terms and conditions
            }
        }

        binding.cardAboutApp.apply {
            icCardSetting.setImageResource(R.drawable.ic_about_app)
            tvTitleSettingCard.text = "Tentang Aplikasi"
            tvSubtitleSettingCard.text = "Versi aplikasi dan pengembangan"

            root.setOnClickListener {
                // ke about app
            }
        }

        binding.cardLogout.apply {
            icCardSetting.setImageResource(R.drawable.ic_logout)
            icArrowRight.visibility = View.GONE
            tvTitleSettingCard.text = "Logout"
            tvSubtitleSettingCard.text = "Keluar dari akun"


            root.strokeWidth = 0
            root.strokeColor = 0
            root.backgroundTintList = null
            root.setBackgroundColor(Color.TRANSPARENT)

        }

    }

}