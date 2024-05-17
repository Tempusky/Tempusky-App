package com.example.tempusky.core.helpers

import java.text.SimpleDateFormat
import java.util.Date

object Utils {

    fun timestampToDate(timestamp: String): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
        val netDate = Date(timestamp.toLong())
        return sdf.format(netDate)
    }

}