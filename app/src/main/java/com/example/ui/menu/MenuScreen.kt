package com.example.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.R
import com.example.data.models.MenuItem
import com.example.ui.DhabaViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(navController: NavController, viewModel: DhabaViewModel) {
    val menuItems by viewModel.menuItems.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    var showVegOnly by remember { mutableStateOf(false) }

    val filteredItems = menuItems.filter {
        (selectedCategory == "All" || it.category == selectedCategory) &&
        (!showVegOnly || it.isVeg)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Authentic Lucknowi Menu") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Filters
            CategoryFilters(selectedCategory) { selectedCategory = it }
            
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = showVegOnly,
                    onClick = { showVegOnly = !showVegOnly },
                    label = { Text("Veg Only") },
                    leadingIcon = {
                        if (showVegOnly) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (selectedCategory == "All") {
                    val grouped = filteredItems.groupBy { it.category }
                    grouped.forEach { (category, items) ->
                        item {
                            CategoryHeader(category)
                        }
                        items(items) { item ->
                            MenuItemRow(item) {
                                viewModel.addToCart(item, 1, emptyList())
                            }
                        }
                    }
                } else {
                    items(filteredItems) { item ->
                        MenuItemRow(item) {
                            viewModel.addToCart(item, 1, emptyList())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(category: String) {
    Text(
        text = category,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun CategoryFilters(selected: String, onSelect: (String) -> Unit) {
    val categories = listOf("All", "Kebabs", "Biryanis", "Breads", "Curries", "Desserts")
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { cat ->
            FilterChip(
                selected = selected == cat,
                onClick = { onSelect(cat) },
                label = { Text(cat) }
            )
        }
    }
}

@Composable
fun MenuItemRow(item: MenuItem, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Circle,
                    contentDescription = null,
                    tint = if (item.isVeg) Color(0xFF4CAF50) else Color(0xFFE53935),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                if (item.rating > 4.0) {
                    Text("Bestseller", color = Color(0xFFE65100), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("₹${item.price}", style = MaterialTheme.typography.bodyMedium)
            Text(
                item.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 2
            )
        }
        
        Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.BottomCenter) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.img_hero_banner)
            )
            Button(
                onClick = onAdd,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.offset(y = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Text("ADD", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
}
