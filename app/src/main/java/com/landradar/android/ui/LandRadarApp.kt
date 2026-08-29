package com.landradar.android.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.landradar.android.data.PreviewPropertyRepository
import com.landradar.android.data.Property
import java.text.NumberFormat
import java.util.Locale

private val LandRadarColors = lightColorScheme(
    primary = Color(0xFF2E6B3D),
    secondary = Color(0xFF597A60),
    background = Color(0xFFF4F8F2),
    surface = Color.White
)

private enum class AuthStep { IDENTIFIER, OTP }
private enum class MainTab(val label: String) {
    SEARCH("ค้นหา"), SAVED("บันทึก"), ALERTS("แจ้งเตือน")
}

@Composable
fun LandRadarApp() {
    var signedIn by rememberSaveable { mutableStateOf(false) }
    MaterialTheme(colorScheme = LandRadarColors) {
        Surface(Modifier.fillMaxSize()) {
            if (signedIn) MainScreen(onSignOut = { signedIn = false })
            else SignInScreen(onAuthenticated = { signedIn = true })
        }
    }
}

@Composable
private fun SignInScreen(onAuthenticated: () -> Unit) {
    var step by rememberSaveable { mutableStateOf(AuthStep.IDENTIFIER) }
    var identifier by rememberSaveable { mutableStateOf("") }
    var otp by rememberSaveable { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("LandRadar", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("ค้นหา บันทึก และติดตามทรัพย์ที่คุณสนใจ")
        Spacer(Modifier.height(24.dp))

        if (step == AuthStep.IDENTIFIER) {
            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                label = { Text("อีเมลหรือเบอร์โทร") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { step = AuthStep.OTP },
                enabled = identifier.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("ขอรหัส OTP") }
        } else {
            Text("กรอกรหัสที่ส่งไปยัง " + identifier)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it.filter(Char::isDigit).take(6) },
                label = { Text("รหัส OTP 6 หลัก") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onAuthenticated,
                enabled = otp.length == 6,
                modifier = Modifier.fillMaxWidth()
            ) { Text("ยืนยัน (โหมดตัวอย่าง)") }
            TextButton(onClick = { step = AuthStep.IDENTIFIER; otp = "" }) {
                Text("เปลี่ยนอีเมลหรือเบอร์โทร")
            }
        }
        Text("โหมดตัวอย่างยังไม่ส่ง OTP จริง และไม่สร้าง session จริง", style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(onSignOut: () -> Unit) {
    val repository = remember { PreviewPropertyRepository() }
    var tab by rememberSaveable { mutableStateOf(MainTab.SEARCH) }
    var query by rememberSaveable { mutableStateOf("") }
    var savedVersion by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Property?>(null) }
    val saved = remember(savedVersion) { repository.savedIds() }
    val visible = repository.search(query).filter { tab != MainTab.SAVED || it.id in saved }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LandRadar") },
                actions = { TextButton(onClick = onSignOut) { Text("ออก") } }
            )
        },
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item; selected = null },
                        icon = { Text(when (item) {
                            MainTab.SEARCH -> "⌕"
                            MainTab.SAVED -> "★"
                            MainTab.ALERTS -> "●"
                        }) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        when {
            selected != null -> PropertyDetail(
                property = selected!!,
                isSaved = selected!!.id in saved,
                onBack = { selected = null },
                onToggleSaved = {
                    repository.toggleSaved(selected!!.id)
                    savedVersion++
                },
                modifier = Modifier.padding(padding)
            )
            tab == MainTab.ALERTS -> AlertsScreen(Modifier.padding(padding))
            else -> PropertyList(
                query = query,
                onQueryChange = { query = it },
                properties = visible,
                savedIds = saved,
                onOpen = { selected = it },
                onToggleSaved = {
                    repository.toggleSaved(it.id)
                    savedVersion++
                },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun PropertyList(
    query: String,
    onQueryChange: (String) -> Unit,
    properties: List<Property>,
    savedIds: Set<String>,
    onOpen: (Property) -> Unit,
    onToggleSaved: (Property) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("ค้นหาจังหวัด อำเภอ หรือประเภททรัพย์") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth().height(150.dp)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("แผนที่ขนาดเล็ก • " + properties.size + " หมุด")
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        if (properties.isEmpty()) item { Text("ไม่พบทรัพย์ที่ตรงกับการค้นหา") }
        items(properties, key = { it.id }) { property ->
            PropertyCard(property, property.id in savedIds, onOpen, onToggleSaved)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun PropertyCard(
    property: Property,
    isSaved: Boolean,
    onOpen: (Property) -> Unit,
    onToggleSaved: (Property) -> Unit
) {
    Card(Modifier.fillMaxWidth().clickable { onOpen(property) }) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(property.title, fontWeight = FontWeight.Bold)
                Text(property.district + ", " + property.province)
                Text(money(property.priceBaht) + " บาท • " + property.areaRai + " ไร่")
                Text("ขายทอดตลาด " + property.auctionDate, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = { onToggleSaved(property) }) {
                Text(if (isSaved) "★" else "☆", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun PropertyDetail(
    property: Property,
    isSaved: Boolean,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("‹ กลับ") }
        Text(property.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth().height(180.dp)) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("ตำแหน่ง " + property.latitude + ", " + property.longitude)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("รหัสทรัพย์: " + property.id)
        Text("ทำเล: " + property.district + ", " + property.province)
        Text("ราคาตั้งต้น: " + money(property.priceBaht) + " บาท")
        Text("ขนาด: " + property.areaRai + " ไร่")
        Text("วันขายทอดตลาด: " + property.auctionDate)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onToggleSaved, modifier = Modifier.fillMaxWidth()) {
            Text(if (isSaved) "เลิกบันทึกทรัพย์" else "☆ บันทึกทรัพย์")
        }
        Text("ข้อมูลเชิงลึกและการวิเคราะห์เต็มรูปแบบให้เปิดดูต่อบนเว็บไซต์หลัก", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun AlertsScreen(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(24.dp)) {
        Text("การแจ้งเตือน", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("ยังไม่มีการแจ้งเตือนใหม่")
                Text("ระบบจะแจ้งเฉพาะทรัพย์ที่คุณบันทึกหรือกำลังติดตาม")
            }
        }
    }
}

private fun money(value: Long): String =
    NumberFormat.getNumberInstance(Locale("th", "TH")).format(value)
