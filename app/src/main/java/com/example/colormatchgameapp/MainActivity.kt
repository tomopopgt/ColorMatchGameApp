package com.example.colormatchgameapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

// 自動獲得ロボのデータモデル（超巨大な数字を扱うために Long 型を使用）
data class AutoWorker(
    val id: Int,
    val name: String,
    val emoji: String,
    val basePower: Long,
    val count: Int,
    val cost: Long,
    val costMultiplier: Double
) {
    // 💡 5レベルごとに威力が 2倍、4倍、8倍... と跳ね上がる計算式！
    val currentPower: Long
        get() {
            val bonusMultiplier = (count / 5).coerceAtMost(60) // 5で割った数だけ倍増（1L shl は2の乗数）
            return basePower * count * (1L shl bonusMultiplier)
        }
}

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
    // ステータス（所持ポイントは限界突破のために Long に変更）
    var points by remember { mutableStateOf(0L) }
    var tapLevel by remember { mutableStateOf(1) }
    var tapCost by remember { mutableStateOf(10L) }

    // 💡 タップ威力も 5レベルごとに 2倍、4倍... と倍増！
    val tapPower: Long = tapLevel.toLong() * (1L shl (tapLevel / 5).coerceAtMost(60))

    // 🤖 自動ロボたちのラインナップ（コストの上昇倍率を 1.15倍 に抑えて買いやすくしました）
    var autoWorkers by remember {
        mutableStateOf(
            listOf(
                AutoWorker(1, "お手伝いロボ", "🤖", basePower = 1L, count = 0, cost = 15L, costMultiplier = 1.15),
                AutoWorker(2, "AIアシスタント", "💻", basePower = 10L, count = 0, cost = 100L, costMultiplier = 1.15),
                AutoWorker(3, "量子スパコン", "⚛️", basePower = 50L, count = 0, cost = 600L, costMultiplier = 1.15),
                AutoWorker(4, "銀河サーバー", "🌌", basePower = 300L, count = 0, cost = 3000L, costMultiplier = 1.15)
            )
        )
    }

    // 1秒あたりの合計自動獲得ポイント
    val totalAutoPower: Long = autoWorkers.sumOf { it.currentPower }

    // ⏱️ 自動ポイント加算ループ
    LaunchedEffect(totalAutoPower) {
        while (true) {
            delay(1000L)
            if (totalAutoPower > 0) {
                points += totalAutoPower
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. ポイント＆ステータス表示
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("所持ポイント", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = "💰 $points PT",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("1タップ威力", fontSize = 11.sp, color = Color.Gray)
                            Text("⚡ +$tapPower", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("自動獲得 (毎秒)", fontSize = 11.sp, color = Color.Gray)
                            Text("🤖 +$totalAutoPower", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. 巨大タップボタン
            Surface(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .clickable { points += tapPower },
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 6.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💥", fontSize = 36.sp)
                        Text("TAP!", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("+$tapPower", fontSize = 12.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. ショップエリア
            Text(
                text = "🛒 強化＆仲間ショップ",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // タップ力強化ボタン
                item {
                    Button(
                        onClick = {
                            if (points >= tapCost) {
                                points -= tapCost
                                tapLevel++
                                tapCost = (tapCost * 1.15).toLong() + 1L // コスト上昇を緩やかに
                            }
                        },
                        enabled = points >= tapCost,
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
                                Text("現在: +$tapPower PT", fontSize = 11.sp, color = Color.White)
                                // 次のボーナスまでのカウントダウン
                                Text("🎁 威力2倍まで: あと ${5 - (tapLevel % 5)}Lv", fontSize = 11.sp, color = Color(0xFFFFEB3B))
                            }
                            Text("💰 $tapCost PT", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 自動ロボ購入ボタン
                items(autoWorkers, key = { it.id }) { worker ->
                    Button(
                        onClick = {
                            if (points >= worker.cost) {
                                points -= worker.cost
                                autoWorkers = autoWorkers.map { item ->
                                    if (item.id == worker.id) {
                                        val newCount = item.count + 1
                                        val newCost = (item.cost * item.costMultiplier).toLong() + 1L // コスト上昇を緩やかに
                                        item.copy(count = newCount, cost = newCost)
                                    } else item
                                }
                            }
                        },
                        enabled = points >= worker.cost,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${worker.emoji} ${worker.name} (Lv.${worker.count})", fontWeight = FontWeight.Bold)
                                Text("現在: 毎秒 +${worker.currentPower} PT", fontSize = 11.sp, color = Color.White)
                                // 次のボーナスまでのカウントダウン
                                Text("🎁 威力2倍まで: あと ${5 - (worker.count % 5)}Lv", fontSize = 11.sp, color = Color(0xFFFFEB3B))
                            }
                            Text("💰 ${worker.cost} PT", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}