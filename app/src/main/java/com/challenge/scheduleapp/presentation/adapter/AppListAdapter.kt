package com.challenge.scheduleapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.challenge.scheduleapp.databinding.ItemAppBinding
import com.challenge.scheduleapp.domain.model.InstalledApp

class AppListAdapter(
    private val onAppClick: (InstalledApp) -> Unit
) : ListAdapter<InstalledApp, AppListAdapter.AppViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding =
            ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AppViewHolder(val binding: ItemAppBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(app: InstalledApp) {
            binding.tvAppName.text = app.appName
            app.icon?.let { binding.ivAppIcon.setImageDrawable(it) }

            itemView.setOnClickListener { onAppClick(app) }
        }
    }

    class AppDiffCallback : DiffUtil.ItemCallback<InstalledApp>() {
        override fun areItemsTheSame(oldItem: InstalledApp, newItem: InstalledApp): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: InstalledApp, newItem: InstalledApp): Boolean {
            return oldItem.packageName == newItem.packageName && oldItem.appName == newItem.appName
        }
    }
}