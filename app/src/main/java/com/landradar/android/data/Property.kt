package com.landradar.android.data

data class Property(
    val id: String,
    val title: String,
    val district: String,
    val province: String,
    val priceBaht: Long,
    val areaRai: Double,
    val auctionDate: String,
    val latitude: Double,
    val longitude: Double
)
