package com.bytecause.nautichart.ui.view.activity

import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.res.ResourcesCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bytecause.nautichart.R
import com.bytecause.nautichart.ui.viewmodels.MapSharedViewModel
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var windowInsetsController: WindowInsetsControllerCompat

    private val mapSharedViewModel: MapSharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start-up splash screen.
        installSplashScreen().apply {
            setOnExitAnimationListener { screen ->
                val zoomX = ObjectAnimator.ofFloat(
                    screen.iconView,
                    View.SCALE_X,
                    0.5f,
                    0.0f
                )
                zoomX.interpolator = OvershootInterpolator()
                zoomX.duration = 500L
                zoomX.doOnEnd { screen.remove() }

                val zoomY = ObjectAnimator.ofFloat(
                    screen.iconView,
                    View.SCALE_Y,
                    0.5f,
                    0.0f
                )
                zoomY.interpolator = OvershootInterpolator()
                zoomY.duration = 500L
                zoomY.doOnEnd { screen.remove() }

                zoomX.start()
                zoomY.start()
            }
        }

        setContentView(R.layout.navigation_activity)

        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return

        // Set up Action Bar
        navController = host.navController

        val drawerLayout: DrawerLayout? = findViewById(R.id.drawer_layout)

        setupNavigationMenu(navController)

        window.statusBarColor = ResourcesCompat.getColor(resources, R.color.statusBarColor, null)


        drawerLayout?.addDrawerListener(
            object : DrawerLayout.DrawerListener {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

                override fun onDrawerOpened(drawerView: View) {}

                override fun onDrawerClosed(drawerView: View) {}

                override fun onDrawerStateChanged(newState: Int) {
                    if (newState == DrawerLayout.STATE_DRAGGING) {
                        drawerLayout.openDrawer(GravityCompat.START)
                    }
                }
            })

        windowInsetsController =
            WindowInsetsControllerCompat(window, window.decorView)

        val intent = intent
        if (Intent.ACTION_VIEW == intent.action) {
            val uri: Uri? = intent.data
            if (uri != null) {
                if ("geo" == uri.scheme) {
                    val parts = uri.toString().split(",").toTypedArray()
                    if (parts.size == 2) {
                        val latitude = parts[0].substring(4)
                        val longitude = parts[1].substring(0..7)
                        val zoom = parts[1].substringAfter("=")

                        mapSharedViewModel.setIntentCoordinates(
                            latitude,
                            longitude,
                            zoom.toDouble()
                        )
                        navController.navigate(R.id.map_dest)
                    }
                }
            }
        }
    }

    private fun setupNavigationMenu(navController: NavController) {

        // In split screen mode, you can drag this view out from the left
        // This does NOT modify the actionbar
        val sideNavView = findViewById<NavigationView>(R.id.nav_view)
        sideNavView?.setupWithNavController(navController)
    }

    fun toggleDrawer() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }
}