package com.example.doorlock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.doorlock.ui.home.HomeFragment

class UserListAdapter(private val context: Context, private val userList: ArrayList<Users>) : RecyclerView.Adapter<UserListAdapter.Holder>() {

    interface OnClickInterface {
        fun OnClick(view: View, position: Int)
    }

    interface OnLongClickInterface {
        fun OnLongClick(view: View, position: Int)
    }

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

            name.setOnClickListener{
            }

        }
    }
}