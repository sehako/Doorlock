package com.example.doorlock.ui.home

import android.R.attr.path
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doorlock.UserAddActivity
import com.example.doorlock.UserListAdapter
import com.example.doorlock.Users
import com.example.doorlock.databinding.FragmentHomeBinding
import java.io.File


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var userList = arrayListOf<Users>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        readPicture(requireContext())
        getImageFromFile()
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val fab: View = binding.fab
        val users: RecyclerView = binding.rvProfile
        val userAdapter = UserListAdapter(requireContext(), userList)
        val linearManager = LinearLayoutManager(requireContext())

        fab.setOnClickListener {
            val intent = Intent(requireContext(), UserAddActivity::class.java)
            startActivity(intent)
        }
        users.adapter = userAdapter
        users.layoutManager = linearManager

        userAdapter.setOnItemClickListener(object : UserListAdapter.OnItemClickListener {
            override fun onItemClick(v: View, data: Users, pos: Int) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("${data.name}을 수정하시겠습니까?")
                builder.setPositiveButton("확인") { dialog, which ->
                    val intent = Intent(requireContext(), UserAddActivity::class.java)
                    intent.putExtra("list", true)
                    intent.putExtra("userName", data.name)
                    intent.putExtra("userFace", data.img)
                    startActivity(intent)
                }
                builder.setNegativeButton("취소") { dialog, which ->
                    dialog.cancel()
                }
                val dialog = builder.create()
                dialog.show()
            }
        })

        userAdapter.setOnLongItemClickListener(object : UserListAdapter.OnItemLongClickListener {
            override fun onItemLongClick(v: View, data: Users, pos: Int) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("${data.name}을 삭제하시겠습니까?")
                builder.setPositiveButton("확인") { dialog, which ->
//                    Toast.makeText(requireContext(), "${getPathFromUri(data.img.toUri())}", Toast.LENGTH_LONG).show()
                    userAdapter.notifyItemRemoved(pos)
                    userList.removeAt(pos)
                    val file = File(data.img.toUri().path!!)
                    Toast.makeText(requireContext(), "${file}", Toast.LENGTH_LONG).show()
                    if (file.exists()) {
                        val file2 = File(file.absolutePath)
                        file2.delete()
                        Toast.makeText(context, "File deleted.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "File not exists", Toast.LENGTH_SHORT).show()
                    }
                }
                builder.setNegativeButton("취소") { dialog, which ->
                    dialog.cancel()
                }
                val dialog = builder.create()
                dialog.show()
            }
        })
        return root
    }
    // DCIM, PICTURE 디렉토리 이미지 읽어옴
    private fun readPicture(context:Context){
        userList.clear()
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.TITLE
        )

        val selection = "${Environment.DIRECTORY_PICTURES}=?"
        val selectionArgs: Array<String> = arrayOf("doorlock")
        val sortOrder = "${MediaStore.Images.ImageColumns.TITLE} ASC"
        val query = context.contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)
        query?.let { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.TITLE)
            while(cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(titleColumn)
                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                userList.add(Users(name, "$contentUri"))

                Log.e("MainActivity", "$id : $name : $contentUri")
            }
            cursor.close()
        }
    }

    private fun getImageFromFile() {
        userList.clear()
        val imageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/doorlock").path
        val file = File(imageDirectory)
        val files = file.listFiles()
        if(files != null) {
            for(onefile in files) {
                val fileUri = onefile.toURI().toString()
                userList.add(Users(onefile.name, fileUri))
            }
        }
    }

    fun getPathFromUri(uri: Uri?): String? {
        uri ?: return null

        val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)
        cursor?.moveToNext()

        val columnIndex = cursor?.getColumnIndex("_data")
        val path = if (columnIndex != null && columnIndex >= 0) {
            cursor.getString(columnIndex)
        } else {
            null
        }

        cursor?.close()
        return path
    }
}