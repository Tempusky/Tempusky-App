package com.example.tempusky

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private var _bottomBarVisibility = MutableLiveData(false)
    val bottomBarVisibility : LiveData<Boolean> = _bottomBarVisibility

    private var _isLoading = MutableLiveData(true)
    val isLoading : LiveData<Boolean> = _isLoading

    fun setLoading(v : Boolean){
        _isLoading.value = v
    }

    fun setBottomBarVisible(v : Boolean){
        _bottomBarVisibility.value = v
    }
}