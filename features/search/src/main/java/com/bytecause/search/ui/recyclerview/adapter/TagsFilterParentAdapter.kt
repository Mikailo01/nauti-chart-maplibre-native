package com.bytecause.search.ui.recyclerview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecause.domain.util.PoiTagsUtil.formatTagString
import com.bytecause.features.search.R
import com.bytecause.search.ui.recyclerview.interfaces.SelectCheckBoxListener

class TagsFilterParentAdapter(
    private val parentList: Map<String, List<com.bytecause.domain.model.ElementTagModel>>,
    private val selectCheckBoxInterface: SelectCheckBoxListener
) : RecyclerView.Adapter<TagsFilterParentAdapter.ViewHolder>() {

    // MutableList which will keep track of expanded lists.
    private val expandedStateList: MutableList<Boolean> = MutableList(parentList.size) { false }

    fun restoreExpandedStates(stateList: List<Boolean>) {
        expandedStateList.clear()
        expandedStateList.addAll(stateList)
    }

    fun getExpandedStateList(): List<Boolean> = expandedStateList.toList()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tagTitle: TextView = itemView.findViewById(R.id.tag_title_text_view)
        val childRecyclerView: RecyclerView =
            itemView.findViewById(R.id.check_box_list_child_recycler_view)
        val expandOrCollapseRecyclerViewLinearLayout: LinearLayout =
            itemView.findViewById(R.id.expand_or_collapse_recycler_view_linear_layout)
        val imageView: ImageView = itemView.findViewById(R.id.filter_list_dynamic_image_view)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.filter_list_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val isExpanded = expandedStateList[position]

        holder.tagTitle.text = formatTagString(parentList.keys.elementAt(position))

        val childAdapter = TagsFilterChildAdapter(
            parentList.values.elementAt(position),
            selectCheckBoxInterface
        ).apply {
            setParentAdapterPosition(position)
        }

        holder.childRecyclerView.apply {
            layoutManager = LinearLayoutManager(holder.itemView.context)
            adapter = childAdapter
            setHasFixedSize(true)
        }

        holder.childRecyclerView.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.imageView.setImageDrawable(
            ContextCompat.getDrawable(
                holder.itemView.context,
                if (isExpanded) com.bytecause.core.resources.R.drawable.expand_less_icon else com.bytecause.core.resources.R.drawable.expand_more_icon
            )
        )

        // It has to be done in this way, because views are being recycled, so after the user expand
        // the corresponding list, it won't expand other lists which would be loaded into the same
        // view holder like the one previously expanded before view recycle.
        holder.expandOrCollapseRecyclerViewLinearLayout.setOnClickListener {
            expandedStateList[position] = !isExpanded
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return parentList.size
    }
}

class TagsFilterChildAdapter(
    private val childList: List<com.bytecause.domain.model.ElementTagModel>,
    private val selectCheckBoxInterface: SelectCheckBoxListener
) : RecyclerView.Adapter<TagsFilterChildAdapter.ViewHolder>() {

    private var parentAdapterPosition: Int = -1

    fun setParentAdapterPosition(position: Int) {
        parentAdapterPosition = position
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.check_box_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.check_box_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val child = childList[position]

        holder.checkBox.apply {
            isChecked = child.isChecked
            text = formatTagString(child.tagName)
            tag = child.tagName
            setOnCheckedChangeListener { buttonView, isChecked ->
                selectCheckBoxInterface.onCheckBoxClickListener(buttonView, position, parentAdapterPosition, isChecked)
            }
        }
    }

    override fun getItemCount(): Int {
        return childList.size
    }
}