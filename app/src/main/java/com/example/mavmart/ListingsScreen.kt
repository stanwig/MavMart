package com.example.mavmart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

// Toggle mode for the screen
enum class ListingsMode { All, Mine }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingsScreen(
    currentUserId: Long?,          // null if not logged in
    onLogout: () -> Unit
) {
    val brandPrimary = Color(0xFF0A2647)
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }

    var mode by remember { mutableStateOf(ListingsMode.All) }
    var listings by remember { mutableStateOf(emptyList<Listing>()) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(mode, currentUserId) {
        listings = when (mode) {
            ListingsMode.All  -> db.getAllListings()
            ListingsMode.Mine -> currentUserId?.let { db.getListingsForSeller(it) } ?: emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (mode == ListingsMode.All) "Listings" else "My Listings", color = brandPrimary) },
                actions = {
                    if (mode == ListingsMode.Mine && currentUserId != null) {
                        TextButton(onClick = { showAddDialog = true }) {
                            Text("Create Listing", color = brandPrimary)
                        }
                    }
                    TextButton(onClick = onLogout) { Text("Logout", color = brandPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = { mode = ListingsMode.All },
                    modifier = Modifier.weight(1f)
                ) { Text("Listings") }

                FilledTonalButton(
                    onClick = { mode = ListingsMode.Mine },
                    modifier = Modifier.weight(1f)
                ) { Text("My Listings") }
            }

            Spacer(Modifier.height(16.dp))

            if (listings.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (mode == ListingsMode.All) "No listings yet." else "You have no listings.",
                        color = Color.Black
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listings) { item ->
                        ElevatedCard {
                            Column(Modifier.padding(16.dp)) {
                                Text(item.title, style = MaterialTheme.typography.titleMedium, color = Color.Black)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    formatCents(item.priceCents),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = brandPrimary
                                )

                                // 🔧 No non-null assertion: show only when non-null & not blank
                                item.description
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let {
                                        Spacer(Modifier.height(6.dp))
                                        Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
                                    }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddListingDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, description, category, priceDollars, condition ->
                if (currentUserId == null) return@AddListingDialog
                val priceCents = (priceDollars.toDoubleOrNull()?.times(100))?.toInt() ?: 0
                val newListing = Listing(
                    id = 0L,
                    sellerId = currentUserId,
                    title = title.trim(),
                    description = description.ifBlank { null },
                    category = category,
                    priceCents = priceCents,
                    condition = condition,
                    photos = emptyList(),
                    status = ListingStatus.ACTIVE,
                    createdAt = System.currentTimeMillis()
                )
                db.insertListing(newListing)
                listings = db.getListingsForSeller(currentUserId) // refresh My Listings
                showAddDialog = false
            }
        )
    }
}

/* ---------- Add Listing Dialog ---------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddListingDialog(
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        category: ListingCategory,
        priceDollars: String,
        condition: ItemCondition
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    // defaults that exist in Listing.kt
    var category by remember { mutableStateOf(ListingCategory.GENERAL) }
    var condition by remember { mutableStateOf(ItemCondition.GOOD) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && price.isNotBlank(),
                onClick = { onSave(title, description, category, price, condition) }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Create Listing", color = Color.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Description (optional)") }, modifier = Modifier.fillMaxWidth()
                )

                CategoryPicker(value = category, onChange = { category = it })
                ConditionPicker(value = condition, onChange = { condition = it })

                OutlinedTextField(
                    value = price, onValueChange = { price = it },
                    label = { Text("Price (e.g., 12.34)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPicker(value: ListingCategory, onChange: (ListingCategory) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = value.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category", color = Color.Black) },
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ListingCategory.entries.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt.label, color = Color.Black) },
                    onClick = { onChange(opt); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConditionPicker(value: ItemCondition, onChange: (ItemCondition) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = value.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Condition", color = Color.Black) },
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ItemCondition.entries.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt.label, color = Color.Black) },
                    onClick = { onChange(opt); expanded = false }
                )
            }
        }
    }
}

private fun formatCents(cents: Int): String {
    val dollars = cents / 100
    val pennies = cents % 100
    return "$$dollars.${pennies.toString().padStart(2, '0')}"
}