package com.example.mavmart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CartScreen(
    currentUserId: Long,
    onCheckout: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val cartItems: SnapshotStateList<CartItem> =
        remember(currentUserId) { CartRepository.getItems(currentUserId) }
    val totalCents by remember(cartItems, currentUserId) {
        derivedStateOf { CartRepository.totalCents(currentUserId) }
    }

    if (cartItems.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Your cart is empty.", color = cs.onSurface)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.surface)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = cartItems, key = { it.id }) { cartItem: CartItem ->
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(containerColor = cs.background),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                cartItem.listing.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = cs.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(formatCents(cartItem.listing.priceCents), color = cs.onSurface)
                        }

                        OutlinedButton(
                            onClick = { CartRepository.remove(currentUserId, cartItem.id) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = cs.primary,
                                contentColor = cs.onPrimary)
                        ) { Text("REMOVE")
                        }
                    }
                }
            }
        }

        Surface(
            tonalElevation = 3.dp,
            shadowElevation = 3.dp,
            color = cs.surface
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total", color = cs.primary, style = MaterialTheme.typography.titleMedium)
                    Text(formatCents(totalCents), color = cs.primary, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { CartRepository.clear(currentUserId) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.primary)
                    ) { Text("CLEAR CART") }

                    Button(
                        onClick = onCheckout,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary)
                    ) { Text("CHECK OUT") }
                }
            }
        }
    }
}