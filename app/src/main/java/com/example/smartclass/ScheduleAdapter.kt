package com.example.smartclass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartclass.data.Schedule
import com.example.smartclass.databinding.ItemScheduleBinding
import java.text.SimpleDateFormat
import java.util.*

class ScheduleAdapter(
    private val onEdit: (Schedule) -> Unit,
    private val onDelete: (Schedule) -> Unit,
    private val viewModel: MainViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val showTasks: Boolean = true,
    private val dayFilter: Int? = null
) : ListAdapter<Schedule, ScheduleAdapter.VH>(DIFF) {

    class VH(val binding: ItemScheduleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH {
        val binding = ItemScheduleBinding.inflate(LayoutInflater.from(p.context), p, false)
        return VH(binding)
    }

    override fun onBindViewHolder(h: VH, i: Int) {
        val schedule = getItem(i)
        h.binding.tvSubject.text = schedule.subject
        h.binding.tvTime.text = "${formatTime(schedule.startTimeMillis)} - ${formatTime(schedule.endTimeMillis)}"
        h.binding.tvRoom.text = schedule.room
        h.binding.tvInstructor.text = schedule.instructor

        if (showTasks) {
            val taskAdapter = InlineTaskAdapter { task, isCompleted ->
                viewModel.saveTask(task.copy(isCompleted = isCompleted)) {}
            }
            h.binding.rvTasks.apply {
                layoutManager = LinearLayoutManager(h.itemView.context)
                adapter = taskAdapter
            }

            viewModel.getTasksForSchedule(schedule.id).observe(lifecycleOwner) { tasks ->
                val filteredTasks = if (dayFilter != null) {
                    tasks.filter {
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = it.dueDate
                        cal.get(Calendar.DAY_OF_WEEK) == dayFilter && it.dueDate >= System.currentTimeMillis()
                    }
                } else {
                    tasks
                }
                taskAdapter.submitList(filteredTasks)
                h.binding.rvTasks.visibility = if (filteredTasks.isEmpty()) View.GONE else View.VISIBLE
            }
        } else {
            h.binding.rvTasks.visibility = View.GONE
        }

        h.itemView.setOnClickListener { onEdit(schedule) }
        h.itemView.setOnLongClickListener {
            onDelete(schedule)
            true
        }
    }

    private fun formatTime(time: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(time))
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Schedule>() {
            override fun areItemsTheSame(a: Schedule, b: Schedule) = a.id == b.id
            override fun areContentsTheSame(a: Schedule, b: Schedule) = a == b
        }
    }
}
