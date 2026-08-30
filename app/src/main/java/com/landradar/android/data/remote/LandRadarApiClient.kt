package com.landradar.android.data.remote

import com.landradar.android.data.Property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

/**
 * Connector boundary for the LandRadar backend.
 *
 * Expected endpoint: GET {baseUrl}/v1/properties
 * No provider credential or server secret is stored in the Android app.
 */
class LandRadarApiClient(
    private val baseUrl: String,
    private val accessTokenProvider: () -> String? = { null }
) {
    suspend fun fetchProperties(): List<Property> = withContext(Dispatchers.IO) {
        require(baseUrl.startsWith("https://")) { "LandRadar API must use HTTPS" }
        val connection = (URL(baseUrl.trimEnd('/') + "/v1/properties").openConnection() as HttpURLConnection)
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("Accept", "application/json")
            accessTokenProvider()?.takeIf { it.isNotBlank() }?.let {
                connection.setRequestProperty("Authorization", "Bearer " + it)
            }
            val status = connection.responseCode
            if (status !in 200..299) throw LandRadarApiException(status)
            val payload = connection.inputStream.bufferedReader().use { it.readText() }
            parseProperties(payload)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseProperties(payload: String): List<Property> {
        val array = JSONArray(payload)
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    Property(
                        id = item.getString("id"),
                        title = item.getString("title"),
                        district = item.getString("district"),
                        province = item.getString("province"),
                        priceBaht = item.getLong("priceBaht"),
                        areaRai = item.getDouble("areaRai"),
                        auctionDate = item.getString("auctionDate"),
                        latitude = item.getDouble("latitude"),
                        longitude = item.getDouble("longitude"),
                        caseNumber = item.optString("caseNumber", "ยังไม่ระบุ"),
                        assetSequence = item.optString("assetSequence", "ยังไม่ระบุ"),
                        assetType = item.optString("assetType", "ยังไม่ระบุ"),
                        address = item.optString("address", "ยังไม่ระบุ"),
                        subdistrict = item.optString("subdistrict", "ยังไม่ระบุ"),
                        titleDeedNumber = item.optString("titleDeedNumber", "ยังไม่ระบุ"),
                        appraisalPriceBaht = if (item.has("appraisalPriceBaht") && !item.isNull("appraisalPriceBaht")) item.getLong("appraisalPriceBaht") else null,
                        auctionRound = item.optString("auctionRound", "ยังไม่ระบุ"),
                        legalExecutionOffice = item.optString("legalExecutionOffice", "ยังไม่ระบุ"),
                        status = item.optString("status", "รอตรวจสอบ"),
                        updatedAt = item.optString("updatedAt", "ยังไม่ระบุ")
                    )
                )
            }
        }
    }
}

class LandRadarApiException(val statusCode: Int) :
    Exception("LandRadar API request failed with HTTP " + statusCode)
