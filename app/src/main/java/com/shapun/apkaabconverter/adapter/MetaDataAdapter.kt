package com.shapun.apkaabconverter.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shapun.apkaabconverter.databinding.RowMetaDataBinding
import com.shapun.apkaabconverter.model.MetaData

class MetaDataAdapter(data: List<MetaData>) : RecyclerView.Adapter<MetaDataAdapter.ViewHolder>() {
    private val mData: List<MetaData> = data

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RowMetaDataBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvMetaFilePath.text = "Path: "+mData[position].path.toString()
        holder.binding.tvMetaDirectory.text= "Folder: "+mData[position].directoryName
        holder.binding.tvMetaFileName.text = "FileName: "+mData[position].fileName
    }

    override fun getItemCount(): Int = mData.size

    inner class ViewHolder(val binding: RowMetaDataBinding) : RecyclerView.ViewHolder(binding.root)
}