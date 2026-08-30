package com.landradar.android.data

import android.content.Context

interface PropertyRepository {
    fun search(query: String): List<Property>
    fun savedIds(): Set<String>
    fun toggleSaved(propertyId: String)
}

/** Local prototype data; replace this implementation when the official API is ready. */
class LocalPropertyRepository(context: Context) : PropertyRepository {
    private val preferences = context.getSharedPreferences("landradar_properties", Context.MODE_PRIVATE)
    private val items = listOf(
        Property(
            id = "LR-001", title = "ที่ดินพร้อมสิ่งปลูกสร้าง",
            district = "เมืองเชียงใหม่", province = "เชียงใหม่",
            priceBaht = 1_850_000, areaRai = 0.75, auctionDate = "15 ก.ย. 2569",
            latitude = 18.7883, longitude = 98.9853,
            caseNumber = "ผบ.1234/2568", assetSequence = "ลำดับที่ 1",
            assetType = "ที่ดินพร้อมสิ่งปลูกสร้าง",
            address = "ถนนเชียงใหม่–ลำพูน", subdistrict = "หนองหอย",
            titleDeedNumber = "โฉนดเลขที่ 45821", appraisalPriceBaht = 2_150_000,
            auctionRound = "นัดที่ 2", legalExecutionOffice = "สำนักงานบังคับคดีจังหวัดเชียงใหม่",
            status = "เปิดขายทอดตลาด", updatedAt = "29 ส.ค. 2569"
        ),
        Property(
            id = "LR-002", title = "ที่ดินเปล่าใกล้ถนนหลัก",
            district = "บางบัวทอง", province = "นนทบุรี",
            priceBaht = 2_400_000, areaRai = 1.20, auctionDate = "22 ก.ย. 2569",
            latitude = 13.9260, longitude = 100.4107,
            caseNumber = "ผบ.778/2568", assetSequence = "ลำดับที่ 3",
            assetType = "ที่ดินเปล่า", address = "ใกล้ถนนบางกรวย–ไทรน้อย",
            subdistrict = "ละหาร", titleDeedNumber = "โฉนดเลขที่ 90112",
            appraisalPriceBaht = 2_780_000, auctionRound = "นัดที่ 1",
            legalExecutionOffice = "สำนักงานบังคับคดีจังหวัดนนทบุรี",
            status = "เปิดขายทอดตลาด", updatedAt = "28 ส.ค. 2569"
        ),
        Property(
            id = "LR-003", title = "บ้านเดี่ยวสองชั้น",
            district = "เมืองขอนแก่น", province = "ขอนแก่น",
            priceBaht = 1_290_000, areaRai = 0.18, auctionDate = "30 ก.ย. 2569",
            latitude = 16.4322, longitude = 102.8236,
            caseNumber = "ผบ.2045/2567", assetSequence = "ลำดับที่ 2",
            assetType = "บ้านเดี่ยวพร้อมที่ดิน", address = "ถนนมิตรภาพ",
            subdistrict = "ในเมือง", titleDeedNumber = "โฉนดเลขที่ 33709",
            appraisalPriceBaht = 1_650_000, auctionRound = "นัดที่ 4",
            legalExecutionOffice = "สำนักงานบังคับคดีจังหวัดขอนแก่น",
            status = "เปิดขายทอดตลาด", updatedAt = "27 ส.ค. 2569"
        )
    )

    override fun search(query: String): List<Property> {
        val normalized = query.trim()
        if (normalized.isEmpty()) return items
        return items.filter {
            it.title.contains(normalized, true) ||
                it.assetType.contains(normalized, true) ||
                it.district.contains(normalized, true) ||
                it.province.contains(normalized, true) ||
                it.caseNumber.contains(normalized, true)
        }
    }

    override fun savedIds(): Set<String> =
        preferences.getStringSet(KEY_SAVED, emptySet())?.toSet().orEmpty()

    override fun toggleSaved(propertyId: String) {
        val updated = savedIds().toMutableSet()
        if (!updated.add(propertyId)) updated.remove(propertyId)
        preferences.edit().putStringSet(KEY_SAVED, updated).apply()
    }

    private companion object { const val KEY_SAVED = "saved_property_ids" }
}
