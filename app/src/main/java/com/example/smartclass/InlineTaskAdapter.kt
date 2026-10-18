package com.example.smartclass

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartclass.data.Task
import com.example.smartclass.databinding.ItemInlineTaskBinding

class InlineTaskAdapter(
    private val onCompletedChanged: (Task, Boolean) -> Unit
) : ListAdapter<Task, InlineTaskAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInlineTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }

    inner class ViewHolder(private val binding: ItemInlineTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvDescription.text = task.description

            binding.cbCompleted.setOnCheckedChangeListener(null)
            binding.cbCompleted.isChecked = task.isCompleted

            if (task.isCompleted) {
                binding.tvDescription.paintFlags = binding.tvDescription.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvDescription.setTextColor(ContextCompat.getColor(itemView.context, R.color.secondary_text))
            } else {
                binding.tvDescription.paintFlags = binding.tvDescription.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvDescription.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary_text))

                if (task.dueDate < System.currentTimeMillis()) {
                    binding.tvDescription.setTextColor(ContextCompat.getColor(itemView.context, R.color.accent))
                }
            }

            binding.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                onCompletedChanged(task, isChecked)
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
