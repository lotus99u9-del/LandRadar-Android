package com.landradar.android.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.landradar.android.data.LocalPropertyRepository
import com.landradar.android.data.MarkerType
import com.landradar.android.data.Property
import java.text.NumberFormat
import java.util.Locale

private val Colors = lightColorScheme(
    primary = Color(0xFF006B3C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC5F2D2),
    secondary = Color(0xFF1557B0),
    secondaryContainer = Color(0xFFD5E3FF),
    tertiary = Color(0xFFB77900),
    tertiaryContainer = Color(0xFFFFDEA3),
    background = Color(0xFFF2F6F2),
    surface = Color.White
)

private enum class Language { TH, EN, ZH }
private enum class Tab { SEARCH, SAVED, ALERTS, ACCOUNT }
private enum class AssetFilter { ALL, LAND, HOUSE, COMMERCIAL, CONDO }
private enum class StatusFilter { ALL, OPEN, EXPIRING, CLOSED }
private enum class SortMode { LATEST, PRICE_LOW, PRICE_HIGH, AUCTION_SOON }

@Composable
fun LandRadarApp() {
    MaterialTheme(colorScheme = Colors) {
        Surface(Modifier.fillMaxSize()) { LandRadarHome() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LandRadarHome() {
    val context = LocalContext.current
    val repository = remember { LocalPropertyRepository(context) }
    var language by rememberSaveable { mutableStateOf(Language.TH) }
    var tab by rememberSaveable { mutableStateOf(Tab.SEARCH) }
    var query by rememberSaveable { mutableStateOf("") }
    var province by rememberSaveable { mutableStateOf("") }
    var district by rememberSaveable { mutableStateOf("") }
    var minPrice by rememberSaveable { mutableStateOf("") }
    var maxPrice by rememberSaveable { mutableStateOf("") }
    var minArea by rememberSaveable { mutableStateOf("") }
    var maxArea by rememberSaveable { mutableStateOf("") }
    var auctionDate by rememberSaveable { mutableStateOf("") }
    var assetFilter by rememberSaveable { mutableStateOf(AssetFilter.ALL) }
    var statusFilter by rememberSaveable { mutableStateOf(StatusFilter.ALL) }
    var sortMode by rememberSaveable { mutableStateOf(SortMode.LATEST) }
    var markerTypes by remember { mutableStateOf(MarkerType.entries.toSet()) }
    var showFilters by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Property?>(null) }
    var savedVersion by remember { mutableIntStateOf(0) }

    val savedIds = remember(savedVersion) { repository.savedIds() }
    val all = repository.search(query)
    val filtered = all.asSequence()
        .filter { it.markerType in markerTypes }
        .filter { province.isBlank() || it.province.contains(province, true) }
        .filter { district.isBlank() || it.district.contains(district, true) }
        .filter { minPrice.toLongOrNull()?.let { min -> it.priceBaht >= min } ?: true }
        .filter { maxPrice.toLongOrNull()?.let { max -> it.priceBaht <= max } ?: true }
        .filter { minArea.toDoubleOrNull()?.let { min -> it.areaRai >= min } ?: true }
        .filter { maxArea.toDoubleOrNull()?.let { max -> it.areaRai <= max } ?: true }
        .filter { auctionDate.isBlank() || it.auctionDate.contains(auctionDate, true) }
        .filter { assetMatches(it, assetFilter) }
        .filter { statusMatches(it, statusFilter) }
        .filter { tab != Tab.SAVED || it.id in savedIds }
        .toList()
        .let { list ->
            when (sortMode) {
                SortMode.LATEST -> list.sortedByDescending { it.updatedAt }
                SortMode.PRICE_LOW -> list.sortedBy { it.priceBaht }
                SortMode.PRICE_HIGH -> list.sortedByDescending { it.priceBaht }
                SortMode.AUCTION_SOON -> list.sortedBy { it.auctionDate }
            }
        }

    fun clearFilters() {
        province = ""; district = ""; minPrice = ""; maxPrice = ""
        minArea = ""; maxArea = ""; auctionDate = ""
        assetFilter = AssetFilter.ALL; statusFilter = StatusFilter.ALL
        sortMode = SortMode.LATEST; markerTypes = MarkerType.entries.toSet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("LandRadar", fontWeight = FontWeight.ExtraBold)
                        Text(tx(language, "เรดาร์ค้นหาทรัพย์", "Property discovery radar", "房产搜索雷达"), style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    LanguageMenu(language) { language = it }
                    Spacer(Modifier.width(8.dp))
                    BadgedBox(badge = { if (savedIds.isNotEmpty()) Badge { Text(savedIds.size.toString()) } }) {
                        Text("★", style = MaterialTheme.typography.titleLarge, color = Color(0xFFD4A423))
                    }
                    Spacer(Modifier.width(20.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                Tab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item; selected = null },
                        icon = { Text(tabIcon(item), style = MaterialTheme.typography.titleLarge) },
                        label = { Text(tabLabel(language, item)) }
                    )
                }
            }
        }
    ) { padding ->
        when {
            selected != null -> DetailScreen(
                property = selected!!,
                saved = selected!!.id in savedIds,
                language = language,
                onBack = { selected = null },
                onSave = { repository.toggleSaved(selected!!.id); savedVersion++ },
                modifier = Modifier.padding(padding)
            )
            tab == Tab.ALERTS -> AlertScreen(savedIds.size, language, Modifier.padding(padding))
            tab == Tab.ACCOUNT -> AccountScreen(language, Modifier.padding(padding))
            else -> SearchScreen(
                language = language,
                query = query,
                onQuery = { query = it },
                properties = filtered,
                savedIds = savedIds,
                markerTypes = markerTypes,
                onToggleMarker = { type ->
                    markerTypes = if (type in markerTypes) markerTypes - type else markerTypes + type
                },
                onMoreFilters = { showFilters = true },
                onClear = { clearFilters() },
                onOpen = { selected = it },
                onSave = { repository.toggleSaved(it.id); savedVersion++ },
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (showFilters) {
        FilterDialog(
            language = language,
            province = province, onProvince = { province = it },
            district = district, onDistrict = { district = it },
            minPrice = minPrice, onMinPrice = { minPrice = digits(it) },
            maxPrice = maxPrice, onMaxPrice = { maxPrice = digits(it) },
            minArea = minArea, onMinArea = { minArea = decimal(it) },
            maxArea = maxArea, onMaxArea = { maxArea = decimal(it) },
            auctionDate = auctionDate, onAuctionDate = { auctionDate = it },
            assetFilter = assetFilter, onAssetFilter = { assetFilter = it },
            statusFilter = statusFilter, onStatusFilter = { statusFilter = it },
            sortMode = sortMode, onSortMode = { sortMode = it },
            onClear = { clearFilters() },
            onDismiss = { showFilters = false }
        )
    }
}

@Composable
private fun LanguageMenu(language: Language, onChange: (Language) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Text(when (language) { Language.TH -> "ไทย"; Language.EN -> "EN"; Language.ZH -> "中文" })
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("ไทย") }, onClick = { onChange(Language.TH); expanded = false })
            DropdownMenuItem(text = { Text("English") }, onClick = { onChange(Language.EN); expanded = false })
            DropdownMenuItem(text = { Text("中文") }, onClick = { onChange(Language.ZH); expanded = false })
        }
    }
}

@Composable
private fun SearchScreen(
    language: Language,
    query: String,
    onQuery: (String) -> Unit,
    properties: List<Property>,
    savedIds: Set<String>,
    markerTypes: Set<MarkerType>,
    onToggleMarker: (MarkerType) -> Unit,
    onMoreFilters: () -> Unit,
    onClear: () -> Unit,
    onOpen: (Property) -> Unit,
    onSave: (Property) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            Text(tx(language, "ค้นหาทรัพย์ที่น่าสนใจ", "Find interesting properties", "查找优质房产"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text(tx(language, "ค้นหา บันทึก และติดตามในที่เดียว", "Search, save and track in one place", "一站式搜索、收藏和追踪"))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query, onValueChange = onQuery,
                leadingIcon = { Text("⌕") },
                placeholder = { Text(tx(language, "จังหวัด อำเภอ ประเภท หรือเลขคดี", "Province, district, type or case no.", "府、区、类型或案件编号")) },
                singleLine = true, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MarkerType.entries) { type ->
                    val selected = type in markerTypes
                    FilterChip(
                        selected = selected,
                        onClick = { onToggleMarker(type) },
                        label = { Text(markerLabel(language, type), fontWeight = FontWeight.Bold) },
                        leadingIcon = { Text("●", color = markerColor(type)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = markerColor(type).copy(alpha = 0.18f),
                            selectedLabelColor = markerColor(type)
                        )
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onMoreFilters, modifier = Modifier.weight(1f)) {
                    Text("☰ " + tx(language, "ตัวกรองเพิ่มเติม", "More filters", "更多筛选"))
                }
                OutlinedButton(onClick = onClear) { Text(tx(language, "ล้าง", "Clear", "清除")) }
            }
            Spacer(Modifier.height(10.dp))
            PropertyMap(properties, onOpen, Modifier.fillMaxWidth().height(180.dp))
            Spacer(Modifier.height(8.dp))
            MarkerLegend(language)
            Spacer(Modifier.height(12.dp))
            Text(tx(language, "พบ ", "Found ", "找到 ") + properties.size + tx(language, " รายการ", " properties", " 个房产"), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }
        if (properties.isEmpty()) item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Text(tx(language, "ไม่พบทรัพย์ ลองเปลี่ยนตัวกรอง", "No properties found. Try changing filters.", "未找到房产，请更改筛选条件。"), Modifier.padding(24.dp))
            }
        }
        items(properties, key = { it.id }) { property ->
            PropertyCard(property, property.id in savedIds, language, onOpen, onSave)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDialog(
    language: Language,
    province: String, onProvince: (String) -> Unit,
    district: String, onDistrict: (String) -> Unit,
    minPrice: String, onMinPrice: (String) -> Unit,
    maxPrice: String, onMaxPrice: (String) -> Unit,
    minArea: String, onMinArea: (String) -> Unit,
    maxArea: String, onMaxArea: (String) -> Unit,
    auctionDate: String, onAuctionDate: (String) -> Unit,
    assetFilter: AssetFilter, onAssetFilter: (AssetFilter) -> Unit,
    statusFilter: StatusFilter, onStatusFilter: (StatusFilter) -> Unit,
    sortMode: SortMode, onSortMode: (SortMode) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text(tx(language, "ตัวกรองทั้งหมด", "All filters", "全部筛选"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Text(tx(language, "เลือกเฉพาะที่จำเป็น", "Choose only what you need", "仅选择需要的条件"))
            }
            item {
                Text(tx(language, "ประเภททรัพย์", "Property type", "房产类型"), fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AssetFilter.entries) { value ->
                        FilterChip(selected = assetFilter == value, onClick = { onAssetFilter(value) }, label = { Text(assetLabel(language, value)) })
                    }
                }
            }
            item {
                OutlinedTextField(province, onProvince, label = { Text(tx(language, "จังหวัด", "Province", "府/省")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(district, onDistrict, label = { Text(tx(language, "อำเภอ/เขต", "District", "区/县")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            item {
                Text(tx(language, "ช่วงราคา (บาท)", "Price range (THB)", "价格范围（泰铢）"), fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(minPrice, onMinPrice, label = { Text(tx(language, "ต่ำสุด", "Min", "最低")) }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(maxPrice, onMaxPrice, label = { Text(tx(language, "สูงสุด", "Max", "最高")) }, singleLine = true, modifier = Modifier.weight(1f))
                }
            }
            item {
                Text(tx(language, "ขนาดเนื้อที่ (ไร่)", "Area range (rai)", "面积范围（莱）"), fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(minArea, onMinArea, label = { Text(tx(language, "ต่ำสุด", "Min", "最低")) }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(maxArea, onMaxArea, label = { Text(tx(language, "สูงสุด", "Max", "最高")) }, singleLine = true, modifier = Modifier.weight(1f))
                }
            }
            item {
                OutlinedTextField(auctionDate, onAuctionDate, label = { Text(tx(language, "วันขายทอดตลาด", "Auction date", "拍卖日期")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            item {
                Text(tx(language, "สถานะ", "Status", "状态"), fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(StatusFilter.entries) { value ->
                        FilterChip(selected = statusFilter == value, onClick = { onStatusFilter(value) }, label = { Text(statusLabel(language, value)) })
                    }
                }
            }
            item {
                Text(tx(language, "เรียงลำดับ", "Sort by", "排序方式"), fontWeight = FontWeight.Bold)
                SortMode.entries.forEach { value ->
                    Row(Modifier.fillMaxWidth().clickable { onSortMode(value) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = sortMode == value, onClick = { onSortMode(value) })
                        Text(sortLabel(language, value))
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onClear, modifier = Modifier.weight(1f)) { Text(tx(language, "ล้างทั้งหมด", "Clear all", "全部清除")) }
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text(tx(language, "ดูผลลัพธ์", "Show results", "查看结果")) }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PropertyCard(property: Property, saved: Boolean, language: Language, onOpen: (Property) -> Unit, onSave: (Property) -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth().clickable { onOpen(property) }) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = markerColor(property.markerType).copy(alpha = 0.14f), shape = MaterialTheme.shapes.medium, modifier = Modifier.size(72.dp)) {
                Box(contentAlignment = Alignment.Center) { Text("⌖", style = MaterialTheme.typography.headlineLarge, color = markerColor(property.markerType)) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(markerLabel(language, property.markerType), color = markerColor(property.markerType), fontWeight = FontWeight.ExtraBold)
                Text(property.title, fontWeight = FontWeight.Bold)
                Text(property.district + " • " + property.province)
                Text(money(property.priceBaht) + " " + tx(language, "บาท", "THB", "泰铢"), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                Text(property.areaRai.toString() + " " + tx(language, "ไร่", "rai", "莱") + " • " + property.auctionDate, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { onSave(property) }) { Text(if (saved) "★" else "☆", style = MaterialTheme.typography.headlineSmall, color = Color(0xFFD4A423)) }
        }
    }
}

@Composable
private fun DetailScreen(property: Property, saved: Boolean, language: Language, onBack: () -> Unit, onSave: () -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            TextButton(onClick = onBack) { Text("‹ " + tx(language, "กลับ", "Back", "返回")) }
            PropertyMap(listOf(property), {}, Modifier.fillMaxWidth().height(220.dp))
            Spacer(Modifier.height(14.dp))
            Text(markerLabel(language, property.markerType), color = markerColor(property.markerType), fontWeight = FontWeight.ExtraBold)
            Text(property.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text(property.subdistrict + " • " + property.district + " • " + property.province)
            Spacer(Modifier.height(12.dp))
            DetailSection(tx(language, "ราคาและเนื้อที่", "Price and area", "价格与面积")) {
                DetailLine(tx(language, "ราคาตั้งขาย", "Starting price", "起拍价"), money(property.priceBaht))
                DetailLine(tx(language, "ราคาประเมิน", "Appraisal price", "评估价"), property.appraisalPriceBaht?.let(::money) ?: "-")
                DetailLine(tx(language, "เนื้อที่", "Area", "面积"), property.areaRai.toString() + " " + tx(language, "ไร่", "rai", "莱"))
            }
            Spacer(Modifier.height(10.dp))
            DetailSection(tx(language, "ข้อมูลคดีและทรัพย์", "Case and property", "案件与房产")) {
                DetailLine(tx(language, "เลขคดี", "Case number", "案件编号"), property.caseNumber)
                DetailLine(tx(language, "ลำดับทรัพย์", "Asset sequence", "资产序号"), property.assetSequence)
                DetailLine(tx(language, "ประเภท", "Type", "类型"), property.assetType)
                DetailLine(tx(language, "โฉนด", "Title deed", "地契"), property.titleDeedNumber)
            }
            Spacer(Modifier.height(10.dp))
            DetailSection(tx(language, "การขายทอดตลาด", "Auction", "拍卖")) {
                DetailLine(tx(language, "รอบขาย", "Auction round", "拍卖轮次"), property.auctionRound)
                DetailLine(tx(language, "วันขาย", "Auction date", "拍卖日期"), property.auctionDate)
                DetailLine(tx(language, "สำนักงาน", "Office", "执行办公室"), property.legalExecutionOffice)
                DetailLine(tx(language, "อัปเดต", "Updated", "更新时间"), property.updatedAt)
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(if (saved) "★ " + tx(language, "บันทึกแล้ว", "Saved", "已收藏") else "☆ " + tx(language, "บันทึกและติดตาม", "Save and track", "收藏并追踪"))
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(tx(language, "ดูการวิเคราะห์บนเว็บไซต์", "View analysis on website", "在网站查看分析"))
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp)); content()
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
        Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AlertScreen(savedCount: Int, language: Language, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text(tx(language, "การแจ้งเตือน", "Notifications", "通知"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(12.dp))
        ElevatedCard(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🔔", style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.width(12.dp))
                Column {
                    Text(tx(language, "ทรัพย์ที่ติดตาม ", "Tracked properties ", "追踪房产 ") + savedCount, fontWeight = FontWeight.Bold)
                    Text(tx(language, "แจ้งเมื่อราคา วันขาย หรือสถานะเปลี่ยน", "Alerts for price, auction date or status changes", "价格、拍卖日期或状态变更时通知"))
                }
            }
        }
    }
}

@Composable
private fun AccountScreen(language: Language, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text(tx(language, "บัญชีผู้ใช้", "Account", "用户账户"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(12.dp))
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                DetailLine(tx(language, "ชื่อผู้ใช้", "Username", "用户名"), "LandRadar User")
                DetailLine(tx(language, "โพสต์หมดอายุ", "Post expiry", "发布到期日"), "30 ก.ย. 2569")
                DetailLine(tx(language, "แพ็กเกจหมดอายุ", "Package expiry", "套餐到期日"), "31 ธ.ค. 2569")
                Spacer(Modifier.height(10.dp))
                Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text(tx(language, "ต่ออายุ", "Renew", "续费")) }
                OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text(tx(language, "ออกจากระบบ", "Sign out", "退出登录")) }
            }
        }
    }
}

@Composable
private fun MarkerLegend(language: Language) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        LazyRow(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(MarkerType.entries) { type ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("●", color = markerColor(type), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(4.dp)); Text(markerLabel(language, type), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

private fun assetMatches(property: Property, filter: AssetFilter): Boolean = when (filter) {
    AssetFilter.ALL -> true
    AssetFilter.LAND -> property.assetType.contains("ที่ดิน") || property.title.contains("ที่ดิน")
    AssetFilter.HOUSE -> property.assetType.contains("บ้าน") || property.title.contains("บ้าน")
    AssetFilter.COMMERCIAL -> property.assetType.contains("อาคาร") || property.title.contains("อาคาร")
    AssetFilter.CONDO -> property.assetType.contains("คอนโด") || property.title.contains("คอนโด")
}

private fun statusMatches(property: Property, filter: StatusFilter): Boolean = when (filter) {
    StatusFilter.ALL -> true
    StatusFilter.OPEN -> property.status.contains("เปิด")
    StatusFilter.EXPIRING -> property.status.contains("ใกล้") || property.status.contains("หมดอายุ")
    StatusFilter.CLOSED -> property.status.contains("ปิด")
}

private fun markerColor(type: MarkerType): Color = when (type) {
    MarkerType.LEGAL_EXECUTION -> Color(0xFF191919)
    MarkerType.FOR_SALE -> Color(0xFF1557B0)
    MarkerType.PRIME_LOCATION -> Color(0xFFD4A423)
}

private fun markerLabel(l: Language, type: MarkerType): String = when (type) {
    MarkerType.LEGAL_EXECUTION -> tx(l, "บังคับคดี", "Legal execution", "法院执行")
    MarkerType.FOR_SALE -> tx(l, "ฝากขาย", "For sale", "委托出售")
    MarkerType.PRIME_LOCATION -> tx(l, "ทำเลทอง", "Prime location", "黄金地段")
}

private fun tabLabel(l: Language, tab: Tab): String = when (tab) {
    Tab.SEARCH -> tx(l, "ค้นหา", "Search", "搜索")
    Tab.SAVED -> tx(l, "บันทึก", "Saved", "收藏")
    Tab.ALERTS -> tx(l, "แจ้งเตือน", "Alerts", "通知")
    Tab.ACCOUNT -> tx(l, "บัญชี", "Account", "账户")
}

private fun tabIcon(tab: Tab) = when (tab) { Tab.SEARCH -> "⌕"; Tab.SAVED -> "★"; Tab.ALERTS -> "●"; Tab.ACCOUNT -> "☺" }
private fun assetLabel(l: Language, v: AssetFilter) = when (v) {
    AssetFilter.ALL -> tx(l, "ทั้งหมด", "All", "全部")
    AssetFilter.LAND -> tx(l, "ที่ดิน", "Land", "土地")
    AssetFilter.HOUSE -> tx(l, "บ้าน", "House", "住宅")
    AssetFilter.COMMERCIAL -> tx(l, "อาคารพาณิชย์", "Commercial", "商业楼")
    AssetFilter.CONDO -> tx(l, "คอนโด", "Condo", "公寓")
}
private fun statusLabel(l: Language, v: StatusFilter) = when (v) {
    StatusFilter.ALL -> tx(l, "ทั้งหมด", "All", "全部")
    StatusFilter.OPEN -> tx(l, "เปิดขาย", "Open", "出售中")
    StatusFilter.EXPIRING -> tx(l, "ใกล้หมดอายุ", "Expiring", "即将到期")
    StatusFilter.CLOSED -> tx(l, "ปิดแล้ว", "Closed", "已关闭")
}
private fun sortLabel(l: Language, v: SortMode) = when (v) {
    SortMode.LATEST -> tx(l, "อัปเดตล่าสุด", "Latest", "最新")
    SortMode.PRICE_LOW -> tx(l, "ราคาต่ำสุด", "Lowest price", "价格最低")
    SortMode.PRICE_HIGH -> tx(l, "ราคาสูงสุด", "Highest price", "价格最高")
    SortMode.AUCTION_SOON -> tx(l, "ใกล้ขายที่สุด", "Auction soonest", "最近拍卖")
}

private fun tx(l: Language, th: String, en: String, zh: String) = when (l) { Language.TH -> th; Language.EN -> en; Language.ZH -> zh }
private fun digits(value: String) = value.filter(Char::isDigit)
private fun decimal(value: String) = value.filter { it.isDigit() || it == '.' }.let { if (it.count { c -> c == '.' } <= 1) it else it.dropLast(1) }
private fun money(value: Long): String = NumberFormat.getNumberInstance(Locale("th", "TH")).format(value)
