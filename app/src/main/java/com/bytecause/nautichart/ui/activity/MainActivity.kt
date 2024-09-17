package com.bytecause.nautichart.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bytecause.nautichart.R
import com.bytecause.nautichart.ui.dialog.NotificationPermissionDeniedAlertDialog
import com.bytecause.nautichart.ui.viewmodel.MainViewModel
import com.bytecause.presentation.interfaces.DrawerController
import com.bytecause.presentation.viewmodels.MapSharedViewModel
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng


private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
private const val PERMISSION_REQUESTED_BUNDLE_TAG = "isPermissionRequested"
private const val NOTIFICATION_PERMISSION_DENIED_ALERT_DIALOG_TAG =
    "NotificationPermissionDeniedDialog"

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), DrawerController {
    private lateinit var navController: NavController
    private lateinit var windowInsetsController: WindowInsetsControllerCompat

    private val mapSharedViewModel: MapSharedViewModel by viewModels()
    private val viewModel: MainViewModel by viewModels()

    private var isPermissionRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Splash Screen API throws iconView NPE when navigating from implicit intent.
        // Start-up splash screen.
        /* installSplashScreen().apply {
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
         }*/

        setContentView(R.layout.navigation_activity)

        val host: NavHostFragment =
            supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return

        // Set up Action Bar
        navController = host.navController

        val drawerLayout: DrawerLayout? = findViewById(R.id.drawer_layout)

        setupNavigationMenu(navController)

        drawerLayout?.addDrawerListener(
            object : DrawerLayout.DrawerListener {
                override fun onDrawerSlide(
                    drawerView: View,
                    slideOffset: Float,
                ) {
                }

                override fun onDrawerOpened(drawerView: View) {}

                override fun onDrawerClosed(drawerView: View) {}

                override fun onDrawerStateChanged(newState: Int) {
                    if (newState == DrawerLayout.STATE_DRAGGING) {
                        drawerLayout.openDrawer(GravityCompat.START)
                    }
                }
            },
        )

        windowInsetsController =
            WindowInsetsControllerCompat(window, window.decorView)

        // Check if intent was already sent to avoid emitting intent on every activity's recreation.
        if (mapSharedViewModel.geoIntentFlow.replayCache.isEmpty()) {
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
                                LatLng(
                                    latitude = latitude.toDouble(),
                                    longitude = longitude.toDouble(),
                                ),
                                zoom.toDouble(),
                            )
                        }
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            lifecycleScope.launch {
                viewModel.shouldRequestNotificationPermission.collect { shouldRequest ->
                    if (shouldRequest) {
                        savedInstanceState?.let {
                            isPermissionRequested =
                                it.getBoolean(PERMISSION_REQUESTED_BUNDLE_TAG, false)
                        }

                        if (isPermissionRequested.not()) {
                            checkNotificationPermission()
                        }
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the state to avoid redundant requests
        outState.putBoolean(PERMISSION_REQUESTED_BUNDLE_TAG, isPermissionRequested)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request notification permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )

            isPermissionRequested = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

                val dialogFragment = NotificationPermissionDeniedAlertDialog()
                dialogFragment.show(
                    supportFragmentManager,
                    NOTIFICATION_PERMISSION_DENIED_ALERT_DIALOG_TAG
                )

                // user has decided not to allow notification permission, store this decision in the
                // preferences datastore, so it won't be requested again on every activity creation/re-creation
                viewModel.saveShouldRequestNotificationPermission(false)
            }
        }
    }

    private fun setupNavigationMenu(navController: NavController) {
        // In split screen mode, you can drag this view out from the left
        // This does NOT modify the actionbar
        val sideNavView = findViewById<NavigationView>(R.id.nav_view)
        sideNavView?.setupWithNavController(navController)
    }

    override fun toggleDrawer() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }
}
