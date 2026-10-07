package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.models.*
import com.example.data.repository.DhabaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DhabaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DhabaRepository(application)
    
    init {
        seedDatabase()
    }

    private fun seedDatabase() {
        viewModelScope.launch {
            val menu = repository.observeMenu().first()
            if (menu.isEmpty()) {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance(
                    getApplication<Application>().getString(R.string.firestore_database_id)
                )
                val items = listOf(
                    MenuItem(
                        id = "b1",
                        name = "Lucknowi Mutton Biryani",
                        description = "Fragrant long-grain basmati rice cooked with tender mutton in the traditional Dum Pukht style.",
                        price = 550.0,
                        category = "Biryanis",
                        imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21bc4a4f8?q=80&w=400&auto=format&fit=crop",
                        rating = 4.9,
                        isVeg = false
                    ),
                    MenuItem(
                        id = "b2",
                        name = "Chicken Dum Biryani",
                        description = "Slow-cooked chicken with saffron-infused rice and aromatic Awadhi spices.",
                        price = 450.0,
                        category = "Biryanis",
                        imageUrl = "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?q=80&w=400&auto=format&fit=crop",
                        rating = 4.7,
                        isVeg = false
                    ),
                    MenuItem(
                        id = "k1",
                        name = "Galouti Kebab",
                        description = "Melt-in-your-mouth minced mutton kebabs, originally made for the Nawab of Lucknow.",
                        price = 390.0,
                        category = "Kebabs",
                        imageUrl = "https://images.unsplash.com/photo-1601050690597-df0568f70950?q=80&w=400&auto=format&fit=crop",
                        rating = 4.9,
                        isVeg = false
                    ),
                    MenuItem(
                        id = "k2",
                        name = "Tunday Kebab",
                        description = "The world-famous specialty made with a secret recipe of 160 spices.",
                        price = 420.0,
                        category = "Kebabs",
                        imageUrl = "https://images.unsplash.com/photo-1599487488170-d11ec9c172f0?q=80&w=400&auto=format&fit=crop",
                        rating = 4.8,
                        isVeg = false
                    ),
                    MenuItem(
                        id = "k3",
                        name = "Paneer Tikka Lucknowi",
                        description = "Succulent paneer cubes marinated in yogurt and yellow chili, grilled to perfection.",
                        price = 280.0,
                        category = "Kebabs",
                        imageUrl = "https://images.unsplash.com/photo-1599487488170-d11ec9c172f0?q=80&w=400&auto=format&fit=crop",
                        rating = 4.5,
                        isVeg = true
                    ),
                    MenuItem(
                        id = "br1",
                        name = "Sheermal",
                        description = "Sweet, saffron-flavored traditional naan made with milk and ghee.",
                        price = 60.0,
                        category = "Breads",
                        imageUrl = "https://images.unsplash.com/photo-1610192244261-3f1301f5584a?q=80&w=400&auto=format&fit=crop",
                        rating = 4.8,
                        isVeg = true
                    ),
                    MenuItem(
                        id = "br2",
                        name = "Roomali Roti",
                        description = "Paper-thin, soft handkerchief bread cooked on an inverted griddle.",
                        price = 40.0,
                        category = "Breads",
                        imageUrl = "https://images.unsplash.com/photo-1505253758473-96b7015fcd40?q=80&w=400&auto=format&fit=crop",
                        rating = 4.6,
                        isVeg = true
                    ),
                    MenuItem(
                        id = "c1",
                        name = "Nihari with Kulcha",
                        description = "Traditional slow-cooked beef/mutton stew with marrow, a breakfast staple.",
                        price = 480.0,
                        category = "Curries",
                        imageUrl = "https://images.unsplash.com/photo-1546833999-b9f581a1996d?q=80&w=400&auto=format&fit=crop",
                        rating = 4.9,
                        isVeg = false
                    ),
                    MenuItem(
                        id = "d1",
                        name = "Shahi Tukda",
                        description = "Golden fried bread soaked in saffron milk, topped with rabri and silver leaf.",
                        price = 180.0,
                        category = "Desserts",
                        imageUrl = "https://images.unsplash.com/photo-1605197293753-ac4d5082e666?q=80&w=400&auto=format&fit=crop",
                        rating = 4.9,
                        isVeg = true
                    )
                )
                items.forEach { item ->
                    db.collection("menu").add(item)
                }
            }
        }
    }

    val menuItems = repository.observeMenu()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders = repository.observeOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart = _cart.asStateFlow()

    fun addToCart(item: MenuItem, quantity: Int, customizations: List<String>) {
        val totalPrice = (item.price + 0.0) * quantity // Add customization prices logic if needed
        val cartItem = CartItem(
            menuId = item.id,
            name = item.name,
            quantity = quantity,
            basePrice = item.price,
            selectedCustomizations = customizations,
            totalPrice = totalPrice
        )
        _cart.update { it + cartItem }
    }

    fun removeFromCart(index: Int) {
        _cart.update { current ->
            current.toMutableList().apply { removeAt(index) }
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun placeOrder(address: UserAddress, paymentMethod: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val items = _cart.value
            val subtotal = items.sumOf { it.totalPrice }
            val tax = subtotal * 0.05
            val deliveryFee = 40.0
            val total = subtotal + tax + deliveryFee

            val order = Order(
                items = items,
                totalBill = total,
                tax = tax,
                deliveryFee = deliveryFee,
                deliveryAddress = address,
                paymentMethod = paymentMethod
            )

            val orderId = repository.placeOrder(order)
            clearCart()
            simulateOrderProgress(orderId)
            onSuccess(orderId)
        }
    }

    private fun simulateOrderProgress(orderId: String) {
        viewModelScope.launch {
            val statuses = listOf("Accepted", "Preparing", "Out for Delivery", "Delivered")
            for (status in statuses) {
                repository.updateOrderStatus(orderId, status)
                delay(15000) // Update status every 15 seconds for the demo
            }
        }
    }
}
