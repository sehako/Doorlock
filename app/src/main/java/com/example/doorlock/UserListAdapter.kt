package com.example.doorlock

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserListAdapter(val context: Context, val userList: ArrayList<Users>) : RecyclerView.Adapter<UserListAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder?.bind(userList[position], context)
    }
    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.tv_name)
        val img = itemView.findViewById<ImageView>(R.id.img_photo)

        fun bind (userList: Users, context: Context) {
            if (userList.img != "") {
                val resourceId = context.resources.getIdentifier(userList.img, "drawable", context.packageName)
                img?.setImageResource(resourceId)
            } else {
                img?.setImageResource(R.mipmap.ic_launcher)
            }
            name.text = userList.name
        }
    }
}