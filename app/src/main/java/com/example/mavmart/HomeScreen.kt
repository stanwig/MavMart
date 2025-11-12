package com.example.mavmart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private enum class HomeTab { Listings, MyListings, Profile, Cart }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentUserId: Long,
    onLogout: () -> Unit,
    onOpenListing: (Long) -> Unit,
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    onCheckout: () -> Unit
) {
    val cs = MaterialTheme.colorScheme

    var tab by remember { mutableStateOf(HomeTab.Listings) }
    var showCreate by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }

    var items by remember { mutableStateOf(emptyList<Listing>()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(tab, currentUserId) {
        items = when (tab) {
            HomeTab.Listings   -> db.getAllListingsVisible()
            HomeTab.MyListings -> db.getListingsForSeller(currentUserId)
            else               -> emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (tab) {
                            HomeTab.Listings   -> "Listings"
                            HomeTab.MyListings -> "My Listings"
                            HomeTab.Profile    -> "Profile"
                            HomeTab.Cart       -> "Cart"
                        },
                        color = cs.primary
                    )
                },
                actions = {
                    if (tab == HomeTab.Listings) {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                        Spacer(Modifier.width(4.dp))
                    }

                    if (tab == HomeTab.Profile) {
                        TextButton(
                            onClick = onToggleTheme,
                            colors = ButtonDefaults.textButtonColors(contentColor = cs.primary)
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = if (isDark) "Switch to light" else "Switch to dark"
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(if (isDark) "Light Mode" else "Dark Mode")
                        }

                        Spacer(Modifier.width(8.dp))

                        TextButton(
                            onClick = onLogout,
                            colors = ButtonDefaults.textButtonColors(contentColor = cs.primary)
                        ) {
                            Text("Logout")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Box {
                NavigationBar(containerColor = cs.surface) {
                    NavigationBarItem(
                        selected = tab == HomeTab.Listings,
                        onClick = { tab = HomeTab.Listings },
                        icon = { Icon(Icons.AutoMirrored.Outlined.ViewList, contentDescription = "Listings") },
                        label = { Text("Listings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = cs.primary,
                            selectedTextColor = cs.primary
                        )
                    )
                    NavigationBarItem(
                        selected = tab == HomeTab.MyListings,
                        onClick = { tab = HomeTab.MyListings },
                        icon = { Icon(Icons.AutoMirrored.Outlined.List, contentDescription = "My Listings") },
                        label = { Text("My Listings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = cs.primary,
                            selectedTextColor = cs.primary
                        )
                    )

                    Spacer(Modifier.weight(1f))

                    NavigationBarItem(
                        selected = tab == HomeTab.Profile,
                        onClick = { tab = HomeTab.Profile },
                        icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = cs.primary,
                            selectedTextColor = cs.primary
                        )
                    )
                    NavigationBarItem(
                        selected = tab == HomeTab.Cart,
                        onClick = { tab = HomeTab.Cart },
                        icon = { Icon(Icons.Outlined.ShoppingCart, contentDescription = "Cart") },
                        label = { Text("Cart") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = cs.primary,
                            selectedTextColor = cs.primary
                        )
                    )
                }
                FloatingActionButton(
                    onClick = { showCreate = true },
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = cs.primary,
                    contentColor = cs.onPrimary
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Create Listing")
                }

            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { inner ->
        Box(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .background(cs.background)
        ) {
            when (tab) {
                HomeTab.Listings -> {
                    ListingsFeed(
                        listings = items,
                        showAddToCart = true,
                        currentUserId = currentUserId,
                        onAddToCart = { listing ->
                            CartRepository.add(currentUserId, listing, 1)
                            scope.launch { snackbarHostState.showSnackbar("Added to cart", withDismissAction = true) }
                        },
                        onOpenListing = onOpenListing
                    )
                }
                HomeTab.MyListings -> {
                    ListingsFeed(
                        listings = items,
                        showAddToCart = false,
                        currentUserId = currentUserId,
                        onAddToCart = {},
                        onOpenListing = onOpenListing
                    )
                }
                HomeTab.Profile ->
                    ProfileScreen(
                        currentUserId = currentUserId
                    )
                HomeTab.Cart -> {
                    CartScreen(
                        currentUserId = currentUserId,
                        onCheckout = onCheckout
                    )
                }
            }
        }
    }

    if (showCreate) {
        AddListingDialog(
            onDismiss = { showCreate = false },
            onSave = { title, description, category, priceDollars, condition, place, contact, photos ->
                val priceCents = (priceDollars.toDoubleOrNull()?.times(100))?.toInt() ?: 0
                val newListing = Listing(
                    id = 0L,
                    sellerId = currentUserId,
                    title = title.trim(),
                    description = description.ifBlank { null },
                    category = category,
                    priceCents = priceCents,
                    condition = condition,
                    photos = photos,
                    status = ListingStatus.ACTIVE,
                    createdAt = System.currentTimeMillis(),
                    place = place.trim(),
                    contact = contact.trim()
                )
                db.insertListing(newListing)
                items = if (tab == HomeTab.MyListings) {
                    db.getListingsForSeller(currentUserId)
                } else {
                    db.getAllListingsVisible()
                }
                showCreate = false
            }
        )
    }

    if (showSearch) {
        SearchScreen(
            items = items,
            onClose = { showSearch = false },
            onOpenListing = { id ->
                showSearch = false
                onOpenListing(id)
            }
        )
    }
}

/* ================== Listings feed ================== */
@Composable
private fun ListingsFeed(
    listings: List<Listing>,
    showAddToCart: Boolean,
    currentUserId: Long,
    onAddToCart: (Listing) -> Unit,
    onOpenListing: (Long) -> Unit
) {
    val cs = MaterialTheme.colorScheme

    if (listings.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No listings yet.", color = cs.onSurface)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(listings) { item ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 110.dp)
                        .clickable { onOpenListing(item.id) },
                    colors = CardDefaults.elevatedCardColors(containerColor = cs.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = cs.primary
                        )

                        item.description?.takeIf { it.isNotBlank() }?.let {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = cs.onSurface
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                formatCents(item.priceCents),
                                style = MaterialTheme.typography.titleSmall,
                                color = cs.primary
                            )

                            if (showAddToCart && item.sellerId != currentUserId) {
                                Button(
                                    onClick = { onAddToCart(item) },
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
                    }
                }
            }
        }
    }
}
