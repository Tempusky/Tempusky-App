package com.example.tempusky

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tempusky.data.SettingsValues

class MainViewModel : ViewModel() {

    private var _bottomBarVisibility = MutableLiveData(false)
    val bottomBarVisibility : LiveData<Boolean> = _bottomBarVisibility

    private var _isLoading = MutableLiveData(true)
    val isLoading : LiveData<Boolean> = _isLoading

    private var _isDarkTheme = MutableLiveData("")
    val appTheme : LiveData<String> = _isDarkTheme
    fun setLoading(v : Boolean){
        _isLoading.value = v
    }

    fun setAppTheme(v : String){
        if(v == SettingsValues.DEFAULT_THEME) return
        _isDarkTheme.value = v
    }

    fun setBottomBarVisible(v : Boolean){
        _bottomBarVisibility.value = v
    }
}