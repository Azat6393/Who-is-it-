package com.woynex.kimbu

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.woynex.kimbu.core.service.CallReceiver
import com.woynex.kimbu.core.utils.isAppDefaultDialer
import com.woynex.kimbu.databinding.ActivityMainBinding
import com.woynex.kimbu.feature_search.presentation.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private val viewModel: SearchViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        val mSharedPreferences = getSharedPreferences("UI", Context.MODE_PRIVATE)
        val isDarkMode = mSharedPreferences.getBoolean("DARK_MODE", false)
        if (isDarkMode) {
            setTheme(R.style.Theme_KimBu_Dark)
        } else {
            setTheme(R.style.Theme_KimBu_Light)
        }
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostController =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostController.findNavController()
        binding.bottomNavigationView.setupWithNavController(navController)

        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu.getItem(1).isEnabled = false

        binding.bottomNavLogo.setOnClickListener {
            binding.bottomNavigationView.selectedItemId = R.id.searchFragment
        }

        if (isAppDefaultDialer()) {
            val intent = Intent(this, CallReceiver::class.java)
            this.sendBroadcast(intent)
        }

        if (isAppDefaultDialer()) {
            viewModel.uploadContactsToDatabase()
        }
    }

    override fun onStart() {
        super.onStart()
        if (isAppDefaultDialer()) {
            getCallLog()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun getCallLog() {
        viewModel.updateCallLogs()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}