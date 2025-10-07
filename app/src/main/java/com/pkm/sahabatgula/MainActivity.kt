package com.pkm.sahabatgula

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.pkm.sahabatgula.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideNavDestinations = listOf(
                // start
                R.id.splashscreen_fragment,
                R.id.onboarding_fragment,

                // auth
                R.id.register_fragment,
                R.id.login_fragment,
                R.id.verify_otp_forgot_password_fragment,
                R.id.reset_password_fragment,
                R.id.otp_verification_fragment,
                R.id.input_email_forgot_password_fragment,
                R.id.welcome_screen_fragment,

                // input data
                R.id.input_data_graph,
                R.id.input_data_gender_fragment,
                R.id.input_data_age_fragment,
                R.id.input_data_height_fragment,
                R.id.input_data_heavy_fragment,
                R.id.input_data_waist_circumferences_fragment,
                R.id.input_data_blood_pressure_fragment,
                R.id.input_data_high_blood_glucose_fragment,
                R.id.input_data_daily_consumption_fragment,
                R.id.input_data_history_family_fragment,
                R.id.input_data_user_activity_fragment,

                // home
                R.id.log_sugar_fragment,
                R.id.log_food_fragment,
                R.id.log_water_fragment,
                R.id.log_history_fragment,
                R.id.log_carbo_fragment,
                R.id.log_fat_fragment,
                R.id.log_protein_fragment,
                R.id.root_log_activity_fragment,

                // scan and log food
                R.id.log_food_fragment,
                R.id.scan_food_fragment,
                R.id.log_food_fragment_destination,
                R.id.log_manual_food_fragment,
                R.id.result_food_scan_fragment,
                R.id.log_manual_custom_food_fragment,
                R.id.food_result_search_fragment,
                R.id.detail_food_fragment,

                // explore
                R.id.article_result_search_fragment,
                R.id.event_result_search_fragment,
                R.id.detail_article,
                R.id.detail_event,

                // setting
                R.id.user_profile_fragment,
                R.id.log_history_fragment,
                R.id.help_center_fragment,
                R.id.about_app_fragment,
                R.id.privacy_policy_fragment,
                R.id.terms_and_conditions_fragment,

            )

            if (destination.id in hideNavDestinations) {
                binding.navView.visibility = View.GONE
            } else {
                binding.navView.visibility = View.VISIBLE
            }
        }


        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.home_fragment, R.id.onboarding_fragment -> setStatusBarGreen()  // Khusus fragment A
                else -> setStatusBarDefault()         // Semua fragment lainnya
            }
        }
    }

    private fun setStatusBarDefault() {
        val window = window
        window.statusBarColor = getColor(R.color.md_theme_onPrimary)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun setStatusBarGreen() {
        val window = window
        window.statusBarColor = getColor(R.color.md_theme_primary)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = 0
        }
    }
}