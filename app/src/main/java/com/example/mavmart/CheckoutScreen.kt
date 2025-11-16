package com.example.mavmart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    currentUserId: Long,
    onBack: () -> Unit,
    onOrderPlaced: (orderId: Long) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }

    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvc by remember { mutableStateOf("") }
    var showCvc by remember { mutableStateOf(false) }

    var cardError by remember { mutableStateOf<String?>(null) }
    var expiryError by remember { mutableStateOf<String?>(null) }
    var cvcError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var valid = true
        if (cardNumber.filter { it.isDigit() }.length != 16) {
            cardError = "Card number must be 16 digits"; valid = false
        } else cardError = null

        if (!isValidExpiry(expiry)) {
            expiryError = "Invalid expiry date (MM/YY)"; valid = false
        } else expiryError = null

        if (cvc.length != 3 || cvc.any { !it.isDigit() }) {
            cvcError = "CVC must be 3 digits"; valid = false
        } else cvcError = null
        return valid
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.surface,
                    navigationIconContentColor = cs.primary,
                    titleContentColor = cs.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                label = { Text("Card Number") },
                isError = cardError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { cardError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cs.surface,
                    unfocusedContainerColor = cs.surface,
                    focusedTextColor = cs.onSurface,
                    unfocusedTextColor = cs.onSurface,
                    focusedBorderColor = cs.primary,
                    unfocusedBorderColor = cs.outline
                )
            )

            OutlinedTextField(
                value = expiry,
                onValueChange = { expiry = it },
                label = { Text("Expiry (MM/YY)") },
                isError = expiryError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { expiryError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cs.surface,
                    unfocusedContainerColor = cs.surface,
                    focusedTextColor = cs.onSurface,
                    unfocusedTextColor = cs.onSurface,
                    focusedBorderColor = cs.primary,
                    unfocusedBorderColor = cs.outline
                )
            )

            OutlinedTextField(
                value = cvc,
                onValueChange = { cvc = it },
                label = { Text("CVC") },
                isError = cvcError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = if (showCvc) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showCvc = !showCvc }) {
                        Text(if (showCvc) "Hide" else "Show")
                    }
                },
                supportingText = { cvcError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cs.surface,
                    unfocusedContainerColor = cs.surface,
                    focusedTextColor = cs.onSurface,
                    unfocusedTextColor = cs.onSurface,
                    focusedBorderColor = cs.primary,
                    unfocusedBorderColor = cs.outline
                )
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (validate()) {
                        focusManager.clearFocus()

                        // Build order from current cart snapshot
                        val snapshot: List<Listing> = CartRepository.snapshot(currentUserId)
                        val orderId = System.currentTimeMillis()

                        val lines = snapshot.map { listing ->
                            val seller = db.getUserById(listing.sellerId)
                            OrderLine(
                                title = listing.title,
                                priceCents = listing.priceCents,
                                sellerId = listing.sellerId,
                                sellerName = seller?.let { "${it.first} ${it.last}" } ?: "Seller",
                                place = listing.place,
                                contact = listing.contact
                            )
                        }

                        val total = snapshot.sumOf { it.priceCents }

                        val order = OrderDetails(
                            orderId = orderId,
                            lines = lines,
                            totalCents = total,
                            placedAtMillis = System.currentTimeMillis()
                        )
                        OrderStore.save(currentUserId, order)

                        // Remove purchased listings from DB
                        snapshot.forEach { listing -> db.deleteListing(listing.id) }

                        // Clear cart
                        CartRepository.clear(currentUserId)

                        // Navigate to confirmation
                        onOrderPlaced(orderId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) { Text("Place Order") }
        }
    }
}

fun isValidExpiry(expiry: String): Boolean {
    val regex = Regex("^(0[1-9]|1[0-2])/[0-9]{2}$")
    if (!regex.matches(expiry)) return false

    val (monthStr, yearStr) = expiry.split("/")
    val month = monthStr.toInt()
    val year = "20$yearStr".toInt()

    val now = java.util.Calendar.getInstance()
    val currentYear = now.get(java.util.Calendar.YEAR)
    val currentMonth = now.get(java.util.Calendar.MONTH) + 1

    if (year < currentYear) return false
    if (year == currentYear && month < currentMonth) return false

    return true
}
