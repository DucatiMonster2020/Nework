package ru.netology.nework.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.ActivityMainBinding
import ru.netology.nework.viewmodel.AuthViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.postsFragment,
                R.id.eventsFragment,
                R.id.usersFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        setupNavigation()
        observeAuthState()
    }

    private fun setupNavigation() {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                }
                else -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                }
            }
        }
    }

    private fun observeAuthState() {
        authViewModel.data.observe(this) { authState ->
            invalidateOptionsMenu()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val isAuthorized = authViewModel.data.value?.isAuthorized ?: false

        menu.findItem(R.id.login).isVisible = !isAuthorized
        menu.findItem(R.id.register).isVisible = !isAuthorized
        menu.findItem(R.id.profile).isVisible = isAuthorized
        menu.findItem(R.id.logout).isVisible = isAuthorized

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.login -> {
                findNavController(R.id.nav_host_fragment_activity_main)
                    .navigate(R.id.loginFragment)
                true
            }
            R.id.register -> {
                findNavController(R.id.nav_host_fragment_activity_main)
                    .navigate(R.id.registerFragment)
                true
            }
            R.id.profile -> {
                findNavController(R.id.nav_host_fragment_activity_main)
                    .navigate(R.id.profileFragment)
                true
            }
            R.id.logout -> {
                authViewModel.logout()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}