package com.pkm.sahabatgula.ui.settings

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.data.local.SessionManager
import com.pkm.sahabatgula.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    @Inject
    lateinit var sessionManager: SessionManager


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
                Toast.makeText(requireContext(), "Dalam Pengembangan", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cardLogHistory.apply {
            icCardSetting.setImageResource(R.drawable.ic_history_outline)
            tvTitleSettingCard.text = "Riwayat Catatan"
            tvSubtitleSettingCard.text = "Lihat kembali catatan harianmu"

            root.setOnClickListener {
                findNavController().navigate(R.id.action_settings_fragment_to_log_history_fragment)
            }
        }

        binding.cardHelpCenter.apply {
            icCardSetting.setImageResource(R.drawable.ic_description)
            tvTitleSettingCard.text = "Pusat Bantuan"
            tvSubtitleSettingCard.text = "Panduan umum untuk pengguna"

            root.setOnClickListener {
                findNavController().navigate(R.id.action_settings_fragment_to_help_center_fragment)
            }

        }

        binding.cardUpgradePremium.apply {
            icCardSetting.setImageResource(R.drawable.ic_upgrade_premium)
            tvTitleSettingCard.text = "Upgrade Premium"
            tvSubtitleSettingCard.text = "Nikmati fitur premium Sahabat Gula"

            root.setOnClickListener {
                Toast.makeText(requireContext(), "Dalam Pengembangan", Toast.LENGTH_SHORT).show()
            }
        }

        val recipient = "info@sahabatgula.com"
        val subject = "Saran Data Makanan Baru"
        val message = "Halo tim Sahabat Gula, saya ingin memberikan saran terkait data makanan baru"
        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:$recipient")
            putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
            putExtra(android.content.Intent.EXTRA_TEXT, message)
        }

        binding.cardNewFoodSuggestions.apply {
            icAction.setImageResource(R.drawable.ic_suggestion_food)
            tvTitleAction.text = "Ada Saran Makanan Baru? "
            tvSubtitleAction.text = "Yuk, kasih tahu kami agar bisa kami tambahkan ke daftar"

            root.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green_suggestion_food_background))
            root.setOnClickListener {
                try {
                    startActivity(intent)
                } catch (e: android.content.ActivityNotFoundException) {
                    android.widget.Toast.makeText(requireContext(), "Tidak ada aplikasi email yang terinstall", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.cardPrivacyPolicy.apply {
            icCardSetting.setImageResource(R.drawable.ic_privacy_policy)
            tvTitleSettingCard.text = "Kebijakan Privasi"
            tvSubtitleSettingCard.text = "Pelajari perizinan dan akses aplikasi"
            root.setOnClickListener {
                findNavController().navigate(R.id.action_settings_fragment_to_privacy_policy_fragment)
            }

        }

        binding.cardTermsAndConditions.apply {
            icCardSetting.setImageResource(R.drawable.ic_term_condition)
            tvTitleSettingCard.text = "Syarat dan Ketentuan"
            tvSubtitleSettingCard.text = "Pelajari hak dan kewajiban pengguna"

            root.setOnClickListener {
                findNavController().navigate(R.id.action_settings_fragment_to_terms_and_conditions_fragment)
            }
        }

        binding.cardAboutApp.apply {
            icCardSetting.setImageResource(R.drawable.ic_about_app)
            tvTitleSettingCard.text = "Tentang Aplikasi"
            tvSubtitleSettingCard.text = "Versi aplikasi dan pengembangan"

            root.setOnClickListener {
                findNavController().navigate(R.id.action_settings_fragment_to_about_app_fragment)
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

            root.setOnClickListener {
                showLogoutConfirmationDialog()
            }

        }

    }

    private fun showLogoutConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Logout")
            .setMessage("Apakah kamu yakin ingin logout dari akun ini?")
            .setPositiveButton("Ya") { dialog, _ ->
                performLogout()
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun performLogout() {
        // Jalankan di background thread karena clear session menyentuh database
        lifecycleScope.launch {
            sessionManager.clearSession()

            // Navigasi balik ke login (atau splash)
            findNavController().navigate(R.id.action_settings_fragment_to_auth_graph)
            Toast.makeText(requireContext(), "Berhasil logout", Toast.LENGTH_SHORT).show()
        }
    }


}