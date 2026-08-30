package com.landradar.android.data

data class LocalizedName(
    val th: String,
    val en: String,
    val zh: String
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
    val provinces = listOf(
        ProvinceOption(
            code = "50",
            name = LocalizedName("เชียงใหม่", "Chiang Mai", "清迈"),
            districts = listOf(
                DistrictOption(
                    code = "5001",
                    name = LocalizedName("เมืองเชียงใหม่", "Mueang Chiang Mai", "清迈府治县"),
                    subdistricts = listOf(
                        SubdistrictOption("500105", LocalizedName("ช้างเผือก", "Chang Phueak", "昌普")),
                        SubdistrictOption("500107", LocalizedName("หนองหอย", "Nong Hoi", "农霍"))
                    )
                )
            )
        ),
        ProvinceOption(
            code = "12",
            name = LocalizedName("นนทบุรี", "Nonthaburi", "暖武里"),
            districts = listOf(
                DistrictOption(
                    code = "1204",
                    name = LocalizedName("บางบัวทอง", "Bang Bua Thong", "邦博通县"),
                    subdistricts = listOf(
                        SubdistrictOption("120403", LocalizedName("บางรักพัฒนา", "Bang Rak Phatthana", "邦拉帕塔纳")),
                        SubdistrictOption("120406", LocalizedName("ละหาร", "Lahan", "拉汉"))
                    )
                )
            )
        ),
        ProvinceOption(
            code = "40",
            name = LocalizedName("ขอนแก่น", "Khon Kaen", "孔敬"),
            districts = listOf(
                DistrictOption(
                    code = "4001",
                    name = LocalizedName("เมืองขอนแก่น", "Mueang Khon Kaen", "孔敬府治县"),
                    subdistricts = listOf(
                        SubdistrictOption("400101", LocalizedName("ในเมือง", "Nai Mueang", "奈孟")),
                        SubdistrictOption("400102", LocalizedName("สำราญ", "Samran", "三兰"))
                    )
                )
            )
        )
    )
}
