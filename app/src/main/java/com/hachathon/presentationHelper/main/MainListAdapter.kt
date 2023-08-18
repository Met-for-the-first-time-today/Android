package com.hachathon.presentationHelper.main

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hachathon.presentationHelper.databinding.ItemMainBinding
import com.hachathon.presentationHelper.detail.DetailActivity
import com.hachathon.presentationHelper.main.data.MainDataResult
import java.text.SimpleDateFormat
import java.util.Locale

class MainListAdapter :
    ListAdapter<MainDataResult, MainListAdapter.MainViewHolder>(diffUtil) {

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<MainDataResult>() {
            override fun areItemsTheSame(
                oldItem: MainDataResult,
                newItem: MainDataResult,
            ): Boolean {
                return oldItem.index == newItem.index
            }

            override fun areContentsTheSame(
                oldItem: MainDataResult,
                newItem: MainDataResult,
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class MainViewHolder(private var binding: ItemMainBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MainDataResult) {
            binding.apply {
                binding.mainItemTitle.text = data.title
                binding.mainItemDate.text = convertDateFormat(data.createdAt)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MainViewHolder {
        val binding =
            ItemMainBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bind(currentList[position])

        holder.itemView.setOnClickListener {
            val mIntent = Intent(holder.itemView.context, DetailActivity::class.java)
            mIntent.putExtra("index", currentList[position].index)
            holder.itemView.context.startActivity(mIntent)
        }
    }

    private fun convertDateFormat(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date)
    }
}
