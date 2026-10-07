package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.models.*
import com.example.data.repository.DhabaRepository
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
                        name = "Lucknowi Mutton Biryani",
                        description = "Authentic slow-cooked mutton with fragrant basmati rice and traditional spices.",
                        price = 450.0,
                        category = "Main Course",
                        imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21bc4a4f8?q=80&w=400&auto=format&fit=crop",
                        rating = 4.9,
                        isVeg = false
                    ),
                    MenuItem(
                        name = "Galouti Kebab (4 pcs)",
                        description = "Melt-in-your-mouth minced mutton kebabs, served with mint chutney.",
                        price = 380.0,
                        category = "Starters",
                        imageUrl = "https://images.unsplash.com/photo-1601050690597-df0568f70950?q=80&w=400&auto=format&fit=crop",
                        rating = 4.8,
                        isVeg = false
                    ),
                    MenuItem(
                        name = "Paneer Tikka",
                        description = "Cottage cheese cubes marinated in yogurt and spices, grilled in tandoor.",
                        price = 280.0,
                        category = "Starters",
                        imageUrl = "https://images.unsplash.com/photo-1599487488170-d11ec9c172f0?q=80&w=400&auto=format&fit=crop",
                        rating = 4.5,
                        isVeg = true
                    ),
                    MenuItem(
                        name = "Dal Makhani",
                        description = "Creamy black lentils slow-cooked overnight with butter and spices.",
                        price = 320.0,
                        category = "Main Course",
                        imageUrl = "https://images.unsplash.com/photo-1546833999-b9f581a1996d?q=80&w=400&auto=format&fit=crop",
                        rating = 4.7,
                        isVeg = true
                    ),
                    MenuItem(
                        name = "Shahi Tukda",
                        description = "Royal bread pudding with rabri, saffron, and nuts.",
                        price = 150.0,
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
            onSuccess(orderId)
        }
    }
}
