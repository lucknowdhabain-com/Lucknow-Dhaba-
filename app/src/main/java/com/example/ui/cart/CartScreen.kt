package com.example.ui.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.models.UserAddress
import com.example.ui.DhabaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController, viewModel: DhabaViewModel) {
    val cartItems by viewModel.cart.collectAsState()
    val subtotal = cartItems.sumOf { it.totalPrice }
    val tax = subtotal * 0.05
    val deliveryFee = if (cartItems.isEmpty()) 0.0 else 40.0
    val total = subtotal + tax + deliveryFee

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    tonalElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total", style = MaterialTheme.typography.bodySmall)
                                Text("₹${"%.2f".format(total)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    viewModel.placeOrder(
                                        address = UserAddress("Home", "Hazratganj, Lucknow", 26.8467, 80.9462),
                                        paymentMethod = "UPI"
                                    ) { orderId ->
                                        navController.navigate("order_tracking/$orderId") {
                                            popUpTo("home")
                                        }
                                    }
                                },
                                modifier = Modifier.height(56.dp).width(200.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("PLACE ORDER", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your cart is empty", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                itemsIndexed(cartItems) { index, item ->
                    CartItemRow(item) {
                        viewModel.removeFromCart(index)
                    }
                }

                item {
                    CouponSection()
                }

                item {
                    BillDetails(subtotal, tax, deliveryFee, total)
                }
                
                item {
                    AddressSelection()
                }
                
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun CartItemRow(item: com.example.data.models.CartItem, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.Bold)
            Text("₹${item.basePrice} x ${item.quantity}", style = MaterialTheme.typography.bodySmall)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("₹${item.totalPrice}", fontWeight = FontWeight.Medium)
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
}

@Composable
fun CouponSection() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Apply Coupon Code", fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
fun BillDetails(subtotal: Double, tax: Double, deliveryFee: Double, total: Double) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Bill Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        BillRow("Item Total", subtotal)
        BillRow("Tax (5%)", tax)
        BillRow("Delivery Fee", deliveryFee)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("To Pay", fontWeight = FontWeight.Bold)
            Text("₹${"%.2f".format(total)}", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BillRow(label: String, amount: Double) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text("₹${"%.2f".format(amount)}", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun AddressSelection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Deliver to", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Home, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Home", fontWeight = FontWeight.Bold)
                    Text("Hazratganj, Lucknow - 226001", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}
