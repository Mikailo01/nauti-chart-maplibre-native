package com.bytecause.nautichart.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.nautichart.domain.model.ApiResult
import com.bytecause.nautichart.domain.model.ElementTagModel
import com.bytecause.nautichart.domain.model.OverpassNodeModel
import com.bytecause.nautichart.domain.model.PoiQueryEntity
import com.bytecause.nautichart.domain.model.UiState
import com.bytecause.nautichart.domain.usecase.PoiUseCase
import com.bytecause.nautichart.util.PoiUtil
import com.bytecause.nautichart.util.StringUtil
import com.bytecause.nautichart.util.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.net.ConnectException
import javax.inject.Inject

@HiltViewModel
class SelectedCategoryElementsViewModel @Inject constructor(
    private val poiUseCase: PoiUseCase
) : ViewModel() {

    // Predefined tags for POIs filtering, only these defined tags will appear in filter dialog.
    private val predefinedTagTypes = setOf(
        "opening_hours", "wheelchair", "amenity", "outdoor_seating", "indoor_seating", "smoking",
        "drink", "drink:beer", "takeaway", "diet:vegan", "payment:cash", "Credit cards",
        "Crypto payment", "QR payment", "brewery", "sport", "shop", "min_age", "cuisine", "tourism",
        "fee", "bus", "trolleybus", "railway", "tram", "train", "microbrewery", "craft", "seamark"
    )

    private var _uiSearchCategoryState = MutableStateFlow<UiState<OverpassNodeModel>?>(null)
    val uiSearchCategoryState get() = _uiSearchCategoryState.asStateFlow()

    private val _categoryElementsList = mutableSetOf<OverpassNodeModel>()
    val categoryElementsList get() = _categoryElementsList.toList()

    private val _elementList = MutableLiveData<List<OverpassNodeModel>>()
    val elementList: LiveData<List<OverpassNodeModel>> get() = _elementList

    // Holds unmodified init map key value pairs.
    private val _allTagsMap = mutableMapOf<String, List<ElementTagModel>>()
    val allTagsMap get() = _allTagsMap.toMap()

    var radius: Int = 30000
        private set

    fun addElements(elements: List<OverpassNodeModel>) {
        _elementList.value = elements
    }

    fun clearElements() {
        _elementList.value = listOf()
    }

    fun addTags(map: Map<String, List<ElementTagModel>>) {
        _allTagsMap.putAll(map)
    }

    fun addAllToCategoryElementsList(element: List<OverpassNodeModel>) {
        _categoryElementsList.addAll(element)
    }

    fun modifySearchRadius(radius: Int) {
        this.radius = radius
    }

    fun extractAllTags() {
        _elementList.value?.let {
            _allTagsMap.putAll(sortMap(extractValuesToMap(it)))
        }
    }

    fun sortMap(mapElement: Map<String, List<ElementTagModel>>): Map<String, List<ElementTagModel>> {
        val sortedKeys = mapElement.keys.sortedBy { it.firstOrNull()?.lowercase() } // Sort the keys
        return sortedKeys.associateWith { mapElement.getValue(it) }
    }

    // gets place name from tags.keys values
    fun getItemName(item: OverpassNodeModel, defaultName: String): String? {
        // ordered by precedence
        val preferredKeys = listOf("name", "amenity", "type", "shop")

        for (key in preferredKeys) {
            for (string in item.tags.keys) {
                if (string.contains(key)) {
                    return StringUtil.formatTagString(item.tags[string])
                }
            }
        }
        return defaultName
    }

    fun getPoiResult(entity: PoiQueryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiSearchCategoryState.value = UiState(isLoading = true)
            when (val result = poiUseCase.getPoiResultByRadius(entity).firstOrNull()) {
                is ApiResult.Success -> {
                    _uiSearchCategoryState.emit(
                        UiState(
                            isLoading = false,
                            items = PoiUtil().generalizeTagKeys(result.data?.map {
                                OverpassNodeModel(
                                    "",
                                    it.placeId,
                                    it.latitude,
                                    it.longitude,
                                    it.tags
                                )
                            } ?: emptyList()
                            )
                        )
                    )
                }

                is ApiResult.Failure -> {
                    when (result.exception) {
                        is ConnectException -> {
                            _uiSearchCategoryState.emit(UiState(error = UiState.Error.ServiceUnavailable))
                        }

                        is FileNotFoundException -> {
                            Log.d(TAG(this), "file not found")
                            _uiSearchCategoryState.emit(UiState(error = UiState.Error.Other))
                        }

                        else -> {
                            _uiSearchCategoryState.emit(UiState(error = UiState.Error.NetworkError))
                        }
                    }
                }

                else -> {}
            }
        }
    }

    fun filterAlgorithm(filterTags: Map<String, List<String>>): List<OverpassNodeModel> {
        val filteredList = mutableListOf<OverpassNodeModel>()
        for (element in categoryElementsList) {
            if (filterTags.all { (key, value) ->
                    element.tags.keys.any { it.contains(key) }
                            && element.tags.values.any { elementValue ->
                        value.any { filterValue ->
                            elementValue.contains(
                                filterValue
                            )
                        }
                    }
                }) {
                filteredList.add(element)
            }
        }
        return filteredList
    }

    // gets place name from tags.keys values
    /* private fun getItemName(item: OverpassApiModel): String? {
         // ordered by precedence
         val preferredKeys = listOf("name", "amenity", "type", "shop")

         for (key in preferredKeys) {
             for (string in item.tags.keys) {
                 if (string.contains(key)) {
                     return StringUtil.formatTagString(item.tags[string])
                 }
             }
         }
         return args.poiCategory.name
     }*/

    // Checked state update method to keep track of currently filtered elements.
    fun updateCheckedStatus(
        allTags: Map<String, List<ElementTagModel>>,
        filteredTags: Map<String, List<String>>
    ): Map<String, List<ElementTagModel>> {

        Log.d(TAG(this), filteredTags.values.joinToString())

        val updatedAllTags = allTags.mapValues { (tagType, allTagList) ->
            val filteredTagList = filteredTags[tagType] ?: emptyList()

            val updatedTagList = allTagList.map { tagElement ->
                // Find tags present in filteredTags map and set isChecked to true, for others set isChecked state to false.
                val matchingFilteredTag =
                    filteredTagList.find { name -> name == tagElement.tagName }
                if (matchingFilteredTag != null) {
                    // Tag is present in filtered tags, set isChecked to true.
                    tagElement.copy(isChecked = true)
                } else {
                    // Tag is not present in filtered tags, set isChecked to false
                    tagElement.copy(isChecked = false)
                }
            }
            updatedTagList
        }
        return updatedAllTags
    }

    // Extracts values from each element of the list and save them as keys, values pairs.
    private fun extractValuesToMap(list: List<OverpassNodeModel>): Map<String, List<ElementTagModel>> {
        val tagTypeToTagsMap = mutableMapOf<String, MutableSet<ElementTagModel>>()

        for (listElement in list) {
            for ((key, value) in listElement.tags) {
                val tagModel = ElementTagModel(value, isChecked = false)

                determineTagType(key)?.let { tagKey ->
                    if (tagTypeToTagsMap.containsKey(tagKey)) {
                        tagTypeToTagsMap[tagKey]?.add(tagModel)
                    } else {
                        tagTypeToTagsMap[tagKey] = mutableSetOf(tagModel)
                    }
                }
            }
        }
        return tagTypeToTagsMap.mapValues {
            it.value.toList().sortedBy { element -> element.tagName.lowercase() }
        }
    }

    // If filters are applied show tags only for filtered elements.
    fun getTagsOfVisibleItems(): Map<String, List<ElementTagModel>> {
        return allTagsMap.filterKeys { key ->
            // Finding keys that are present in visible elements.

            _elementList.value?.any { it.tags.keys.contains(key) } == true
        }.mapValues { (_, value) ->
            value.filter { elementTagModel ->
                // Finding values held by visible elements.
                _elementList.value
                    ?.any { it.tags.values.contains(elementTagModel.tagName) } == true
            }
        }
    }

    // Checks type of tag to filter unnecessary tags, if match is found tag type is returned,
    private fun determineTagType(tagKey: String): String? {
        // Modify tagPattern to match only tags with the structure "something:type"
        val tagPattern = Regex("^([^:]+):[^:]+")

        val matchResult = tagPattern.find(tagKey)
        val firstWord = matchResult?.groupValues?.get(1) ?: ""

        return when {
            firstWord.isNotEmpty() && predefinedTagTypes.any { it.contains(firstWord) } -> matchResult?.value
            tagKey in predefinedTagTypes -> tagKey
            // Tag type isn't present in predefined tag types set, so null is returned.
            else -> null
        }
    }
}