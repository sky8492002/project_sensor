package com.choi.sensorproject.ui.recyclerview
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.choi.sensorproject.ui.model.AppInfoUIModel
import com.choi.sensorproject.ui.setting.SettingClickHandler
import com.example.sensorproject.databinding.ItemAppInfoBinding

class AppInfoAdapter(
    private val settingClickHandler: SettingClickHandler
): ListAdapter<AppInfoUIModel, AppInfoAdapter.AppInfoViewHolder>(
    diffUtil
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppInfoViewHolder {
        val binding = ItemAppInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.settingClickHandler = settingClickHandler
        return AppInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppInfoViewHolder, position: Int) {
        getItem(position)?.let {
            holder.setItem(getItem(position))
        }
    }

    class AppInfoViewHolder(
        val binding: ItemAppInfoBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun setItem(item: AppInfoUIModel?) {
            binding.appInfoUIModel = item
        }
    }
    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<AppInfoUIModel>() {
            override fun areItemsTheSame(
                oldItem: AppInfoUIModel,
                newItem: AppInfoUIModel
            ): Boolean {
                return oldItem.appName == newItem.appName
            }

            override fun areContentsTheSame(
                oldItem: AppInfoUIModel,
                newItem: AppInfoUIModel
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}