package ru.netology.nework.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.netology.nework.activity.UserJobsFragment
import ru.netology.nework.activity.UserWallFragment

class UserProfilePagerAdapter(
    fragmentActivity: FragmentActivity,
    private val userId: Long
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UserWallFragment.newInstance(userId)
            1 -> UserJobsFragment.newInstance(userId)
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}