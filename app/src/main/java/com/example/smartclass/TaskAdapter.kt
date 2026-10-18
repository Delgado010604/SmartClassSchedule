package com.example.smartclass

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartclass.data.Task
import com.example.smartclass.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onEdit: (Task) -> Unit,
    private val onCompletedChanged: (Task, Boolean) -> Unit
) : ListAdapter<Task, TaskAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }

    inner class ViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvTitle.text = task.description
            val sdf = SimpleDateFormat("EEE, MMM d, yyyy, hh:mm a", Locale.getDefault())
            binding.tvDueDate.text = "Due: ${sdf.format(Date(task.dueDate))}"
            binding.tvTaskType.text = task.taskType

            binding.cbCompleted.setOnCheckedChangeListener(null)
            binding.cbCompleted.isChecked = task.isCompleted

            if (task.isCompleted) {
                binding.tvTitle.paintFlags = binding.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.secondary_text))
            } else {
                binding.tvTitle.paintFlags = binding.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary_text))

                if (task.dueDate < System.currentTimeMillis()) {
                    binding.tvTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.accent))
                }
            }

            itemView.setOnClickListener { onEdit(task) }
            binding.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                onCompletedChanged(task, isChecked)
                if (isChecked) {
                    AlarmHelper.cancelTaskAlarm(itemView.context, task)
                } else {
                    AlarmHelper.scheduleTaskAlarm(itemView.context, task)
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem == newItem
            }
        }
    }
}
