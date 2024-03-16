package com.bytecause.nautichart.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecause.nautichart.R
import com.bytecause.nautichart.domain.model.SearchHistoryParentItem
import com.bytecause.nautichart.interfaces.SearchHistoryRecyclerViewInterface
import com.bytecause.nautichart.ui.view.custom.FullyExpandedRecyclerView
import com.bytecause.nautichart.RecentlySearchedPlace

class FullSearchHistoryListParentAdapter(
    private var parentList: List<SearchHistoryParentItem>,
    private val searchHistoryRecyclerViewInterface: SearchHistoryRecyclerViewInterface
) : RecyclerView.Adapter<FullSearchHistoryListParentAdapter.ViewHolder>() {

    fun submitList(list: List<SearchHistoryParentItem>) {
        parentList = list
        notifyItemRangeChanged(0, parentList.size)
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
    private val childList: List<RecentlySearchedPlace>,
    private val parentPosition: Int,
    searchHistoryRecyclerViewInterface: SearchHistoryRecyclerViewInterface
) : RecyclerView.Adapter<FullSearchHistoryListChildAdapter.ViewHolder>() {

    private val listener: SearchHistoryRecyclerViewInterface = searchHistoryRecyclerViewInterface

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val innerItemView: LinearLayout = itemView.findViewById(R.id.recycler_view_inner_item_view)
        val placeImage: ImageView = itemView.findViewById(R.id.place_image_view)
        val placeName: TextView = itemView.findViewById(R.id.place_name_text_view)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.searched_places_recycler_view_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element = childList[position]

        holder.placeImage.setImageResource(
            when (element.type) {
                "city" -> R.drawable.town_icon
                "village" -> R.drawable.village_icon
                "house" -> R.drawable.house_icon
                else -> R.drawable.map_marker
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

