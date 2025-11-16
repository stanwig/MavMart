package com.example.mavmart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.collections.plus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    items: List<Listing>,
    onClose: () -> Unit,
    onOpenListing: (Long) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    var query by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    val categories = ListingCategory.entries
    var selectedCategories by remember { mutableStateOf(setOf<ListingCategory>()) }

    // Price bounds from items
    val (minCents, maxCents) = remember(items) {
        val prices = items.map { it.priceCents }
        val min = prices.minOrNull() ?: 0
        val max = prices.maxOrNull() ?: 0
        min to max
    }
    val safeMin = minCents
    val safeMax = if (maxCents <= minCents) minCents + 100 else maxCents

    // Slider in dollars
    val initialRange = remember(safeMin, safeMax) {
        (safeMin / 100f)..(safeMax / 100f)
    }
    var priceRange by remember(safeMin, safeMax) { mutableStateOf(initialRange) }

    fun inPriceRange(priceCents: Int): Boolean {
        val minSel = (priceRange.start * 100).toInt()
        val maxSel = (priceRange.endInclusive * 100).toInt()
        return priceCents in minSel..maxSel
    }

    val filtered = remember(query, selectedCategories, items, priceRange) {
        items.filter { listing ->
            val matchesQuery = query.isBlank() ||
                    listing.title.contains(query, ignoreCase = true) ||
                    (listing.description?.contains(query, ignoreCase = true) ?: false)
            val matchesCategory =
                selectedCategories.isEmpty() || selectedCategories.contains(listing.category)
            val matchesPrice = inPriceRange(listing.priceCents)
            matchesQuery && matchesCategory && matchesPrice
        }
    }

    Surface(Modifier.fillMaxSize(), color = cs.surface) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Search Listings", color = cs.primary) },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = cs.primary
                            )
                        }
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
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search listings…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        focusedLabelColor = cs.primary
                    ),
                    trailingIcon = {
                        TextButton(onClick = { showFilters = !showFilters }) {
                            Text(if (showFilters) "Hide Filters" else "Filters", color = cs.primary)
                        }
                    }
                )

                if (showFilters) {
                    Spacer(Modifier.height(8.dp))
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(containerColor = cs.surface),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Filter by Category", color = cs.primary, style = MaterialTheme.typography.titleSmall)
                            categories.forEach { cat ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = selectedCategories.contains(cat),
                                        onCheckedChange = { checked ->
                                            selectedCategories =
                                                if (checked) selectedCategories + cat else selectedCategories - cat
                                        }
                                    )
                                    Text(cat.label, color = cs.onSurface)
                                }
                            }

                            HorizontalDivider()

                            // Price Range
                            Text("Price Range", color = cs.primary, style = MaterialTheme.typography.titleSmall)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("$" + String.format(Locale.US, "%.2f", priceRange.start), color = cs.onSurface)
                                Text("$" + String.format(Locale.US, "%.2f", priceRange.endInclusive), color = cs.onSurface)
                            }

                            // Slider (in dollars)
                            RangeSlider(
                                value = priceRange,
                                onValueChange = { range ->
                                    val clampedStart = range.start.coerceIn(safeMin / 100f, safeMax / 100f)
                                    val clampedEnd = range.endInclusive.coerceIn(safeMin / 100f, safeMax / 100f)
                                    priceRange = clampedStart..clampedEnd
                                },
                                valueRange = (safeMin / 100f)..(safeMax / 100f),
                                steps = 10,
                                colors = SliderDefaults.colors(
                                    thumbColor = cs.primary,
                                    activeTrackColor = cs.primary
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No listings found", color = cs.onSurface.copy(alpha = 0.7f))
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filtered) { item ->
                            ElevatedCard(
                                colors = CardDefaults.elevatedCardColors(containerColor = cs.surface),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenListing(item.id) }
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(item.title, color = cs.primary, style = MaterialTheme.typography.titleMedium)
                                    item.description?.takeIf { it.isNotBlank() }?.let {
                                        Spacer(Modifier.height(4.dp))
                                        Text(it, color = cs.onSurface)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(formatCents(item.priceCents), color = cs.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
