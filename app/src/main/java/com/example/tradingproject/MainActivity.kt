package com.example.tradingproject
import android.os.Bundle
import android.text.Html
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

import java.security.KeyStore

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navigationView)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val navOptions = NavOptions.Builder()
                .setLaunchSingleTop(true)
                // popUpTo: ให้ pop กลับไปจนถึง start destination ของกราฟ (ไม่ inclusive ถ้าต้องการให้ destination นั้นยังอยู่ใน back stack)
                .setPopUpTo(navController.graph.findStartDestination().id, inclusive = false)
                .build()
            navController.navigate(menuItem.itemId, null, navOptions)
            true
        }


//        bottomNavigationView.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.nav_home -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.nav_host_fragment, HomeFragment())
//                        .commit()
//                    true
//                }
//                R.id.nav_invest -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.nav_host_fragment, InvestFragment())
//                        .commit()
//                    true
//                }
//                R.id.nav_news -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.nav_host_fragment, NewsFragment())
//                        .commit()
//                    true
//                }
//                R.id.nav_activity -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.nav_host_fragment, ActivityFragment())
//                        .commit()
//                    true
//                }
//                R.id.nav_me -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.nav_host_fragment, MeFragment())
//                        .commit()
//                    true
//                }
//                else -> false
//            }
//        }


    }
}