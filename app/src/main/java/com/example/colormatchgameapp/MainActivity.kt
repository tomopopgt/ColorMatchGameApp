package com.example.colormatchgameapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                LevelUpTapGameScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelUpTapGameScreen() {
    // ゲームのステータス状態
    var level by remember { mutableStateOf(1) }
    var currentExp by remember { mutableStateOf(0) }
    var totalTaps by remember { mutableStateOf(0) }
    var isJustLeveledUp by remember { mutableStateOf(false) }

    // 必要経験値（レベル×10タップでレベルアップ）
    val requiredExp = level * 10

    // レベルに応じた称号リスト
    val titles = listOf(
        "見習いタッパー 🐣",
        "駆け出しハンター 🗡️",
        "ベテラン連打王 ⚔️",
        "神速の指先 ⚡",
        "タップの覇者 👑",
        "伝説のクリッカー 🌟",
        "神の領域 🌌"
    )
    val currentTitle = titles.getOrElse((level - 1) / 2) { "無敵の存在 ♾️" }

    // レベルアップ時のゴールド発光アニメーション
    val cardBgColor by animateColorAsState(
        targetValue = if (isJustLeveledUp) Color(0xFFFFD700) else MaterialTheme.colorScheme.secondaryContainer,
        animationSpec = tween(durationMillis = 300),
        label = "LevelUpAnimation"
    )

    // タップ時の処理
    fun handleTap() {
        totalTaps++
        currentExp++
        isJustLeveledUp = false

        // 必要EXPに達したらレベルアップ！
        if (currentExp >= requiredExp) {
            currentExp = 0
            level++
            isJustLeveledUp = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚔️ タップ・レベルアップ RPG", fontWeight = FontWeight.Bold) },
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. レベル & 称号 & EXPゲージ
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isJustLeveledUp) "✨ LEVEL UP! ✨" else currentTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isJustLeveledUp) Color(0xFFD81B60) else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lv. $level",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 経験値プログレスバー
                    val progress = currentExp.toFloat() / requiredExp.toFloat()
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = Color(0xFF4CAF50),
                        trackColor = Color.LightGray.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "EXP: $currentExp / $requiredExp (あと ${requiredExp - currentExp} タップ)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 2. 巨大タップボタン
            Surface(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .clickable { handleTap() },
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 8.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "💥",
                            fontSize = 60.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "TAP!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            // 3. 通算成績 & リセットボタン
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("通算タップ数", fontSize = 12.sp, color = Color.Gray)
                            Text("$totalTaps 回", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("1タップ威力", fontSize = 12.sp, color = Color.Gray)
                            Text("1 EXP", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = {
                        level = 1
                        currentExp = 0
                        totalTaps = 0
                        isJustLeveledUp = false
                    }
                ) {
                    Text("🔄 最初からやり直す", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}