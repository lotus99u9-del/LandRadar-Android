package com.landradar.android.data

import android.content.Context
import org.json.JSONArray
import java.util.zip.GZIPInputStream

data class LocalizedName(
    val th: String,
    val en: String,
    val zh: String = en
)

data class SubdistrictOption(val code: String, val name: LocalizedName)

data class DistrictOption(
    val code: String,
    val name: LocalizedName,
    val subdistricts: List<SubdistrictOption>
)

data class ProvinceOption(
    val code: String,
    val name: LocalizedName,
    val districts: List<DistrictOption>
)

/**
 * Offline administrative options used by the prototype data set.
 * Codes are stable so changing the display language never changes the selection.
 * Replace/extend this list from the production administrative-area data source.
 */
object AdministrativeAreas {
    fun load(context: Context): List<ProvinceOption> {
        val rows = JSONArray(
            GZIPInputStream(context.assets.open("thailand_geography.json.gz"))
                .bufferedReader()
                .use { it.readText() }
        )
        val provinces = linkedMapOf<String, ProvinceBuilder>()
        for (index in 0 until rows.length()) {
            val row = rows.getJSONObject(index)
            val provinceCode = row.getInt("provinceCode").toString()
            val districtCode = row.getInt("districtCode").toString()
            val subdistrictCode = row.getInt("subdistrictCode").toString()
            val province = provinces.getOrPut(provinceCode) {
                ProvinceBuilder(provinceCode, LocalizedName(row.getString("provinceNameTh"), row.getString("provinceNameEn")))
            }
            val district = province.districts.getOrPut(districtCode) {
                DistrictBuilder(districtCode, LocalizedName(row.getString("districtNameTh"), row.getString("districtNameEn")))
            }
            district.subdistricts.putIfAbsent(
                subdistrictCode,
                SubdistrictOption(subdistrictCode, LocalizedName(row.getString("subdistrictNameTh"), row.getString("subdistrictNameEn")))
            )
        }
        return provinces.values.map { province ->
            ProvinceOption(
                province.code,
                province.name,
                province.districts.values.map { district ->
                    DistrictOption(district.code, district.name, district.subdistricts.values.toList())
                }
            )
        }
    }

    private data class ProvinceBuilder(
        val code: String,
        val name: LocalizedName,
        val districts: LinkedHashMap<String, DistrictBuilder> = linkedMapOf()
    )

    private data class DistrictBuilder(
        val code: String,
        val name: LocalizedName,
        val subdistricts: LinkedHashMap<String, SubdistrictOption> = linkedMapOf()
    )
}
