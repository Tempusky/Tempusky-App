package com.example.tempusky.ui.screens.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tempusky.data.SearchDataResult

class SearchViewModel : ViewModel() {

    private val _searchDatResult : MutableLiveData<List<SearchDataResult>> = MutableLiveData()
    val searchDataResult = _searchDatResult

    fun setSearchDataResult(data: List<SearchDataResult>){
        _searchDatResult.value = data
    }
}