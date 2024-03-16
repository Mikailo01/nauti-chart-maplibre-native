package com.bytecause.nautichart.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bytecause.nautichart.R
import com.bytecause.nautichart.data.local.room.tables.Region
import com.bytecause.nautichart.interfaces.RecyclerViewOnClickListener
import java.util.Locale

class RegionsRecyclerViewAdapter(
    private val regionList: List<Region>,
    private val onItemViewClickListener: RecyclerViewOnClickListener
) : RecyclerView.Adapter<RegionsRecyclerViewAdapter.ViewHolder>() {

    private fun getLocaleNameOrDefault(names: Map<String, String>): String {
        // extracts name from object's properties, names in user's device language has higher precedence
        // if name for given language is not found, then another for-loop will look for default region name
        for ((key, value) in names) {
            if (key.startsWith("name:${Locale.getDefault().language}")) {
                return value
            }
        }

        for ((key, value) in names) {
            if (key == "name") {
                return value
            }
        }
        return ""
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.download_region_recycler_view_inner_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = regionList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val region = regionList[position]

        holder.continentName.text = getLocaleNameOrDefault(region.names)
        holder.imageView.setImageDrawable(
            ContextCompat.getDrawable(
                holder.itemView.context,
                R.drawable.earth_24
            )
        )

        holder.checkBox.setOnClickListener {
            onItemViewClickListener.onItemViewClickListener(holder.itemView, position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.place_image_view)
        val continentName: TextView = itemView.findViewById(R.id.place_name_text_view)
        val checkBox: CheckBox = itemView.findViewById(R.id.download_region_check)
        val downloadProgressBar: ProgressBar = itemView.findViewById(R.id.download_child_progress_bar)

    }

}