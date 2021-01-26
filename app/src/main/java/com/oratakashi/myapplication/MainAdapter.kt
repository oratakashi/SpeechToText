package com.oratakashi.myapplication

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.oratakashi.myapplication.databinding.AdapterMainBinding

class MainAdapter : RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            tvText.text = data[position]
        }
    }

    override fun getItemCount(): Int = data.size

    private val data : MutableList<String> = ArrayList()

    fun addItem(item : String){
        data.add(item)
        notifyItemInserted(data.indexOf(item))
    }

    class ViewHolder(val binding: AdapterMainBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            AdapterMainBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
            )
    )
}