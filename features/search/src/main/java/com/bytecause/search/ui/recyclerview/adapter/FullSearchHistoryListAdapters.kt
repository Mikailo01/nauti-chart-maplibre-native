package com.bytecause.search.ui.recyclerview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecause.features.search.R
import com.bytecause.presentation.components.views.recyclerview.FullyExpandedRecyclerView
import com.bytecause.search.ui.model.RecentlySearchedPlaceUiModel
import com.bytecause.search.ui.model.SearchHistoryParentItem
import com.bytecause.search.ui.recyclerview.interfaces.SearchHistoryAdapterListener

class FullSearchHistoryListParentAdapter(
    private var parentList: List<SearchHistoryParentItem>,
    private val searchHistoryRecyclerViewInterface: SearchHistoryAdapterListener
) : RecyclerView.Adapter<FullSearchHistoryListParentAdapter.ViewHolder>() {

    fun submitList(list: List<SearchHistoryParentItem>) {
        parentList = list
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val childName: TextView =
            itemView.findViewById(R.id.search_history_recycler_view_child_name)
        val childRecyclerView: FullyExpandedRecyclerView =
            itemView.findViewById(R.id.history_list_child_recycler_view)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.full_history_list_child_recycler_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val parent = parentList[position]
        holder.childName.text = parent.sectionTitle

        val childAdapter =
            FullSearchHistoryListChildAdapter(
                parent.searchHistory,
                position,
                searchHistoryRecyclerViewInterface
            )

        holder.childRecyclerView.apply {
            layoutManager = LinearLayoutManager(holder.itemView.context)
            adapter = childAdapter
            setHasFixedSize(true)
        }
    }

    override fun getItemCount(): Int {
        return parentList.size
    }
}

class FullSearchHistoryListChildAdapter(
    private val childList: List<RecentlySearchedPlaceUiModel>,
    private val parentPosition: Int,
    searchHistoryAdapterListener: SearchHistoryAdapterListener
) : RecyclerView.Adapter<FullSearchHistoryListChildAdapter.ViewHolder>() {

    private val listener: SearchHistoryAdapterListener = searchHistoryAdapterListener

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val innerItemView: LinearLayout =
            itemView.findViewById(com.bytecause.core.presentation.R.id.recycler_view_inner_item_view)
        val placeImage: ImageView =
            itemView.findViewById(com.bytecause.core.presentation.R.id.place_image_view)
        val placeName: TextView =
            itemView.findViewById(com.bytecause.core.presentation.R.id.place_name_text_view)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                com.bytecause.core.presentation.R.layout.searched_places_recycler_view_item_view,
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element = childList[position]

        holder.placeImage.setImageResource(
            when (element.type) {
                "city" -> com.bytecause.core.resources.R.drawable.town_icon
                "village" -> com.bytecause.core.resources.R.drawable.village_icon
                "house" -> com.bytecause.core.resources.R.drawable.house_icon
                else -> com.bytecause.core.resources.R.drawable.map_marker
            }
        )
        holder.placeName.text =
            if (element.displayName.startsWith(element.name)) element.displayName else element.name
        holder.innerItemView.setOnClickListener {
            listener.onItemViewClickListener(it, parentPosition, holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return childList.size
    }
}

