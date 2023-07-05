package com.example.doorlock.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.doorlock.Users

class HomeViewModel : ViewModel() {

    private var userList = arrayListOf<Users>(
        Users("소순성", ""),
        Users("이종석", ""),
        Users("안진원", ""),
        Users("오세학", "")
    )

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text
}