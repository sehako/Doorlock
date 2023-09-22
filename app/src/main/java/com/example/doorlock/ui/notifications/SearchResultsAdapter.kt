package com.example.doorlock.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doorlock.R

class SearchResultsAdapter(private var results: List<SearchResult>) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.search_result_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = results[position]
        holder.textViewName.text = result.name
        holder.textViewDate.text = result.date
    }

    override fun getItemCount(): Int {
        return results.size
    }


    fun updateData(newResults: List<SearchResult>) {
        results = newResults
        notifyDataSetChanged()
    }
}
