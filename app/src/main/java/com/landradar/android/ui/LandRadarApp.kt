package com.landradar.android.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.landradar.android.data.LocalPropertyRepository
import com.landradar.android.data.Property
import java.text.NumberFormat
import java.util.Locale

private val Colors = lightColorScheme(
    primary = Color(0xFF176B3A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9F2DF),
    background = Color(0xFFF5F7F4),
    surface = Color.White
)

private enum class Tab(val title: String, val icon: String) {
    SEARCH("ค้นหา", "⌕"), SAVED("บันทึก", "★"), ALERTS("แจ้งเตือน", "●")
}

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
    var tab by rememberSaveable { mutableStateOf(Tab.SEARCH) }
    var query by rememberSaveable { mutableStateOf("") }
    var province by rememberSaveable { mutableStateOf("ทุกจังหวัด") }
    var maxPrice by rememberSaveable { mutableStateOf<Long?>(null) }
    var selected by remember { mutableStateOf<Property?>(null) }
    var savedVersion by remember { mutableIntStateOf(0) }
    val savedIds = remember(savedVersion) { repository.savedIds() }
    val results = repository.search(query).filter {
        (province == "ทุกจังหวัด" || it.province == province) &&
            (maxPrice == null || it.priceBaht <= maxPrice!!) &&
            (tab != Tab.SAVED || it.id in savedIds)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("LandRadar", fontWeight = FontWeight.Bold)
                        Text("เรดาร์ค้นหาทรัพย์", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    BadgedBox(badge = { if (savedIds.isNotEmpty()) Badge { Text(savedIds.size.toString()) } }) {
                        Text("★", style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(Modifier.width(24.dp))
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
                        icon = { Text(item.icon, style = MaterialTheme.typography.titleLarge) },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { padding ->
        if (selected != null) {
            DetailScreen(
                property = selected!!,
                saved = selected!!.id in savedIds,
                onBack = { selected = null },
                onSave = {
                    repository.toggleSaved(selected!!.id)
                    savedVersion++
                },
                modifier = Modifier.padding(padding)
            )
        } else if (tab == Tab.ALERTS) {
            AlertScreen(savedIds.size, Modifier.padding(padding))
        } else {
            SearchScreen(
                query = query,
                onQuery = { query = it },
                province = province,
                onProvince = { province = it },
                maxPrice = maxPrice,
                onMaxPrice = { maxPrice = it },
                properties = results,
                savedIds = savedIds,
                onOpen = { selected = it },
                onSave = {
                    repository.toggleSaved(it.id)
                    savedVersion++
                },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(
    query: String,
    onQuery: (String) -> Unit,
    province: String,
    onProvince: (String) -> Unit,
    maxPrice: Long?,
    onMaxPrice: (Long?) -> Unit,
    properties: List<Property>,
    savedIds: Set<String>,
    onOpen: (Property) -> Unit,
    onSave: (Property) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Text("ค้นหาทรัพย์ที่น่าสนใจ", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("ค้นหาและบันทึกไว้ แล้วดูข้อมูลเชิงลึกต่อบนเว็บไซต์")
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = query,
                onValueChange = onQuery,
                leadingIcon = { Text("⌕") },
                placeholder = { Text("จังหวัด อำเภอ หรือประเภททรัพย์") },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Text("จังหวัด", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("ทุกจังหวัด", "เชียงใหม่", "นนทบุรี", "ขอนแก่น")) { value ->
                    FilterChip(
                        selected = province == value,
                        onClick = { onProvince(value) },
                        label = { Text(value) }
                    )
                }
            }
            Text("ราคาไม่เกิน", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf<Pair<String, Long?>>("ทั้งหมด" to null, "1.5 ล้าน" to 1_500_000, "2 ล้าน" to 2_000_000, "3 ล้าน" to 3_000_000)) { option ->
                    FilterChip(
                        selected = maxPrice == option.second,
                        onClick = { onMaxPrice(option.second) },
                        label = { Text(option.first) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            PropertyMap(
                properties = properties,
                onPropertyClick = onOpen,
                modifier = Modifier.fillMaxWidth().height(180.dp)
            )
            Spacer(Modifier.height(14.dp))
            Text("พบ " + properties.size + " รายการ", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }
        if (properties.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Text("ไม่พบทรัพย์ ลองเปลี่ยนตัวกรอง", Modifier.padding(24.dp))
                }
            }
        }
        items(properties, key = { it.id }) { property ->
            PropertyCard(property, property.id in savedIds, onOpen, onSave)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun PropertyCard(
    property: Property,
    saved: Boolean,
    onOpen: (Property) -> Unit,
    onSave: (Property) -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth().clickable { onOpen(property) }) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("🏠", style = MaterialTheme.typography.headlineLarge)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(property.title, fontWeight = FontWeight.Bold)
                Text(property.district + " • " + property.province)
                Text(money(property.priceBaht) + " บาท", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(property.areaRai.toString() + " ไร่ • " + property.auctionDate, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { onSave(property) }) {
                Text(if (saved) "★" else "☆", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun DetailScreen(
    property: Property,
    saved: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            TextButton(onClick = onBack) { Text("‹ กลับไปหน้าค้นหา") }
            PropertyMap(
                properties = listOf(property),
                onPropertyClick = {},
                modifier = Modifier.fillMaxWidth().height(240.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(property.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(property.district + ", " + property.province)
            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            DetailLine("รหัสทรัพย์", property.id)
            DetailLine("ราคาตั้งต้น", money(property.priceBaht) + " บาท")
            DetailLine("ขนาด", property.areaRai.toString() + " ไร่")
            DetailLine("วันขายทอดตลาด", property.auctionDate)
            Spacer(Modifier.height(18.dp))
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(if (saved) "★ บันทึกแล้ว" else "☆ บันทึกทรัพย์นี้")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("ดูข้อมูลเชิงลึกบนเว็บไซต์หลัก")
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp)) {
        Text(label, Modifier.weight(1f), color = Color.Gray)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AlertScreen(savedCount: Int, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text("การแจ้งเตือน", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("ติดตามเฉพาะเรื่องสำคัญ ไม่รบกวนเกินจำเป็น")
        Spacer(Modifier.height(16.dp))
        ElevatedCard(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🔔", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("ทรัพย์ที่บันทึกไว้ " + savedCount + " รายการ", fontWeight = FontWeight.Bold)
                    Text(if (savedCount == 0) "กดดาวที่หน้าค้นหาเพื่อเริ่มติดตาม" else "ระบบจะแจ้งเมื่อวันขายหรือข้อมูลเปลี่ยน")
                }
            }
        }
    }
}

private fun money(value: Long): String =
    NumberFormat.getNumberInstance(Locale("th", "TH")).format(value)
