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
    val longitude: Double,
    val caseNumber: String = "ยังไม่ระบุ",
    val assetSequence: String = "ยังไม่ระบุ",
    val assetType: String = "ยังไม่ระบุ",
    val address: String = "ยังไม่ระบุ",
    val subdistrict: String = "ยังไม่ระบุ",
    val titleDeedNumber: String = "ยังไม่ระบุ",
    val appraisalPriceBaht: Long? = null,
    val auctionRound: String = "ยังไม่ระบุ",
    val legalExecutionOffice: String = "ยังไม่ระบุ",
    val status: String = "รอตรวจสอบ",
    val updatedAt: String = "ยังไม่ระบุ"
)
