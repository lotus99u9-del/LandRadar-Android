package com.landradar.android.data

interface PropertyRepository {
    fun search(query: String): List<Property>
    fun savedIds(): Set<String>
    fun toggleSaved(propertyId: String)
}

/** Preview data is isolated so an API implementation can replace it later. */
class PreviewPropertyRepository : PropertyRepository {
    private val saved = mutableSetOf<String>()
    private val items = listOf(
        Property("LR-001", "ที่ดินพร้อมสิ่งปลูกสร้าง", "เมืองเชียงใหม่", "เชียงใหม่", 1_850_000, 0.75, "15 ก.ย. 2569", 18.7883, 98.9853),
        Property("LR-002", "ที่ดินเปล่าใกล้ถนนหลัก", "บางบัวทอง", "นนทบุรี", 2_400_000, 1.20, "22 ก.ย. 2569", 13.9260, 100.4107),
        Property("LR-003", "บ้านเดี่ยวสองชั้น", "เมืองขอนแก่น", "ขอนแก่น", 1_290_000, 0.18, "30 ก.ย. 2569", 16.4322, 102.8236)
    )

    override fun search(query: String): List<Property> {
        val normalized = query.trim()
        if (normalized.isEmpty()) return items
        return items.filter {
            it.title.contains(normalized, ignoreCase = true) ||
                it.district.contains(normalized, ignoreCase = true) ||
                it.province.contains(normalized, ignoreCase = true)
        }
    }

    override fun savedIds(): Set<String> = saved.toSet()

    override fun toggleSaved(propertyId: String) {
        if (!saved.add(propertyId)) saved.remove(propertyId)
    }
}
