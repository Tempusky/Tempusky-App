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

    fun updateSearchDataResult(userInput: String){
        //Logic to search for data and update list and filter from data already stored
        val tempData = SearchDataResult("User", "Location", 25.0, 50.0, 1000.0, "2021-09-01")
        val tempList = _searchDatResult.value?.toMutableList() ?: mutableListOf()
        tempList.add(tempData)
        setSearchDataResult(tempList)
    }

}