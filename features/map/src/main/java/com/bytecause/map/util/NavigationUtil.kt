package com.bytecause.map.util

import androidx.core.net.toUri
import androidx.dynamicanimation.R
import androidx.navigation.NavController
import androidx.navigation.NavOptions

// Extension functions for navigating to destinations present in different feature modules using
// Deep Link
fun NavController.navigateToSearchNavigation() = navigate("search://home".toUri())
fun NavController.navigateToCustomPoiNavigation() = navigate("custom-poi://home".toUri())
fun NavController.navigateToFirstRunNavigation() = navigate("first-run://home".toUri())
fun NavController.navigateToDownloadTilesNavigation() = navigate("download-tiles://home".toUri())
fun NavController.navigateToCustomTileProviderNavigation() =
    navigate("custom-tile-provider://home".toUri())