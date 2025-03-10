package com.example.tradingproject

data class StockModel(
    val StockDetailID: String,
    val StockSymbol: String,
    val ClosePrice: String?,
    val ChangePercentage: String?,
    val FlagIcon: Int
)


