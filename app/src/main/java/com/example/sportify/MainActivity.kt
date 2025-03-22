// Update: app/src/main/java/com/example/sportify/MainActivity.kt

package com.example.sportify

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.sportify.model.AuthManager
import com.example.sportify.model.Game
import com.google.android.material.bottomnavigation.BottomNavigationView

interface OnPublicGameClickListener {
    fun onApprovalClicked(position: Int)
}

interface OnMyGameClickListener {
    fun onEditClick(game: Game?)
}

class MainActivity : AppCompatActivity() {

    var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostController: NavHostFragment? = supportFragmentManager.findFragmentById(R.id.main_nav_host) as? NavHostFragment
        navController = navHostController?.navController

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_bar)
        navController?.let { NavigationUI.setupWithNavController(bottomNavigationView, it) }
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.myGamesListFragment -> {
                    navController?.navigate(
                        R.id.myGamesListFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.addGameFragment, true) // Clear AddGameFragment from stack
                            .build()
                    )
                    true
                }
                R.id.addGameFragment -> {
                    navController?.navigate(R.id.addGameFragment)
                    true
                }
                R.id.publicGamesListFragment -> {
                    navController?.navigate(R.id.publicGamesListFragment)
                    true
                }
                R.id.profileFragment -> {
                    navController?.navigate(R.id.profileFragment)
                    true
                }
                else -> false
            }
        }

        setupNavigationListener(bottomNavigationView)
    }

    private fun setupNavigationListener(bottomNavigationView: BottomNavigationView) {
        navController?.addOnDestinationChangedListener { _, destination, _ ->
            // Hide bottom navigation on authentication screens
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment, R.id.forgotPasswordFragment -> {
                    bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }
}