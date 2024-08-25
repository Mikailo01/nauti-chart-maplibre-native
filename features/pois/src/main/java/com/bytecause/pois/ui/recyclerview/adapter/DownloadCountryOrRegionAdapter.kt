package com.bytecause.pois.ui.recyclerview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecause.features.pois.R
import com.bytecause.pois.ui.getKeyByIndex
import com.bytecause.pois.ui.model.CountryParentItem
import com.bytecause.pois.ui.recyclerview.interfaces.CountryAndRegionsListener
import java.util.Locale

class CountryParentAdapter(
    private var content: Map<String, CountryParentItem>,
    private val listener: CountryAndRegionsListener
) : RecyclerView.Adapter<CountryParentAdapter.ViewHolder>() {

    private var isDownloading: Boolean = false

    fun setIsDownloading(boolean: Boolean) {
        isDownloading = boolean
        notifyItemRangeChanged(0, content.size)
    }

    fun submitMap(updatedMap: Map<String, CountryParentItem>) {
        content = updatedMap
        notifyDataSetChanged()
    }

    // MutableList which will keep track of expanded lists.
    private val expandedStateList: MutableList<Boolean> = MutableList(content.size) { false }

    fun restoreExpandedStates(stateList: List<Boolean>) {
        expandedStateList.clear()
        expandedStateList.addAll(stateList)
    }

    fun getExpandedStateList(): List<Boolean> = expandedStateList.toList()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val countryName: TextView = itemView.findViewById(R.id.country_name_text_view)
        val expandImageView: ImageView =
            itemView.findViewById(R.id.expand_country_regions_image_view)
        val sizeTextView: TextView = itemView.findViewById(R.id.element_size)
        val downloadImageView: ImageView = itemView.findViewById(R.id.download_image_view)
        val childRecyclerView: RecyclerView =
            itemView.findViewById(R.id.region_check_box_list)
        val progressBarLinearLayout: LinearLayout =
            itemView.findViewById(R.id.progress_bar_linear_layout)
        val expandableContent: LinearLayout =
            itemView.findViewById(R.id.expandable_content_linear_layout)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.download_country_or_region_poi_parent_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val isExpanded = if (expandedStateList.size != 0) expandedStateList[position] else false

        val iso2 = content.keys.elementAt(position)

        holder.countryName.text = Locale("", iso2).displayCountry

        val size = content[iso2]?.size
        if (!size.isNullOrEmpty() && size != "" && !isDownloading) {
            // If size is available and not empty
            holder.sizeTextView.visibility = View.VISIBLE
            holder.sizeTextView.text = size
            holder.downloadImageView.visibility = View.VISIBLE
        } else {
            // If size is not available or empty
            holder.sizeTextView.visibility = View.GONE
            holder.downloadImageView.visibility = View.GONE
        }

        if (holder.downloadImageView.visibility == View.VISIBLE) {
            holder.downloadImageView.setOnClickListener {
                listener.onCountryDownloadClickListener(position)
            }
        }

        val childAdapter = RegionChildAdapter(
            content.values.elementAt(position),
            isDownloading,
            listener
        ).apply {
            setParentAdapterPosition(position)
        }

        holder.childRecyclerView.apply {
            layoutManager = LinearLayoutManager(holder.itemView.context)
            adapter = childAdapter
            setHasFixedSize(true)
        }

        holder.expandableContent.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.expandImageView.setImageDrawable(
            ContextCompat.getDrawable(
                holder.itemView.context,
                if (isExpanded) com.bytecause.core.resources.R.drawable.expand_less_icon else com.bytecause.core.resources.R.drawable.expand_more_icon
            )
        )

        if (content[content.getKeyByIndex(position)]?.isLoading == true) {
            holder.progressBarLinearLayout.visibility =
                View.VISIBLE
        } else {
            holder.progressBarLinearLayout.visibility = View.GONE
        }

        // It has to be done in this way, because views are being recycled, so after the user expand
        // the corresponding country, it won't expand other countries which would be loaded into the same
        // view holder like the one previously expanded before view recycle.
        holder.expandImageView.setOnClickListener {
            // if checkbox is checked, disable expand button.
            if (content[content.getKeyByIndex(position)]?.regionList?.any { it.isChecked } == true) {
                return@setOnClickListener
            }
            // if regionList is empty, invoke callback in DownloadPoiSelectCountryFragment.
            if (content[content.getKeyByIndex(position)]?.regionList?.isEmpty() == true && !isExpanded) {
                listener.onExpandClickListener(position)
            }

            if (expandedStateList.size != 0) {
                expandedStateList[position] = !isExpanded
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount(): Int = content.size
}

class RegionChildAdapter(
    private val regionEntity: CountryParentItem,
    private val isDownloadInProgress: Boolean,
    private val selectCheckBoxInterface: CountryAndRegionsListener
) : RecyclerView.Adapter<RegionChildAdapter.ViewHolder>() {

    private var parentAdapterPosition: Int = -1

    fun setParentAdapterPosition(position: Int) {
        parentAdapterPosition = position
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.child_region_image_view)
        val checkBox: CheckBox = itemView.findViewById(R.id.download_region_check)
        val downloadProgressBar: ProgressBar =
            itemView.findViewById(R.id.download_child_progress_bar)
        val regionNameTextView: TextView = itemView.findViewById(R.id.region_name_text_view)
        val progressTextView: TextView = itemView.findViewById(R.id.progress_text_view)
        val elementSize: TextView = itemView.findViewById(R.id.element_size)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.download_region_recycler_view_inner_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = regionEntity.regionList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val regionElement = regionEntity.regionList[position]

        if (regionElement.isDownloaded) {
            holder.imageView.apply {
                val color = ContextCompat.getColor(
                    context,
                    com.bytecause.core.resources.R.color.colorPrimary
                )
                setColorFilter(color)
            }
        } else {
            holder.checkBox.apply {
                visibility = View.VISIBLE
                isEnabled = regionElement.isCheckBoxEnabled
                isChecked = regionElement.isChecked

                setOnCheckedChangeListener { buttonView, isChecked ->
                    selectCheckBoxInterface.onCheckBoxClickListener(
                        buttonView = buttonView,
                        position = position,
                        parentPosition = parentAdapterPosition,
                        isChecked
                    )
                }
            }
        }

        when (regionElement.loading.isLoading) {
            true -> {
                holder.checkBox.visibility = View.GONE
                holder.downloadProgressBar.visibility = View.VISIBLE

                regionElement.loading.progress?.let { progress ->
                    holder.progressTextView.apply {
                        text =
                            holder.itemView.context.getString(com.bytecause.core.resources.R.string.processed_count)
                                .format(progress)
                        visibility = View.VISIBLE
                    }
                }
            }

            false -> {
                // If download is in progress, hide checkbox for regions, which are not being downloaded.
                if (isDownloadInProgress) {
                    holder.downloadProgressBar.visibility = View.GONE
                    holder.checkBox.visibility = View.INVISIBLE
                } else {
                    holder.downloadProgressBar.visibility = View.GONE
                    holder.checkBox.visibility =
                        if (regionElement.isDownloaded) View.INVISIBLE else View.VISIBLE
                    holder.progressTextView.visibility = View.GONE
                }
            }
        }

        holder.regionNameTextView.text =
            regionElement.regionEntity.names["name:${Locale.getDefault().language}"]
                ?: regionElement.regionEntity.names["name:en"]
                        ?: regionElement.regionEntity.names["name"]

        val size = regionElement.size
        if (size.isNotEmpty() && size != "") {
            // If size is available and not empty
            holder.elementSize.visibility = View.VISIBLE
            holder.elementSize.text = size
        } else {
            // If size is not available or empty
            holder.elementSize.visibility = View.GONE
        }
    }
}