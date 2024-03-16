package com.bytecause.nautichart.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecause.nautichart.R
import com.bytecause.nautichart.domain.model.LayersChildItem
import com.bytecause.nautichart.interfaces.SelectLayerInterface
import com.bytecause.nautichart.ui.view.custom.FullyExpandedRecyclerView
import com.bytecause.nautichart.ui.view.fragment.bottomsheet.LayerTypes
import com.google.android.material.divider.MaterialDivider

class LayerParentAdapter(
    private val contentMap: Map<LayerTypes, List<LayersChildItem>>,
    private val selectLayerInterface: SelectLayerInterface
) : RecyclerView.Adapter<LayerParentAdapter.ViewHolder>() {
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

        if (entry.key == LayerTypes.ADDITIONALOVERLAY) holder.divider.visibility =
            View.GONE

        val childAdapter = LayerChildAdapter(
            entry.value.filter {
                it.type == entry.key
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

        holder.imageView.setImageDrawable(
            ContextCompat.getDrawable(
                holder.itemView.context,
                child.drawableId
            )
        )
        holder.textView.text = holder.itemView.context.getString(child.resourceNameId)
        holder.innerItemView.apply {
            tag = child.type
            setOnClickListener {
                selectLayerInterface.onItemViewClickListener(it, holder.bindingAdapterPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return childList.size
    }
}