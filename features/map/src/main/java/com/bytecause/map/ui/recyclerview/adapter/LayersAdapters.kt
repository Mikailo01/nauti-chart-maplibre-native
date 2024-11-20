package com.bytecause.map.ui.recyclerview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bytecause.feature.map.R
import com.bytecause.map.ui.bottomsheet.LayerTypes
import com.bytecause.map.ui.bottomsheet.MapBottomSheetResources
import com.bytecause.map.ui.model.LayersChildItem
import com.bytecause.map.ui.recyclerview.interfaces.SelectLayerListener
import com.bytecause.presentation.components.views.recyclerview.FullyExpandedRecyclerView
import com.google.android.material.divider.MaterialDivider


class LayerParentAdapter(
    private var contentMap: Map<LayerTypes, List<LayersChildItem>>,
    private val selectLayerInterface: SelectLayerListener
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
        val layerTypeTextView: TextView = itemView.findViewById(R.id.layer_type_text_view)
        val divider: MaterialDivider = itemView.findViewById(R.id.recycler_view_divider)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = contentMap.entries.toList()[position]

        holder.layerTypeTextView.text = when (entry.value.firstOrNull()?.layerType) {
            LayerTypes.CUSTOM_OFFLINE_RASTER_TILE_SOURCE -> {
                holder.itemView.context.getString(com.bytecause.core.resources.R.string.offline_raster)
            }

            LayerTypes.CUSTOM_ONLINE_RASTER_TILE_SOURCE -> {
                holder.itemView.context.getString(com.bytecause.core.resources.R.string.online_raster)
            }

            LayerTypes.CUSTOM_OFFLINE_VECTOR_TILE_SOURCE -> {
                holder.itemView.context.getString(com.bytecause.core.resources.R.string.offline_vector)
            }

            LayerTypes.ADDITIONAL_OVERLAY -> {
                holder.itemView.context.getString(com.bytecause.core.resources.R.string.additional_overlay)
            }

            else -> {
                holder.layerTypeTextView.visibility = View.GONE
                null
            }
        }

        holder.divider.visibility =
            if (position == (holder.bindingAdapter?.itemCount?.minus(1) ?: -1)) {
                View.GONE
            } else {
                View.VISIBLE
            }

        val childAdapter = LayerChildAdapter(
            entry.value.filter {
                it.layerType == entry.key
            },
            position,
            selectLayerInterface
        )
        holder.childRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                holder.itemView.context, LinearLayoutManager.HORIZONTAL,
                false
            )
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
    private val parentPosition: Int,
    private val selectLayerInterface: SelectLayerListener
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

        val resources = child.resource as? MapBottomSheetResources.Custom

        if (resources?.imageUrl != null) {
            holder.imageView.load(resources.imageUrl)
        } else {
            // Asynchronous image loading
            holder.imageView.load(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    com.bytecause.core.resources.R.drawable.baseline_layers_24
                )
            )
        }

        child.resource

        holder.textView.apply {
            // Invokes Marquee animation for overflowed texts
            isSelected = true
            text = resources?.name
        }
        holder.innerItemView.apply {
            tag = child.layerType
            setOnClickListener {
                selectLayerInterface.onItemViewClickListener(it, holder.bindingAdapterPosition)
            }
            setOnLongClickListener {
                selectLayerInterface.onItemViewLongClickListener(
                    it,
                    parentPosition,
                    holder.bindingAdapterPosition
                )
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return childList.size
    }
}