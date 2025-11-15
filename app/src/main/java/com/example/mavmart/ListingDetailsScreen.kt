package com.example.mavmart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailsScreen(
    currentUserId: Long,
    listingId: Long,
    onBack: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var listing by remember { mutableStateOf<Listing?>(null) }
    var seller by remember { mutableStateOf<User?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(listingId) {
        val l = db.getListingById(listingId)
        listing = l
        seller = l?.let { db.getUserById(it.sellerId) }
    }

    val sellerEnabled = seller?.enabled ?: true
    if (!sellerEnabled) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("This listing is no longer available.")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listing Details", color = cs.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = cs.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            val l = listing
            if (l != null) {
                val isOwner = l.sellerId == currentUserId
                if (isOwner) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cs.primary,
                                contentColor = cs.onPrimary
                            )
                        ) { Text("DELETE LISTING") }

                        Button(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cs.primary,
                                contentColor = cs.onPrimary
                            )
                        ) { Text("EDIT LISTING") }
                    }
                } else {
                    Button(
                        onClick = {
                            CartRepository.add(currentUserId, l)
                            scope.launch { snackbarHostState.showSnackbar("Added to cart", withDismissAction = true) }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.primary,
                            contentColor = cs.onPrimary
                        )
                    ) {
                        Icon(Icons.Outlined.ShoppingCart, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("ADD TO CART")
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { inner ->
        val l = listing
        if (l == null) {
            Box(Modifier.padding(inner).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (l.photos.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(l.photos.size) { i ->
                            AsyncImage(
                                model = l.photos[i],
                                contentDescription = null,
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(MaterialTheme.shapes.medium)
                            )
                        }
                    }
                }

                Text(l.title, style = MaterialTheme.typography.headlineSmall, color = cs.onBackground)
                Text(formatCents(l.priceCents), style = MaterialTheme.typography.titleMedium, color = cs.onBackground)
                Text("Category: ${l.category.label}", color = cs.onBackground)
                Text("Condition: ${l.condition.label}", color = cs.onBackground)

                val sellerName = seller?.let { "${it.first} ${it.last}" } ?: "Unknown"
                Text("Seller: $sellerName", color = cs.onBackground)
                Text("Contact: ${l.contact}", color = cs.onBackground)
                Text("Meet at: ${l.place}", color = cs.onBackground)


                l.description?.takeIf { it.isNotBlank() }?.let {
                    HorizontalDivider()
                    Text(it, style = MaterialTheme.typography.bodyLarge, color = cs.onBackground)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete listing?", color = cs.onSurface) },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        listing?.let { db.deleteListing(it.id) }
                        showDeleteConfirm = false
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = cs.onSurface)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = cs.onSurface)
                ) { Text("Cancel") }
            }
        )
    }
    if (showEditDialog && listing != null) {
        EditListingDialog(
            listing = listing!!,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                db.updateListing(updated)
                listing = db.getListingById(updated.id)
                showEditDialog = false
                scope.launch { snackbarHostState.showSnackbar("Listing updated", withDismissAction = true) }
            }
        )
    }
}
