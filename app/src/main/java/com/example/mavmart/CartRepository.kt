package com.example.mavmart

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class CartItem(
    val id: Long,
    val listing: Listing
)

object CartRepository {
    private val carts = mutableMapOf<Long, SnapshotStateList<CartItem>>()
    private var nextId = 1L

    private fun cartFor(userId: Long): SnapshotStateList<CartItem> =
        carts.getOrPut(userId) { mutableStateListOf() }

    fun getItems(userId: Long): SnapshotStateList<CartItem> = cartFor(userId)

    fun add(userId: Long, listing: Listing) {
        val cart = cartFor(userId)
        if (cart.any { it.listing.id == listing.id }) return
        cart.add(CartItem(id = nextId++, listing = listing))
    }

    fun remove(userId: Long, cartItemId: Long) {
        cartFor(userId).removeAll { it.id == cartItemId }
    }

    fun clear(userId: Long) {
        cartFor(userId).clear()
    }

    fun totalCents(userId: Long): Int =
        cartFor(userId).sumOf { it.listing.priceCents }

    fun snapshot(userId: Long): List<Listing> =
        cartFor(userId).map { it.listing }
}