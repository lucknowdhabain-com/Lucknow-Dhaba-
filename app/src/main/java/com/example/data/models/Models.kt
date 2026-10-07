package com.example.data.models

import com.google.firebase.Timestamp

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val imageUrl: String = "",
    val rating: Double = 0.0,
    val isVeg: Boolean = true,
    val customizations: List<CustomizationGroup> = emptyList()
)

data class CustomizationGroup(
    val name: String = "",
    val options: List<CustomizationOption> = emptyList()
)

data class CustomizationOption(
    val name: String = "",
    val price: Double = 0.0
)

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val addresses: List<UserAddress> = emptyList()
)

data class UserAddress(
    val label: String = "",
    val addressString: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalBill: Double = 0.0,
    val tax: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val status: String = "Accepted",
    val deliveryPin: String = "",
    val deliveryAddress: UserAddress = UserAddress(),
    val paymentMethod: String = "UPI",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val deliveryBoy: DeliveryBoy? = null
)

data class CartItem(
    val menuId: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val basePrice: Double = 0.0,
    val selectedCustomizations: List<String> = emptyList(),
    val totalPrice: Double = 0.0
)

data class DeliveryBoy(
    val name: String = "",
    val phone: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0
)
