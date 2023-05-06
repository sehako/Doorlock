package com.example.doorlock

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserRvAdapter(private val context: Context): RecyclerView.Adapter<UserRvAdapter.ViewHolder>(){

    var datas = mutableListOf<Users>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int  {
        return datas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val img: ImageView = itemView.findViewById(R.id.img_photo)

        fun bind(item: Users) {
            tvName.text = item.name
            tvDate.text = item.date
        }
    }
}