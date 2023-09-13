package com.example.doorlock

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class UserListAdapter(private val context: Context, private val userList: ArrayList<Users>) : RecyclerView.Adapter<UserListAdapter.Holder>() {
    private var listener : OnItemClickListener? = null
    private var longClickListener : OnItemLongClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(userList[position], context)
    }
    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name = itemView.findViewById<TextView>(R.id.tv_name)
        private val img = itemView.findViewById<ImageView>(R.id.img_photo)
        fun bind (userList: Users, context: Context) {
            if (userList.img != "") {
                Glide.with(itemView).load(userList.img).into(img)
            } else {
                img?.setImageResource(R.mipmap.ic_launcher)
            }
            name.text = userList.name
            itemView.setOnLongClickListener {
                true
            }
            val pos = adapterPosition
            if(pos!= RecyclerView.NO_POSITION)
            {
                itemView.setOnClickListener {
                    listener?.onItemClick(itemView, userList, pos)
                }
                itemView.setOnLongClickListener {
                    longClickListener?.onItemLongClick(itemView, userList, pos)
                    true
                }
            }
        }
    }
    interface OnItemClickListener{
        fun onItemClick(v:View, data: Users, pos : Int)
    }
    fun setOnItemClickListener(listener : OnItemClickListener) {
        this.listener = listener
    }
    interface OnItemLongClickListener {
        fun onItemLongClick(v: View, data: Users, pos: Int)
    }
    fun setOnLongItemClickListener(longClickListener: OnItemLongClickListener) {
        this.longClickListener = longClickListener
    }
}