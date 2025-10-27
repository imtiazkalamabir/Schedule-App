package com.challenge.scheduleapp.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.challenge.scheduleapp.R
import com.challenge.scheduleapp.databinding.ItemScheduleBinding
import com.challenge.scheduleapp.domain.model.AppSchedule
import com.challenge.scheduleapp.domain.model.ScheduleStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScheduleListAdapter(
    private val onEditClick: (AppSchedule) -> Unit,
    private val onCancelClick: (AppSchedule) -> Unit,
    private val onDeleteClick: (AppSchedule) -> Unit
) : ListAdapter<AppSchedule, ScheduleListAdapter.ScheduleViewHolder>(ScheduleDiffCallback()) {

    private var lastClickTime = 0L
    private val CLICK_BUFFER = 1000L // 1 second buffer

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding =
            ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class ScheduleViewHolder(val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(schedule: AppSchedule) {
            binding.tvAppName.text = schedule.appName
            binding.tvScheduledTime.text =
                binding.root.context.getString(
                    R.string.scheduled_time,
                    formatTime(schedule.scheduledTime)
                )
            binding.tvStatus.text =
                binding.root.context.getString(R.string.status, schedule.status.name)

            // Set status color
            val statusColor = when (schedule.status) {
                ScheduleStatus.PENDING -> ContextCompat.getColor(
                    binding.root.context,
                    R.color.orange
                )

                ScheduleStatus.EXECUTED -> ContextCompat.getColor(
                    binding.root.context,
                    R.color.green
                )

                ScheduleStatus.CANCELLED -> ContextCompat.getColor(
                    binding.root.context,
                    R.color.gray
                )

                ScheduleStatus.FAILED -> ContextCompat.getColor(binding.root.context, R.color.red)
            }

            binding.tvStatus.setTextColor(statusColor)


            when (schedule.status) {
                ScheduleStatus.PENDING -> {
                    // For Pending: show edit and cancel buttons
                    binding.btnEdit.visibility = View.VISIBLE
                    binding.btnCancel.visibility = View.VISIBLE
                    binding.btnDelete.visibility = View.GONE
                }

                ScheduleStatus.EXECUTED, ScheduleStatus.CANCELLED, ScheduleStatus.FAILED -> {
                    // For Executed/Cancelled/Failed: show delete button only
                    binding.btnEdit.visibility = View.GONE
                    binding.btnCancel.visibility = View.GONE
                    binding.btnDelete.visibility = View.VISIBLE
                }
            }


            binding.btnEdit.setOnClickListener {
                if (isClickAllowed()) {
                    onEditClick(schedule)
                }
            }
            binding.btnCancel.setOnClickListener {
                if (isClickAllowed()) {
                    onCancelClick(schedule)
                }
            }
            binding.btnDelete.setOnClickListener {
                if (isClickAllowed()) {
                    onDeleteClick(schedule)
                }
            }
        }

        private fun formatTime(timeInMillis: Long): String {
            val sdf = SimpleDateFormat(
                binding.root.context.getString(R.string.time_pattern),
                Locale.getDefault()
            )
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