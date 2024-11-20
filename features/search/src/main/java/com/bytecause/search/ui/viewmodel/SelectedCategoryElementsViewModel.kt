package com.bytecause.search.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.domain.model.ApiResult
import com.bytecause.domain.model.Loading
import com.bytecause.domain.model.PoiQueryModel
import com.bytecause.domain.usecase.GetPoiResultByRadiusUseCase
import com.bytecause.domain.util.PoiTagsUtil.formatTagString
import com.bytecause.presentation.model.UiState
import com.bytecause.search.ui.model.ElementTagModel
import com.bytecause.search.ui.model.PoiUiModel
import com.bytecause.search.util.PoiTagsUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val INIT_SEARCH_RADIUS = 30000

@HiltViewModel
class SelectedCategoryElementsViewModel
@Inject
constructor(
    private val getPoiResultByRadiusUseCase: GetPoiResultByRadiusUseCase,
) : ViewModel() {
    // Predefined tags for POIs filtering, only these defined tags will appear in filter dialog.
    private val predefinedTagTypes =
        setOf(
            "opening_hours",
            "wheelchair",
            "amenity",
            "leisure",
            "outdoor_seating",
            "indoor_seating",
            "smoking",
            "drink",
            "drink:beer",
            "takeaway",
            "diet:vegan",
            "payment:cash",
            "Credit cards",
            "Crypto payment",
            "QR payment",
            "brewery",
            "sport",
            "shop",
            "min_age",
            "cuisine",
            "tourism",
            "fee",
            "bus",
            "trolleybus",
            "railway",
            "tram",
            "train",
            "microbrewery",
            "craft",
            "seamark",
        )

    private var _uiSearchCategoryState =
        MutableStateFlow<UiState<PoiUiModel>?>(
            null
        )
    val uiSearchCategoryState get() = _uiSearchCategoryState.asStateFlow()

    var categoryElementsSet: Set<PoiUiModel> = emptySet()
        private set

    private val _elementSet =
        MutableStateFlow<Set<PoiUiModel>>(emptySet())
    val elementSet: StateFlow<Set<PoiUiModel>> get() = _elementSet.asStateFlow()

    // Holds unmodified init map key value pairs.
    private val _allTagsMap = mutableMapOf<String, List<ElementTagModel>>()
    val allTagsMap get() = _allTagsMap.toMap()

    var radius: Int = INIT_SEARCH_RADIUS
        private set

    private var getPoiJob: Job? = null

    fun addElements(elements: Set<PoiUiModel>) {
        _elementSet.value = elements
    }

    fun clearElements() {
        _elementSet.value = setOf()
    }

    fun resetSearchRadius() {
        radius = INIT_SEARCH_RADIUS
    }

    // Via this function we save all items for given category, elementSet holds content which will be
    // shown to the user and can be modified using filters, categoryElementsList serves as holder
    // for all elements, so when user clear all filters, elementList's content is replaced by
    // categoryElementsList's content
    fun addAllToCategoryElementsSet(element: Set<PoiUiModel>) {
        categoryElementsSet = element
    }

    fun doubleSearchRadius() {
        radius *= 2
    }

    fun extractAllTags() {
        _elementSet.value.let {
            _allTagsMap.putAll(sortMap(extractValuesToMap(it)))
        }
    }

    fun sortMap(mapElement: Map<String, List<ElementTagModel>>): Map<String, List<ElementTagModel>> {
        val sortedKeys = mapElement.keys.sortedBy { it.firstOrNull()?.lowercase() } // Sort the keys
        return sortedKeys.associateWith { mapElement.getValue(it) }
    }

    // gets place name from tags.keys values
    fun getItemName(
        item: PoiUiModel,
        defaultName: String,
    ): String? {
        // ordered by precedence
        val preferredKeys = listOf("name", "amenity", "type", "shop")

        for (key in preferredKeys) {
            for (string in item.tags.keys) {
                if (string.contains(key)) {
                    return formatTagString(item.tags[string])
                }
            }
        }
        return defaultName
    }

    fun getPoiResult(entity: PoiQueryModel) {
        // we need to cancel current active collector explicitly or additional collector will be created
        getPoiJob?.cancel()

        getPoiJob = viewModelScope.launch {
            _uiSearchCategoryState.value =
                UiState(loading = Loading(true))

            getPoiResultByRadiusUseCase(entity).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val itemsWithUnifiedTags = PoiTagsUtil.generalizeTagKeys(
                            result.data?.map {
                                PoiUiModel(
                                    id = it.placeId,
                                    lat = it.latitude,
                                    lon = it.longitude,
                                    tags = it.tags
                                )
                            } ?: emptyList()
                        )

                        _uiSearchCategoryState.emit(
                            uiSearchCategoryState.value?.copy(
                                loading = Loading(isLoading = false),
                                items = uiSearchCategoryState.value?.items?.plus(
                                    itemsWithUnifiedTags
                                ) ?: itemsWithUnifiedTags
                            )
                        )
                    }

                    is ApiResult.Failure -> {
                        _uiSearchCategoryState.emit(UiState(error = result.exception))
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }
    }

    // filter algorithm which will filter POIs based on selected tags
    fun filterAlgorithm(filterTags: Map<String, List<String>>): Set<PoiUiModel> {
        if (filterTags.isEmpty()) return categoryElementsSet

        val filteredList = mutableSetOf<PoiUiModel>()
        for (element in categoryElementsSet) {
            for ((key, value) in filterTags) {
                if (element.tags.keys.contains(key)) {
                    if (value.contains(element.tags[key])) {
                        filteredList.add(element)
                    }
                }
            }
        }
        return filteredList
    }

    // Checked state update method to keep track of currently filtered elements.
    fun updateCheckedStatus(
        allTags: Map<String, List<ElementTagModel>>,
        filteredTags: Map<String, List<String>>,
    ): Map<String, List<ElementTagModel>> {

        val updatedAllTags =
            allTags.mapValues { (tagType, allTagList) ->
                val filteredTagList = filteredTags[tagType] ?: emptyList()

                val updatedTagList =
                    allTagList.map { tagElement ->
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
    private fun extractValuesToMap(list: Set<PoiUiModel>): Map<String, List<ElementTagModel>> {
        val tagTypeToTagsMap = mutableMapOf<String, MutableSet<ElementTagModel>>()

        for (listElement in list) {
            for ((key, value) in listElement.tags) {
                val tagModel =
                    ElementTagModel(value, isChecked = false)

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

            _elementSet.value.any { it.tags.keys.contains(key) }
        }.mapValues { (_, value) ->
            value.filter { elementTagModel ->
                // Finding values held by visible elements.
                _elementSet.value
                    .any { it.tags.values.contains(elementTagModel.tagName) }
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

    /* private fun extractDaysAndHours(openingHoursString: String?): List<OpeningHour>? {
        openingHoursString ?: return null

        val extractedValues = mutableListOf<OpeningHour>()
        if (openingHoursString.contains(",") || openingHoursString.contains(";")) {
            val splittedStringList = openingHoursString.split("[,;]".toRegex()).map { it.trim() }

            splittedStringList.forEach {

                if (it.matches("\\d\\d:\\d\\d-\\d\\d:\\d\\d".toRegex()) && splittedStringList[1].matches(
                        "[A-Za-z][A-Za-z]\\s(?:closed|off)".toRegex()
                    )
                ) {
                    Log.d(TAG(this), openingHoursString)
                    Log.d(TAG(this), splittedStringList[1])
                    extractedValues.add(OpeningHour("Su-Sa", splittedStringList[0]))
                    return extractedValues
                }

                val reader = StringReader(it)
                val ohParser = OpeningHoursParser(reader)
                val oh = ohParser.rule()

                val days = oh.days?.joinToString()
                val timeSpans = oh.times?.joinToString()

                days?.let {
                    timeSpans?.let {
                        extractedValues.add(OpeningHour(days, timeSpans))
                    }
                }

                /* Log.d(TAG(this), "original: " + openingHoursString)
                 Log.d(TAG(this), "result: " + extractedValues.joinToString())*/

            }
        }
        // Match 08:00-17:00 format.
        else if (openingHoursString.matches("\\d\\d:\\d\\d-\\d\\d:\\d\\d".toRegex())) {
            extractedValues.add(OpeningHour("Su-Sa", openingHoursString))
        } else {
            val reader = StringReader(openingHoursString)
            val ohParser = OpeningHoursParser(reader)
            val oh = ohParser.rule()

            val days = oh.days?.joinToString()
            val timeSpans = oh.times?.joinToString()

            days?.let {
                timeSpans?.let {
                    extractedValues.add(OpeningHour(days, timeSpans))
                }
            }

            /*Log.d(TAG(this), "original: " + openingHoursString)
            Log.d(TAG(this), "result: " + extractedValues.joinToString())*/
        }
        return extractedValues
    }*/
}