package com.bytecause.nautichart.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecause.nautichart.R
import com.bytecause.nautichart.domain.model.IconsChildItem
import com.bytecause.nautichart.domain.model.IconsParentItem
import com.bytecause.nautichart.interfaces.SelectPoiMarkerIconInterface
import com.bytecause.nautichart.ui.view.custom.FullyExpandedRecyclerView
import com.bytecause.nautichart.ui.view.custom.GridItemViewSpacingDecoration

class CustomPoiMarkerIconParentAdapter(
    private var parentList: List<IconsParentItem>,
    private val selectPoiMarkerIconInterface: SelectPoiMarkerIconInterface
) : RecyclerView.Adapter<CustomPoiMarkerIconParentAdapter.ViewHolder>() {

    fun submitList(newList: List<IconsParentItem>) {
        parentList = newList
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textView: TextView = itemView.findViewById(R.id.recycler_view_child_name)
        val childRecyclerView: FullyExpandedRecyclerView =
            itemView.findViewById(R.id.poi_marker_icons_child_recycler_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.select_poi_marker_icon_child_recycler_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return parentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val parent = parentList[position]
        holder.textView.text = parent.categoryName

        val childAdapter = CustomPoiMarkerIconChildAdapter(
            parent.childList.filter {
                it.categoryName == parent.categoryName
            }, selectPoiMarkerIconInterface
        )

        holder.childRecyclerView.apply {
            layoutManager = GridLayoutManager(holder.itemView.context, 8)
            adapter = childAdapter
            setHasFixedSize(true)
            if (itemDecorationCount == 0) addItemDecoration(
                GridItemViewSpacingDecoration(
                    8,
                    20,
                    false
                )
            )
        }
    }
}

class CustomPoiMarkerIconChildAdapter(
    private val iconList: List<IconsChildItem>,
    selectPoiMarkerIconInterface: SelectPoiMarkerIconInterface
) :
    RecyclerView.Adapter<CustomPoiMarkerIconChildAdapter.ViewHolder>() {

    private val listener: SelectPoiMarkerIconInterface = selectPoiMarkerIconInterface

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconImageView: ImageButton = itemView.findViewById(R.id.icon_view_holder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_marker_icon_item_view_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return iconList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val icon = iconList[position]

        holder.iconImageView.apply {
            setImageResource(icon.drawableId)
            setOnClickListener {
                tag = iconList[holder.bindingAdapterPosition].drawableId
                listener.onIconClickListener(it, holder.bindingAdapterPosition)
            }
        }
    }
}