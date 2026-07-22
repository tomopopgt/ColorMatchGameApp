package com.example.colormatchgameapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// 自動獲得ロボのデータモデル
data class AutoWorker(
    val id: Int,
    val name: String,
    val emoji: String,
    val basePower: Long,
    val count: Int,
    val cost: Long,
    val costMultiplier: Double
) {
    val currentPower: Long
        get() {
            if (count == 0) return 0L
            val bonusMultiplier = (count / 5).coerceAtMost(60)
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
    // 💎 永続ステータス
    var gems by remember { mutableStateOf(0L) }
    var rebornCount by remember { mutableStateOf(0) }
    val gemMultiplier = 1L + gems

    // 周回ステータス
    var points by remember { mutableStateOf(0L) }
    var tapLevel by remember { mutableStateOf(1) }
    var tapCost by remember { mutableStateOf(10L) }

    // 🔘 まとめ買いモード ("1", "10", "100", "MAX")
    var buyMode by remember { mutableStateOf("1") }

    fun createInitialWorkers() = listOf(
        AutoWorker(1, "お手伝いロボ", "🤖", basePower = 1L, count = 0, cost = 15L, costMultiplier = 1.15),
        AutoWorker(2, "AIアシスタント", "💻", basePower = 10L, count = 0, cost = 100L, costMultiplier = 1.15),
        AutoWorker(3, "量子スパコン", "⚛️", basePower = 50L, count = 0, cost = 600L, costMultiplier = 1.15),
        AutoWorker(4, "銀河サーバー", "🌌", basePower = 300L, count = 0, cost = 3000L, costMultiplier = 1.15)
    )

    var autoWorkers by remember { mutableStateOf(createInitialWorkers()) }

    val rawTapPower: Long = tapLevel.toLong() * (1L shl (tapLevel / 5).coerceAtMost(60))
    val tapPower: Long = rawTapPower * gemMultiplier

    val rawAutoPower: Long = autoWorkers.sumOf { it.currentPower }
    val totalAutoPower: Long = rawAutoPower * gemMultiplier

    val pendingGems = if (points >= 10000L) points / 5000L else 0L
    var showRebornDialog by remember { mutableStateOf(false) }

    // ⏱️ 自動ポイント加算ループ
    LaunchedEffect(totalAutoPower) {
        while (true) {
            delay(1000L)
            if (totalAutoPower > 0) {
                points += totalAutoPower
            }
        }
    }

    // 🧮 まとめ買いの正確な計算ロジック (タップ強化用)
    fun getTapBuyInfo(): Pair<Int, Long> {
        val targetCount = when (buyMode) {
            "10" -> 10
            "100" -> 100
            "MAX" -> -1
            else -> 1
        }
        var p = points
        var cost = tapCost
        var totalCost = 0L

        if (targetCount != -1) {
            for (i in 0 until targetCount) {
                totalCost += cost
                cost = (cost * 1.15).toLong() + 1L
            }
            return if (p >= totalCost) Pair(targetCount, totalCost) else Pair(0, totalCost)
        } else {
            var count = 0
            while (p >= cost && count < 1000) {
                p -= cost
                totalCost += cost
                cost = (cost * 1.15).toLong() + 1L
                count++
            }
            return Pair(count, if (count > 0) totalCost else tapCost)
        }
    }

    // 🧮 まとめ買いの正確な計算ロジック (自動ロボ用)
    fun getWorkerBuyInfo(worker: AutoWorker): Pair<Int, Long> {
        val targetCount = when (buyMode) {
            "10" -> 10
            "100" -> 100
            "MAX" -> -1
            else -> 1
        }
        var p = points
        var cost = worker.cost
        var totalCost = 0L

        if (targetCount != -1) {
            for (i in 0 until targetCount) {
                totalCost += cost
                cost = (cost * worker.costMultiplier).toLong() + 1L
            }
            return if (p >= totalCost) Pair(targetCount, totalCost) else Pair(0, totalCost)
        } else {
            var count = 0
            while (p >= cost && count < 1000) {
                p -= cost
                totalCost += cost
                cost = (cost * worker.costMultiplier).toLong() + 1L
                count++
            }
            return Pair(count, if (count > 0) totalCost else worker.cost)
        }
    }

    // 🛒 一括購入実行処理 (タップ強化)
    fun executeTapBuy() {
        val (buyCount, totalCost) = getTapBuyInfo()
        if (buyCount > 0 && points >= totalCost) {
            points -= totalCost
            var newCost = tapCost
            repeat(buyCount) {
                newCost = (newCost * 1.15).toLong() + 1L
            }
            tapLevel += buyCount
            tapCost = newCost
        }
    }

    // 🛒 一括購入実行処理 (自動ロボ)
    fun executeWorkerBuy(workerId: Int) {
        val worker = autoWorkers.find { it.id == workerId } ?: return
        val (buyCount, totalCost) = getWorkerBuyInfo(worker)
        if (buyCount > 0 && points >= totalCost) {
            points -= totalCost
            autoWorkers = autoWorkers.map { item ->
                if (item.id == workerId) {
                    var newCost = item.cost
                    repeat(buyCount) {
                        newCost = (newCost * item.costMultiplier).toLong() + 1L
                    }
                    item.copy(count = item.count + buyCount, cost = newCost)
                } else item
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
            // 1. ステータス表示
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
                    if (gems > 0L || rebornCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💎 転生石: $gems 個", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00BCD4))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("(全獲得PT ×${gemMultiplier}倍!!)", fontSize = 12.sp, color = Color(0xFFE91E63), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

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

            Spacer(modifier = Modifier.height(8.dp))

            // 2. 巨大タップボタン
            Surface(
                modifier = Modifier
                    .size(110.dp)
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
                        Text("💥", fontSize = 28.sp)
                        Text("TAP!", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("+$tapPower", fontSize = 10.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🌀 転生ボタン
            Button(
                onClick = { showRebornDialog = true },
                enabled = points >= 10000L,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0),
                    disabledContainerColor = Color.LightGray
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🌀 強くてニューゲーム (転生)", fontWeight = FontWeight.Bold)
                        Text(
                            text = if (points >= 10000L) "獲得ダイヤ: 💎 +$pendingGems 個" else "条件: 10,000 PT 以上",
                            fontSize = 11.sp,
                            color = Color.Yellow
                        )
                    }
                    Text("転生 ${rebornCount}回目", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. 🛒 ショップヘッダー & まとめ買い切替トグル
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🛒 強化ショップ", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("1", "10", "100", "MAX").forEach { mode ->
                        FilterChip(
                            selected = buyMode == mode,
                            onClick = { buyMode = mode },
                            label = { Text(if (mode == "MAX") "MAX" else "${mode}x", fontSize = 11.sp) },
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ショップリスト
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // タップ力強化
                item {
                    val (buyCount, totalCost) = getTapBuyInfo()
                    val canBuy = buyCount > 0 && points >= totalCost

                    LongPressButton(
                        onAction = { executeTapBuy() },
                        enabled = canBuy,
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.primary
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("⚡ タップ威力 (Lv.$tapLevel)", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("現在: +$tapPower PT", fontSize = 11.sp, color = Color.White)
                                Text("🎁 2倍まで: あと ${5 - (tapLevel % 5)}Lv", fontSize = 11.sp, color = Color(0xFFFFEB3B))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("💰 $totalCost PT", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("+$buyCount レベル", fontSize = 11.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 自動ロボリスト
                items(autoWorkers, key = { it.id }) { worker ->
                    val (buyCount, totalCost) = getWorkerBuyInfo(worker)
                    val canBuy = buyCount > 0 && points >= totalCost

                    LongPressButton(
                        onAction = { executeWorkerBuy(worker.id) },
                        enabled = canBuy,
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xFF3F51B5)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${worker.emoji} ${worker.name} (Lv.${worker.count})", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("現在: 毎秒 +${worker.currentPower * gemMultiplier} PT", fontSize = 11.sp, color = Color.White)
                                Text("🎁 2倍まで: あと ${5 - (worker.count % 5)}Lv", fontSize = 11.sp, color = Color(0xFFFFEB3B))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("💰 $totalCost PT", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("+$buyCount 個", fontSize = 11.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // 🌀 転生確認ダイアログ
    if (showRebornDialog) {
        AlertDialog(
            onDismissRequest = { showRebornDialog = false },
            title = { Text("🌀 強くてニューゲームを実行しますか？") },
            text = {
                Column {
                    Text("ポイントやレベルはリセットされますが、永続秘宝【転生石 💎】を獲得します！")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("獲得できる転生石: 💎 +$pendingGems 個", fontWeight = FontWeight.Bold, color = Color(0xFF00BCD4))
                    Text("転生後の全体倍率: ×${1L + gems + pendingGems}倍！", fontWeight = FontWeight.Bold, color = Color(0xFFE91E63))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        gems += pendingGems
                        rebornCount++
                        points = 0L
                        tapLevel = 1
                        tapCost = 10L
                        autoWorkers = createInitialWorkers()
                        showRebornDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                ) {
                    Text("転生する！🔥")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRebornDialog = false }) {
                    Text("やめる")
                }
            }
        )
    }
}

// 👆 1回タップも長押し連打も100%完璧に動くカスタムボタン
@Composable
fun LongPressButton(
    onAction: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    content: @Composable RowScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    // 長押し中の自動連打処理
    LaunchedEffect(isPressed, enabled) {
        if (isPressed && enabled) {
            delay(300L) // 長押し検知の待ち時間
            while (isPressed && enabled) {
                onAction()
                delay(80L) // 高速連打（80ミリ秒ごと）
            }
        }
    }

    Surface(
        modifier = modifier.pointerInput(enabled) {
            if (!enabled) return@pointerInput
            detectTapGestures(
                onTap = { if (enabled) onAction() }, // 1回タップで確実に1回購入！
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                }
            )
        },
        shape = RoundedCornerShape(12.dp),
        color = if (enabled) backgroundColor else Color.Gray.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}