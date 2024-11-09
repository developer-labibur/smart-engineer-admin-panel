package com.rudhashi.seadminpanel.model

data class Book(
    val BCover: String = "",
    val BId: String = "",
    val BCode : String = "",
    val BName: String = "",
    val BTotalU: String = "",
    val BView: Long = 0,
    val xDep: List<String> = listOf(),  // Store as List<String> instead of List<Int>
    val xSem: List<String> = listOf()   // Store as List<String>
)
