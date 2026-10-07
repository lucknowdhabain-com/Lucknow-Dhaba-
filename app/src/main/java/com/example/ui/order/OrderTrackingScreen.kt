package com.example.ui.order

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.data.models.Order
import com.example.ui.DhabaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(orderId: String, navController: NavController, viewModel: DhabaViewModel) {
    val orders by viewModel.orders.collectAsState()
    val order = orders.find { it.id == orderId } ?: orders.firstOrNull() // Fallback for demo

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Tracking") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    .height(300.dp)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                SimulatedMapContent()
                
                Card(
                    modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
                ) {
                    Text(
                        "Arriving in 15 mins",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Order Status
            OrderSummaryCard(order)

            Spacer(modifier = Modifier.height(16.dp))

            StatusTracker(order?.status ?: "Preparing")

            Spacer(modifier = Modifier.weight(1f))

            // Delivery Boy Info & PIN
            DeliveryBoyCard(order?.deliveryPin ?: "----")
        }
    }
}

@Composable
fun SimulatedMapContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "map")
    val dotOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dot"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val start = Offset(size.width * 0.2f, size.height * 0.8f)
        val end = Offset(size.width * 0.8f, size.height * 0.2f)
        
        // Path
        drawLine(
            color = Color.Gray,
            start = start,
            end = end,
            strokeWidth = 4.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
        )

        // User Location
        drawCircle(color = Color.Red, radius = 8.dp.toPx(), center = end)
        
        // Delivery Boy
        val currentPos = Offset(
            lerp(start.x, end.x, dotOffset),
            lerp(start.y, end.y, dotOffset)
        )
        drawCircle(color = Color(0xFF4CAF50), radius = 10.dp.toPx(), center = currentPos)
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
