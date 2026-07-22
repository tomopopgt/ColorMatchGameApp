package com.example.colormatchgameapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                IdleClickerGameScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdleClickerGameScreen() {
    // ステータス（所持ポイント・威力）
    var points by remember { mutableStateOf(0) }
    var tapPower by remember { mutableStateOf(1) }       // 1タップあたりの威力
    var autoPower by remember { mutableStateOf(0) }      // 1秒あたりの自動獲得ポイント

    // 強化レベルと必要なコスト
    var tapLevel by remember { mutableStateOf(1) }
    var tapCost by remember { mutableStateOf(10) }

    var autoLevel by remember { mutableStateOf(0) }
    var autoCost by remember { mutableStateOf(20) }

    // ⏱️ 自動ポイント加算ループ（1秒ごとに autoPower 分だけポイント増加）
    LaunchedEffect(autoPower) {
        while (true) {
            delay(1000L) // 1秒待つ
            if (autoPower > 0) {
                points += autoPower
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚔️ 放置＆連打クリッカーRPG", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. ポイント＆ステータス表示
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("所持ポイント", fontSize = 14.sp, color = Color.Gray)
                    Text(
                        text = "💰 $points PT",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("1タップ威力", fontSize = 12.sp, color = Color.Gray)
                            Text("⚡ +$tapPower PT", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("自動獲得 (毎秒)", fontSize = 12.sp, color = Color.Gray)
                            Text("🤖 +$autoPower PT/秒", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. 巨大タップボタン
            Surface(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .clickable { points += tapPower }, // タップで威力分だけ増える
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 8.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💥", fontSize = 50.sp)
                        Text(
                            text = "TAP!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "+$tapPower",
                            fontSize = 14.sp,
                            color = Color.Yellow,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 3. ショップ / アップグレードエリア
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🛒 強化ショップ", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                // 強化①：タップ力強化
                Button(
                    onClick = {
                        if (points >= tapCost) {
                            points -= tapCost
                            tapLevel++
                            tapPower += 1
                            tapCost = (tapCost * 1.5).toInt() // 次のコストを1.5倍に
                        }
                    },
                    enabled = points >= tapCost, // ポイントが足りない時はボタンを押せない
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("⚡ タップ威力強化 (Lv.$tapLevel)", fontWeight = FontWeight.Bold)
                            Text("タップ時の獲得量を +1 増加", fontSize = 11.sp)
                        }
                        Text("💰 $tapCost PT", fontWeight = FontWeight.Bold)
                    }
                }

                // 強化②：自動獲得強化（放置収益）
                Button(
                    onClick = {
                        if (points >= autoCost) {
                            points -= autoCost
                            autoLevel++
                            autoPower += 1
                            autoCost = (autoCost * 1.6).toInt() // 次のコストを1.6倍に
                        }
                    },
                    enabled = points >= autoCost,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50) // 緑色のボタン
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🤖 自動収益ロボ雇用 (Lv.$autoLevel)", fontWeight = FontWeight.Bold)
                            Text("毎秒の自動獲得を +1/秒 増加", fontSize = 11.sp)
                        }
                        Text("💰 $autoCost PT", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}