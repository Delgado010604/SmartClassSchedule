package com.example.smartclass

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartclass.data.NotificationItem
import com.example.smartclass.databinding.ItemNotificationBinding
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter : ListAdapter<NotificationItem, NotificationAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notificationItem = getItem(position)
        holder.bind(notificationItem)
    }

    inner class ViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notificationItem: NotificationItem) {
            when (notificationItem) {
                is NotificationItem.ScheduleItem -> {
                    binding.tvSubject.text = notificationItem.schedule.subject
                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    binding.tvTime.text = sdf.format(Date(notificationItem.schedule.startTimeMillis))
                }
                is NotificationItem.TaskItem -> {
                    binding.tvSubject.text = notificationItem.task.description
                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    binding.tvTime.text = "Due: ${sdf.format(Date(notificationItem.task.dueDate))}"
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<NotificationItem>() {
            override fun areItemsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
                return when (oldItem) {
                    is NotificationItem.ScheduleItem -> newItem is NotificationItem.ScheduleItem && oldItem.schedule.id == newItem.schedule.id
                    is NotificationItem.TaskItem -> newItem is NotificationItem.TaskItem && oldItem.task.id == newItem.task.id
                }
            }

            override fun areContentsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
