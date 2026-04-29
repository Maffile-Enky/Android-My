package com.example.jjjjjppppp

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.jjjjjppppp.databinding.ActivityMainBinding
import com.example.jjjjjppppp.R
import com.example.jjjjjppppp.utils.ThemeManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ThemeManager.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeManager.applyTheme(this, ThemeManager.getCurrentTheme(this))

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavigationView = binding.bottomNavigationView

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 使用NavigationUI设置底部导航栏与NavController的关联
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        // 设置默认选中主页
        bottomNavigationView.selectedItemId = R.id.nav_home
    }
}
