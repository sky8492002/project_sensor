package com.choi.sensorproject.ui.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.choi.sensorproject.ui.model.RecordsForHourUIModel
import com.example.sensorproject.databinding.ItemRecordsForHourBinding

// 화면에 보이는 시간대(리사이클러뷰 아이템들)의 데이터를 가져와 표시
class RecordsForHourAdapter(): PagingDataAdapter<RecordsForHourUIModel, RecordsForHourAdapter.RecordsForHourViewHolder>(
    diffUtil
){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecordsForHourViewHolder {
        val binding =
            ItemRecordsForHourBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordsForHourViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordsForHourViewHolder, position: Int) {
        getItem(position)?.let {
            holder.setItem(getItem(position))
        }
    }

    fun getRecordsForHourModel(position: Int): RecordsForHourUIModel{
        return getItem(position) as RecordsForHourUIModel
    }

    class RecordsForHourViewHolder(
        val binding: ItemRecordsForHourBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun setItem(item: RecordsForHourUIModel?) {
            binding.recordsForHourModel = item
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<RecordsForHourUIModel>() {
            override fun areItemsTheSame(
                oldItem: RecordsForHourUIModel,
                newItem: RecordsForHourUIModel
            ): Boolean {
                return oldItem.records == newItem.records
            }

            override fun areContentsTheSame(
                oldItem: RecordsForHourUIModel,
                newItem: RecordsForHourUIModel
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}