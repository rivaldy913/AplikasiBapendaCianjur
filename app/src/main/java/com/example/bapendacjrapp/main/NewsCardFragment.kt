package com.example.bapendacjrapp.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.bapendacjrapp.R
import com.bumptech.glide.Glide

class NewsCardFragment : Fragment() {

    companion object {
        private const val ARG_NEWS_ITEM = "news_item"

        fun newInstance(newsItem: BeritaPengumumanItem): NewsCardFragment {
            val fragment = NewsCardFragment()
            val args = Bundle()
            args.putParcelable(ARG_NEWS_ITEM, newsItem)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_news_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newsItem: BeritaPengumumanItem? = arguments?.getParcelable(ARG_NEWS_ITEM)

        if (newsItem != null) {
            val imageView: ImageView = view.findViewById(R.id.itemImage)
            val titleView: TextView = view.findViewById(R.id.itemTitle)
            val dateCategoryView: TextView = view.findViewById(R.id.itemDateCategory)
            val descriptionView: TextView = view.findViewById(R.id.itemDescription)

            if (newsItem.imageUrl != null && newsItem.imageUrl.startsWith("http")) {
                Glide.with(view.context)
                    .load(newsItem.imageUrl)
                    .placeholder(R.drawable.placeholder_news_image)
                    .into(imageView)
            } else {
                val imageResId = view.context.resources.getIdentifier(
                    newsItem.imageUrl, "drawable", view.context.packageName
                )
                if (imageResId != 0) {
                    imageView.setImageResource(imageResId)
                } else {
                    imageView.setImageResource(R.drawable.placeholder_news_image)
                }
            }

            titleView.text = newsItem.title
            dateCategoryView.text = "${newsItem.date} | ${newsItem.category}"
            descriptionView.text = newsItem.description

            view.setOnClickListener {
                val intent = Intent(context, DetailActivity::class.java).apply {
                    putExtra("title", newsItem.title)
                    putExtra("content", newsItem.description)
                    putExtra("image_res_id", -1)
                    putExtra("image_url", newsItem.imageUrl)
                }
                startActivity(intent)
            }
        }
    }
}