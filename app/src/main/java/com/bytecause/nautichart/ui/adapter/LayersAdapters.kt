package com.bytecause.nautichart.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bytecause.nautichart.R
import com.bytecause.nautichart.domain.model.LayersChildItem
import com.bytecause.nautichart.interfaces.SelectLayerInterface
import com.bytecause.nautichart.ui.view.custom.FullyExpandedRecyclerView
import com.bytecause.nautichart.ui.view.fragment.bottomsheet.LayerTypes
import com.bytecause.nautichart.ui.view.fragment.bottomsheet.MapBottomSheetResources
import com.google.android.material.divider.MaterialDivider

class LayerParentAdapter(
    private var contentMap: Map<LayerTypes, List<LayersChildItem>>,
    private val selectLayerInterface: SelectLayerInterface
) : RecyclerView.Adapter<LayerParentAdapter.ViewHolder>() {

    fun submitList(newContentMap: Map<LayerTypes, List<LayersChildItem>>) {
        contentMap = newContentMap
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layers_parent_item_view, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val childRecyclerView: FullyExpandedRecyclerView =
            itemView.findViewById(R.id.child_layers_recycler_view)
        val divider: MaterialDivider = itemView.findViewById(R.id.recycler_view_divider)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = contentMap.entries.toList()[position]

        if (holder.divider.visibility == View.GONE) {
            if (entry.key == LayerTypes.ADDITIONAL_OVERLAY) holder.divider.visibility =
                View.GONE
        }

        val childAdapter = LayerChildAdapter(
            entry.value.filter {
                it.layerType == entry.key
            }, selectLayerInterface
        )
        holder.childRecyclerView.apply {
            layoutManager = GridLayoutManager(holder.itemView.context, 3)
            adapter = childAdapter
            setHasFixedSize(true)
        }
    }

    override fun getItemCount(): Int {
        return contentMap.size
    }
}

class LayerChildAdapter(
    private val childList: List<LayersChildItem>,
    private val selectLayerInterface: SelectLayerInterface
) : RecyclerView.Adapter<LayerChildAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.layer_drawable)
        val textView: TextView = itemView.findViewById(R.id.layer_text)
        val innerItemView: View = itemView.findViewById(R.id.layers_child_item_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layers_child_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val child = childList[position]

        val imageRes = when (child.resource) {
            MapBottomSheetResources.Default -> {
                R.drawable.terrain
            }

            MapBottomSheetResources.Satellite -> {
                R.drawable.satellite
            }

            MapBottomSheetResources.Topo -> {
                R.drawable.topo_map
            }

            MapBottomSheetResources.Grid -> {
                R.drawable.grid
            }

            is MapBottomSheetResources.Custom -> {
                R.drawable.baseline_layers_24
            }
        }

        val name = when (child.resource) {
            MapBottomSheetResources.Default -> {
                holder.itemView.context.getString(R.string.default_tile_source)
            }

            MapBottomSheetResources.Satellite -> {
                holder.itemView.context.getString(R.string.satellite)
            }

            MapBottomSheetResources.Topo -> {
                holder.itemView.context.getString(R.string.topography)
            }

            MapBottomSheetResources.Grid -> {
                holder.itemView.context.getString(R.string.grid)
            }

            is MapBottomSheetResources.Custom -> {
                child.resource.name
            }
        }

        // Asynchronous image loading
        holder.imageView.load(
            ContextCompat.getDrawable(
                holder.itemView.context,
                imageRes
            )
        )
        holder.textView.apply {
            // Invokes Marquee animation for overflowed texts
            isSelected = true
            text = name
        }
        holder.innerItemView.apply {
            tag = child.layerType
            setOnClickListener {
                selectLayerInterface.onItemViewClickListener(it, holder.bindingAdapterPosition)
            }
            setOnLongClickListener {
                if (position <= 2) return@setOnLongClickListener false
                selectLayerInterface.onItemViewLongClickListener(it, holder.bindingAdapterPosition)
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return childList.size
    }
}