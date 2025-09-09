package com.example.bapendacjrapp.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class NewsPagerAdapter(fragmentActivity: FragmentActivity, private val newsList: List<BeritaPengumumanItem>) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = newsList.size

    override fun createFragment(position: Int): Fragment {
        return NewsCardFragment.newInstance(newsList[position])
    }
}