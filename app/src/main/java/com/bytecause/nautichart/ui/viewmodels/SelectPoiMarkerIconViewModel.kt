package com.bytecause.nautichart.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.nautichart.R
import com.bytecause.nautichart.RecentlyUsedPoiMarkerIcon
import com.bytecause.nautichart.RecentlyUsedPoiMarkerIconList
import com.bytecause.nautichart.data.repository.RecentlyUsedIconsRepository
import com.bytecause.nautichart.domain.model.IconsChildItem
import com.bytecause.nautichart.domain.model.IconsParentItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectPoiMarkerIconViewModel @Inject constructor(
    private val repository: RecentlyUsedIconsRepository
) : ViewModel() {

    private val _contentList = MutableStateFlow<List<IconsParentItem>?>(null)
    val contentList: StateFlow<List<IconsParentItem>?> = _contentList.asStateFlow()

    private val _iconList = mutableListOf<IconsChildItem>()
    val iconList: List<IconsChildItem> = _iconList

    private val parentList = mutableListOf(IconsParentItem("Recently used", _iconList))

    var recentlyUsedIcons: List<IconsChildItem> = listOf()
        private set

    fun addUsedIcon(icon: IconsChildItem) {
        recentlyUsedIcons = recentlyUsedIcons + icon
    }

    fun updateRecentlyUsedIcons(icons: List<IconsChildItem> = recentlyUsedIcons) {
        _iconList.addAll(icons)
        parentList[0] = parentList[0].copy(childList = _iconList.toList())
        _contentList.value = parentList.toList()
    }

    init {
        drawablesReflectionLoader()
    }

    private fun drawablesReflectionLoader() {
        val fieldList = R.drawable::class.java.declaredFields.filter {
            it.name.startsWith("poi_marker_")
        }

        fieldList.forEach {
            getCategory(it.name).let { name ->
                name ?: return@let
                val element = IconsChildItem(name, it.getInt(null))
                _iconList.add(element)
                if (!parentList.contains(IconsParentItem(element.categoryName, _iconList))) {
                    parentList.add(
                        IconsParentItem(name, _iconList)
                    )
                }
            }
        }
        _contentList.value = parentList
    }

    private fun getCategory(fileName: String): String? {
        val parts = fileName.split("_")

        if (parts.size >= 3) {
            return parts[2].replaceFirstChar { it.uppercase() }
        }
        return null
    }

    fun saveRecentlyUsedPoiMarkerIcon(drawableResourceName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getRecentUsedPoiMarkerIcons().firstOrNull().let {
                it ?: return@launch

                if (it.iconDrawableResourceNameList.isNullOrEmpty()) {
                    RecentlyUsedPoiMarkerIcon.newBuilder()
                        .setDrawableResourceName(drawableResourceName).build()
                        .let { newValue ->
                            repository.addRecentUsedPoiMarkerIconList(
                                listOf(newValue)
                            )
                        }
                } else {
                    RecentlyUsedPoiMarkerIcon.newBuilder()
                        .setDrawableResourceName(drawableResourceName).build()
                        .let { newValue ->
                            // If new value is already present, update proto datastore with updated list of values.
                            if (it.iconDrawableResourceNameList.contains(
                                    newValue
                                )
                            ) {
                                val updatedList =
                                    (it.iconDrawableResourceNameList.filter { drawable -> drawable != newValue } + newValue).toList()
                                repository.updateRecentUsedPoiMarkerIconList(updatedList)
                            } else {
                                if (recentlyUsedIcons.size >= 24) {
                                    val updatedList =
                                        it.iconDrawableResourceNameList.toMutableList().apply {
                                            removeFirst()
                                            add(newValue)
                                        }
                                    repository.updateRecentUsedPoiMarkerIconList(
                                        updatedList
                                    )
                                } else {
                                    repository.addRecentUsedPoiMarkerIconList(
                                        listOf(newValue)
                                    )
                                }
                            }
                        }
                }
            }
        }
    }

    fun getRecentUsedPoiMarkerIcons(): Flow<RecentlyUsedPoiMarkerIconList> =
        repository.getRecentUsedPoiMarkerIcons()
}