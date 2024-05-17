package com.example.tempusky.ui.screens.search

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tempusky.data.SearchDataResult
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date

class SearchViewModel : ViewModel() {

    private val _searchDatResult : MutableLiveData<List<SearchDataResult>> = MutableLiveData()
    val searchDataResult = _searchDatResult
    val db = Firebase.firestore

    fun setSearchDataResult(data: List<SearchDataResult>){
        _searchDatResult.value = data
    }

    fun updateSearchDataResult(userInput: String?) {
        if (userInput == null) {
            return
        }
        val dataList : List<SearchDataResult> = if (userInput.isBlank()) {
            getDataMatchingUserInput("")
        } else {
            getDataMatchingUserInput(userInput)
        }
        //Logic to search for data and update list and filter from data already stored
        Log.d("TAG", "DataList: $dataList")
        setSearchDataResult(dataList)
    }

    private fun getDataMatchingUserInput(userInput: String): List<SearchDataResult> {
        //Logic to search for data and return list
        db.collection("environment_sensors_data").get()
            .addOnSuccessListener { result ->
                val tempList = mutableListOf<SearchDataResult>()
                for (document in result) {
                    if (document.data["location"].toString().startsWith(userInput, true)) {
                        val tempData = SearchDataResult(
                            document.data["username"].toString(),
                            document.data["location"].toString(),
                            document.data["temperature"].toString().toDouble(),
                            document.data["humidity"].toString().toDouble(),
                            document.data["pressure"].toString().toDouble(),
                            timestampToDate(document.data["timestamp"].toString())
                        )
                        tempList.add(tempData)
                    }
                }
                tempList.sortByDescending { it.date }
                setSearchDataResult(tempList)
            }
        return _searchDatResult.value ?: listOf()
    }

    private fun timestampToDate(timestamp: String): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
        val netDate = Date(timestamp.toLong())
        return sdf.format(netDate)
    }

}