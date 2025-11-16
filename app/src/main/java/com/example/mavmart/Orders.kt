package com.example.mavmart

data class OrderLine(
    val title: String,
    val priceCents: Int,
    val sellerId: Long,
    val sellerName: String,
    val place: String,
    val contact: String
)

data class OrderDetails(
    val orderId: Long,
    val lines: List<OrderLine>,
    val totalCents: Int,
    val placedAtMillis: Long
)

object OrderStore {
    private val ordersByUser = mutableMapOf<Long, MutableMap<Long, OrderDetails>>()

    fun save(userId: Long, order: OrderDetails) {
        val bucket = ordersByUser.getOrPut(userId) { mutableMapOf() }
        bucket[order.orderId] = order
    }

    fun get(userId: Long, orderId: Long): OrderDetails? =
        ordersByUser[userId]?.get(orderId)
}
