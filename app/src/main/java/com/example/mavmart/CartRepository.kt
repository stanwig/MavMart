package com.example.mavmart

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class CartItem(
    val id: Long,
    val listing: Listing,
    var quantity: Int
)

object CartRepository {
    private val carts = mutableMapOf<Long, SnapshotStateList<CartItem>>()
    private var nextId = 1L

    private fun cartFor(userId: Long): SnapshotStateList<CartItem> {
        return carts.getOrPut(userId) { mutableStateListOf() }
    }

    fun getItems(userId: Long): SnapshotStateList<CartItem> = cartFor(userId)

    fun add(userId: Long, listing: Listing, qty: Int = 1) {
        val cart = cartFor(userId)
        val existing = cart.firstOrNull { it.listing.id == listing.id }
        if (existing != null) {
            existing.quantity += qty.coerceAtLeast(1)
            val idx = cart.indexOf(existing)
            if (idx >= 0) cart[idx] = cart[idx]
        } else {
            cart.add(
                CartItem(
                    id = nextId++,
                    listing = listing,
                    quantity = qty.coerceAtLeast(1)
                )
            )
        }
    }

    fun updateQuantity(userId: Long, cartItemId: Long, quantity: Int) {
        val cart = cartFor(userId)
        val idx = cart.indexOfFirst { it.id == cartItemId }
        if (idx >= 0) {
            if (quantity <= 0) {
                cart.removeAt(idx)
            } else {
                cart[idx] = cart[idx].copy(quantity = quantity)
            }
        }
    }

    fun remove(userId: Long, cartItemId: Long) {
        val cart = cartFor(userId)
        cart.removeAll { it.id == cartItemId }
    }

    fun clear(userId: Long) {
        cartFor(userId).clear()
    }

    fun totalCents(userId: Long): Int {
        return cartFor(userId).sumOf { it.listing.priceCents * it.quantity }
    }

    fun snapshot(userId: Long): List<Pair<Listing, Int>> =
        cartFor(userId).map { it.listing to it.quantity }
}