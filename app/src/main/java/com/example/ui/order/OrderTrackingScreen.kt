package com.example.ui.order

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.navigation.NavController
import com.example.R
import com.example.data.models.Order
import com.example.ui.DhabaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(orderId: String, navController: NavController, viewModel: DhabaViewModel) {
    val orders by viewModel.orders.collectAsState()
    val order = orders.find { it.id == orderId } ?: orders.lastOrNull() 

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Tracking Order", style = MaterialTheme.typography.titleMedium)
                        Text(order?.status ?: "Updating...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Help */ }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Help")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Simulated Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                SimulatedMapContent(order?.status ?: "Accepted")
                
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Moped, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                if (order?.status == "Delivered") "Order Delivered!" else "Arriving in 12-15 mins",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Your delivery partner is on the way", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    OrderSummaryCard(order)
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusTracker(order?.status ?: "Accepted")
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    DeliveryBoyCard(order?.deliveryPin ?: "----")
                }
            }
        }
    }
}

@Composable
fun SimulatedMapContent(status: String) {
    val isOutForDelivery = status == "Out for Delivery" || status == "Delivered"
    
    val infiniteTransition = rememberInfiniteTransition(label = "map")
    val dotOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = if (status == "Delivered") RepeatMode.Restart else RepeatMode.Restart
        ),
        label = "dot"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val start = Offset(size.width * 0.15f, size.height * 0.85f)
        val mid = Offset(size.width * 0.5f, size.height * 0.5f)
        val end = Offset(size.width * 0.85f, size.height * 0.15f)
        
        // Draw some grid lines/roads
        val roadColor = Color.LightGray.copy(alpha = 0.5f)
        for (i in 0..10) {
            drawLine(roadColor, Offset(0f, i * size.height / 10), Offset(size.width, i * size.height / 10), 1.dp.toPx())
            drawLine(roadColor, Offset(i * size.width / 10, 0f), Offset(i * size.width / 10, size.height), 1.dp.toPx())
        }

        // Delivery Route
        drawLine(
            color = Color.Gray.copy(alpha = 0.3f),
            start = start,
            end = end,
            strokeWidth = 6.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 20f), 0f)
        )

        // Home Location
        drawCircle(color = Color(0xFFE53935), radius = 10.dp.toPx(), center = end)
        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = end)
        
        // Dhaba Location
        drawCircle(color = Color(0xFF43A047), radius = 10.dp.toPx(), center = start)
        
        // Delivery Boy Movement
        if (isOutForDelivery) {
            val progress = if (status == "Delivered") 1f else dotOffset
            val currentPos = Offset(
                lerp(start.x, end.x, progress),
                lerp(start.y, end.y, progress)
            )
            
            // Draw a "radar" ring
            drawCircle(
                color = Color(0xFF2196F3).copy(alpha = 0.2f),
                radius = 25.dp.toPx() * (1f + dotOffset % 0.5f),
                center = currentPos
            )
            
            drawCircle(color = Color(0xFF2196F3), radius = 12.dp.toPx(), center = currentPos)
            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = currentPos)
        } else {
            // Restaurant pulse when preparing
            drawCircle(
                color = Color(0xFF43A047).copy(alpha = 0.3f),
                radius = 20.dp.toPx() * (1f + dotOffset % 0.4f),
                center = start
            )
        }
    }
}

fun lerp(start: Float, stop: Float, fraction: Float): Float = start + fraction * (stop - start)

@Composable
fun OrderSummaryCard(order: Order?) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Order #${order?.id?.takeLast(6) ?: "123456"}", fontWeight = FontWeight.Bold)
                Text("₹${order?.totalBill ?: 0.0}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Text("${order?.items?.size ?: 0} Items", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun StatusTracker(currentStatus: String) {
    val statuses = listOf("Accepted", "Preparing", "Out for Delivery", "Delivered")
    val currentIndex = statuses.indexOf(currentStatus).coerceAtLeast(0)

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        statuses.forEachIndexed { index, status ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (index <= currentIndex) MaterialTheme.colorScheme.primary else Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        if (index < currentIndex) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        } else if (index == currentIndex) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                        }
                    }
                    if (index < statuses.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(40.dp)
                                .background(if (index < currentIndex) MaterialTheme.colorScheme.primary else Color.LightGray)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (index == currentIndex) FontWeight.Bold else FontWeight.Normal,
                    color = if (index <= currentIndex) Color.Black else Color.Gray,
                    modifier = Modifier.padding(bottom = if (index < statuses.size - 1) 40.dp else 0.dp)
                )
            }
        }
    }
}

@Composable
fun DeliveryBoyCard(pin: String) {
    Surface(
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp).navigationBarsPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray)) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.align(Alignment.Center).size(30.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Rajesh Kumar", fontWeight = FontWeight.Bold)
                    Text("Delivery Partner", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                IconButton(onClick = { /* Call */ }, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)) {
                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Delivery PIN", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = pin,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 8.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text("Share this PIN only with the delivery partner", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}
