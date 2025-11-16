package com.example.mavmart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderConfirmationScreen(
    userId: Long,
    orderId: Long,
    onContinue: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val order = remember(userId, orderId) { OrderStore.get(userId, orderId) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Order Confirmation") }
            )
        },
        bottomBar = {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cs.primary,
                    contentColor = cs.onPrimary
                )
            ) { Text("Continue") }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (order == null) {
                Text("Order not found.", color = cs.onSurface)
                return@Column
            }

            Text("Order ID: $orderId", style = MaterialTheme.typography.titleMedium, color = cs.primary)

            val fmt = remember { SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()) }
            Text("Placed: ${fmt.format(Date(order.placedAtMillis))}", color = cs.onSurface)

            HorizontalDivider()

            Text("Items", style = MaterialTheme.typography.titleSmall, color = cs.primary)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                items(order.lines) { line ->
                    ListItem(
                        headlineContent = { Text(line.title) },
                        // no quantity anymore
                        trailingContent = { Text(formatCents(line.priceCents)) }
                    )
                    HorizontalDivider()
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", style = MaterialTheme.typography.titleMedium, color = cs.primary)
                Text(formatCents(order.totalCents), style = MaterialTheme.typography.titleMedium, color = cs.primary)
            }

            Spacer(Modifier.height(12.dp))

            // If all items share the same seller/place/contact, show one message
            val uniquePickup = order.lines
                .map { Triple(it.sellerName, it.place, it.contact) }
                .toSet()

            if (uniquePickup.size == 1) {
                val (sellerName, place, contact) = uniquePickup.first()
                Text(
                    "You can meet $sellerName at $place to pick up your order. " +
                            "Contact the seller via $contact to schedule a date and time.",
                    color = cs.onSurface
                )
            } else {
                // Multiple sellers/places in one order – show per item notes
                order.lines.forEach { line ->
                    Text(
                        "• ${line.title}: meet ${line.sellerName} at ${line.place}. Contact: ${line.contact}",
                        color = cs.onSurface
                    )
                }
            }
        }
    }
}
