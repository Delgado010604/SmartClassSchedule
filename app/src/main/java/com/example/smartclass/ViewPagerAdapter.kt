package com.example.smartclass

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 7

    override fun createFragment(position: Int): Fragment {
        val dayOfWeek = when (position) {
            0 -> 2 // Monday
            1 -> 3 // Tuesday
            2 -> 4 // Wednesday
            3 -> 5 // Thursday
            4 -> 6 // Friday
            5 -> 7 // Saturday
            else -> 1 // Sunday
        }
        return ScheduleListFragment.newInstance(dayOfWeek)
    }
}
