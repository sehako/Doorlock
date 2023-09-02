package com.example.doorlock.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.doorlock.Users

class HomeViewModel : ViewModel() {
    val _userList = MutableLiveData<List<Users>>()
    val userList = arrayListOf<Users>()

    fun toggleUser() {
        _userList.value = userList
    }

    fun addUser(user: Users) {
        userList.add(user)
        _userList.value = userList
    }

    fun deleteUser(position: Int) {
        userList.removeAt(position)
        _userList.value = userList
    }
}