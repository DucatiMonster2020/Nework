package ru.netology.nework.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.netology.nework.activity.EventParticipantsFragment
import ru.netology.nework.activity.EventSpeakersFragment

class EventDetailPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> EventParticipantsFragment()
            1 -> EventSpeakersFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}