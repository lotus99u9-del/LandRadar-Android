package com.landradar.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LandRadarColors = lightColorScheme(
    primary = Color(0xFF2E6B3D),
    secondary = Color(0xFF597A60),
    background = Color(0xFFF4F8F2)
)

@Composable
fun LandRadarApp() {
    var signedIn by rememberSaveable { mutableStateOf(false) }
    MaterialTheme(colorScheme = LandRadarColors) {
        Surface(Modifier.fillMaxSize()) {
            if (signedIn) HomeScreen(onSignOut = { signedIn = false })
            else SignInScreen(onAuthenticated = { signedIn = true })
        }
    }
}

@Composable
private fun SignInScreen(onAuthenticated: () -> Unit) {
    var identifier by rememberSaveable { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("LandRadar", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text("ค้นหา บันทึก และติดตามทรัพย์ที่คุณสนใจ")
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = identifier,
            onValueChange = { identifier = it },
            label = { Text("อีเมลหรือเบอร์โทร") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onAuthenticated,
            enabled = identifier.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("ขอรหัส OTP") }
        Spacer(Modifier.height(8.dp))
        Text(
            "หน้าจอนี้เป็น UI scaffold — ต้องเชื่อม Auth API ก่อนใช้งานจริง",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun HomeScreen(onSignOut: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LandRadar") },
                actions = { TextButton(onClick = onSignOut) { Text("ออกจากระบบ") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(Modifier.fillMaxWidth().height(180.dp)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("แผนที่และหมุดทรัพย์")
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("ค้นหา • ตัวกรองพื้นฐาน • บันทึก • การแจ้งเตือน")
        }
    }
}
