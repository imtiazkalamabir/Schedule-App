package com.challenge.scheduleapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.challenge.scheduleapp.R
import com.challenge.scheduleapp.databinding.ItemScheduleBinding
import com.challenge.scheduleapp.domain.model.AppSchedule
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScheduleListAdapter(private val onEditClick: (AppSchedule) -> Unit,
                         private val onCancelClick: (AppSchedule) -> Unit,
                         private val onDeleteClick: (AppSchedule) -> Unit
) : ListAdapter<AppSchedule, ScheduleListAdapter.ScheduleViewHolder>(ScheduleDiffCallback()) {

    private var lastClickTime = 0L
    private val CLICK_BUFFER = 1000L // 1 second throttle

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun isClickAllowed(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < CLICK_BUFFER) {
            return false
        }
        lastClickTime = currentTime
        return true
    }

    inner class ScheduleViewHolder(val binding: ItemScheduleBinding) : RecyclerView.ViewHolder(binding.root) {
        private val appNameText: TextView = itemView.findViewById(R.id.tvAppName)
        private val scheduledTimeText: TextView = itemView.findViewById(R.id.tvScheduledTime)
        private val statusText: TextView = itemView.findViewById(R.id.tvStatus)
        private val editButton: Button = itemView.findViewById(R.id.btnEdit)
        private val cancelButton: Button = itemView.findViewById(R.id.btnCancel)
        private val deleteButton: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(schedule: AppSchedule) {
            binding.tvAppName.text = schedule.appName
            binding.tvScheduledTime.text =
                binding.root.context.getString(R.string.scheduled_time, formatTime(schedule.scheduledTime))
            binding.tvStatus.text =
                binding.root.context.getString(R.string.status, "")


            editButton.setOnClickListener {
                if (isClickAllowed()) {
                    onEditClick(schedule)
                }
            }
            cancelButton.setOnClickListener {
                if (isClickAllowed()) {
                    onCancelClick(schedule)
                }
            }
            deleteButton.setOnClickListener {
                if (isClickAllowed()) {
                    onDeleteClick(schedule)
                }
            }
        }

        private fun formatTime(timeInMillis: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timeInMillis))
        }
    }

    class ScheduleDiffCallback : DiffUtil.ItemCallback<AppSchedule>() {
        override fun areItemsTheSame(oldItem: AppSchedule, newItem: AppSchedule): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AppSchedule, newItem: AppSchedule): Boolean {
            // Compare only the fields that affect UI
            return oldItem.id == newItem.id &&
                    oldItem.appName == newItem.appName &&
                    oldItem.scheduledTime == newItem.scheduledTime &&
                    oldItem.status == newItem.status
        }
    }

}