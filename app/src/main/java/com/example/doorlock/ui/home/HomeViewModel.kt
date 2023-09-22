package com.example.doorlock.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.doorlock.Users

class HomeViewModel : ViewModel() {
    private val userListLiveData = MutableLiveData<List<Users>>()

    fun getUserListLiveData(): LiveData<List<Users>> {
        return userListLiveData
    }

    // 사용자 데이터를 업데이트하는 메서드
    fun updateUserList(newList: List<Users>) {
        userListLiveData.value = newList
    }
}