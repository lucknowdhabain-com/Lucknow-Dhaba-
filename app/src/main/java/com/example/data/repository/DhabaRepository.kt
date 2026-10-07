package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.models.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

class DhabaRepository(context: Context) {
    private val db = FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
    )
    private val auth = Firebase.auth

    private fun requireUserId(): String = auth.currentUser?.uid
        ?: throw IllegalStateException("User must be signed in.")

    fun observeMenu(): Flow<List<MenuItem>> = db.collection("menu")
        .snapshots()
        .map { snapshot -> snapshot.toObjects(MenuItem::class.java) }
        .catch { e ->
            Log.e("DhabaRepository", "Error observing menu", e)
            emit(emptyList())
        }

    fun observeOrders(): Flow<List<Order>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return db.collection("orders")
            .whereEqualTo("userId", uid)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Order::class.java) }
            .catch { e ->
                Log.e("DhabaRepository", "Error observing orders", e)
                emit(emptyList())
            }
    }

    suspend fun placeOrder(order: Order): String {
        val uid = requireUserId()
        val orderRef = db.collection("orders").document()
        val deliveryPin = (1000..9999).random().toString()
        
        val orderData = hashMapOf(
            "userId" to uid,
            "items" to order.items,
            "totalBill" to order.totalBill,
            "tax" to order.tax,
            "deliveryFee" to order.deliveryFee,
            "status" to "Accepted",
            "deliveryPin" to deliveryPin,
            "deliveryAddress" to order.deliveryAddress,
            "paymentMethod" to order.paymentMethod,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        orderRef.set(orderData).await()
        return orderRef.id
    }

    suspend fun saveUserAddress(address: UserAddress) {
        val uid = requireUserId()
        val userRef = db.collection("users").document(uid)
        userRef.update("addresses", FieldValue.arrayUnion(address)).await()
    }

    suspend fun getUserProfile(): UserProfile? {
        val uid = auth.currentUser?.uid ?: return null
        return db.collection("users").document(uid).get().await().toObject(UserProfile::class.java)
    }

    suspend fun updateOrderStatus(orderId: String, status: String) {
        db.collection("orders").document(orderId).update(
            "status", status,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }
}
