package com.bytecause.presentation.components.views.recyclerview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.bytecause.util.bindings.RecyclerViewBindingInterface


class GenericRecyclerViewAdapter<T : Any>(
    private var dataList: List<T>,
    @LayoutRes private val layoutRes: Int,
    private val bindingInterface: RecyclerViewBindingInterface<T>
) : RecyclerView.Adapter<GenericRecyclerViewAdapter.ViewHolder>() {

    fun updateContent(list: List<T>) {
        dataList = list
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun <T : Any> bind(
            item: T,
            bindingInterface: RecyclerViewBindingInterface<T>
        ) = bindingInterface.bindData(item, itemView, bindingAdapterPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element = dataList[position]
        holder.bind(element, bindingInterface)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}