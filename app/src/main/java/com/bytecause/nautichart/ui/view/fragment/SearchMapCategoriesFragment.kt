package com.bytecause.nautichart.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecause.nautichart.R
import com.bytecause.nautichart.databinding.SearchMapCategoriesFragmentLayoutBinding
import com.bytecause.nautichart.domain.model.parcelable.PoiCategoryModel
import com.bytecause.nautichart.ui.adapter.GenericRecyclerViewAdapter
import com.bytecause.nautichart.ui.adapter.RecyclerViewBindingInterface
import com.bytecause.nautichart.ui.view.delegate.viewBinding
import com.bytecause.nautichart.ui.view.fragment.dialog.SearchMapFragmentDialogDirections
import com.bytecause.nautichart.ui.viewmodels.SearchMapCategoriesViewModel
import com.bytecause.nautichart.util.KeyboardUtils

class SearchMapCategoriesFragment : Fragment(R.layout.search_map_categories_fragment_layout) {

    private val binding by viewBinding(SearchMapCategoriesFragmentLayoutBinding::bind)

    private val viewModel: SearchMapCategoriesViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var genericRecyclerViewAdapter: GenericRecyclerViewAdapter<PoiCategoryModel>

    private var isKeyboardVisible: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val bindingInterface = object : RecyclerViewBindingInterface<PoiCategoryModel> {
            override fun bindData(item: PoiCategoryModel, itemView: View, itemPosition: Int) {
                val innerItemView: LinearLayout =
                    itemView.findViewById(R.id.recycler_view_inner_item_view)
                val categoryImage: ImageView = itemView.findViewById(R.id.place_image_view)
                val categoryName: TextView = itemView.findViewById(R.id.place_name_text_view)
                val distance: TextView = itemView.findViewById(R.id.distance_textview)

                innerItemView.setOnClickListener {
                    val action =
                        SearchMapFragmentDialogDirections.actionSearchMapFragmentDialogToSelectedCategoryElementsDialogFragment(
                            poiCategory = item
                        )
                    findNavController().navigate(action)
                }

                categoryImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        item.drawableId
                    )
                )
                categoryName.text = item.name
                distance.visibility = View.GONE
            }
        }

        genericRecyclerViewAdapter = GenericRecyclerViewAdapter(
            viewModel.categoryList,
            R.layout.searched_places_recycler_view_item_view,
            bindingInterface
        )

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = genericRecyclerViewAdapter

            KeyboardUtils.addKeyboardToggleListener(this@SearchMapCategoriesFragment.activity) { isVisible ->
                isKeyboardVisible = isVisible
            }

            setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    if (isKeyboardVisible) {
                        KeyboardUtils.forceCloseKeyboard(v)
                        v.performClick()
                    }
                    return@setOnTouchListener false
                }
                return@setOnTouchListener false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        KeyboardUtils.removeAllKeyboardToggleListeners()
    }
}