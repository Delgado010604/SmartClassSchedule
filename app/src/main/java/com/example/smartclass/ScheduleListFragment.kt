package com.example.smartclass

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartclass.data.Schedule
import com.example.smartclass.databinding.FragmentScheduleListBinding

class ScheduleListFragment : Fragment() {

    private lateinit var binding: FragmentScheduleListBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var scheduleAdapter: ScheduleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScheduleListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dayOfWeek = arguments?.getInt(ARG_DAY_OF_WEEK) ?: 1

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        scheduleAdapter = ScheduleAdapter(::onEdit, ::onDelete, viewModel, viewLifecycleOwner, showTasks = true, dayFilter = dayOfWeek)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = scheduleAdapter
        }

        viewModel.getSchedulesForDay(dayOfWeek).observe(viewLifecycleOwner) {
            scheduleAdapter.submitList(it)
        }
    }

    private fun onEdit(schedule: Schedule) {
        val i = Intent(requireContext(), AddEditScheduleActivity::class.java)
        i.putExtra("id", schedule.id)
        startActivity(i)
    }

    private fun onDelete(schedule: Schedule) {
        val weekdays = schedule.weekdays.split(",").mapNotNull { it.toIntOrNull() }.toSet()

        AlertDialog.Builder(requireContext())
            .setTitle("Delete schedule?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.delete(schedule)

                if (weekdays.isNotEmpty()) {
                    AlarmHelper.cancelScheduleAlarm(requireContext(), schedule.id, weekdays)
                }
            }
            .setNegativeButton("No", null)
            .show()
        }

    companion object {
        private const val ARG_DAY_OF_WEEK = "day_of_week"

        fun newInstance(dayOfWeek: Int): ScheduleListFragment {
            val fragment = ScheduleListFragment()
            val args = Bundle()
            args.putInt(ARG_DAY_OF_WEEK, dayOfWeek)
            fragment.arguments = args
            return fragment
        }
    }
}
