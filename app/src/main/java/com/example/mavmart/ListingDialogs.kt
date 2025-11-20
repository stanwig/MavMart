package com.example.mavmart

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddListingDialog(
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        category: ListingCategory,
        priceDollars: String,
        condition: ItemCondition,
        place: String,
        contact: String,
        photos: List<String>
    ) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ListingCategory.GENERAL) }
    var condition by remember { mutableStateOf(ItemCondition.GOOD) }
    var place by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }

    var selectedPhotos by remember { mutableStateOf<List<String>>(emptyList()) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedPhotos = uris.map { it.toString() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Listing", color = cs.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Title") },
                    textStyle = LocalTextStyle.current.copy(color = cs.onSurface),
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        focusedLabelColor = cs.primary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    textStyle = LocalTextStyle.current.copy(color = cs.onSurface),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        focusedLabelColor = cs.primary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                CategoryPicker(value = category, onChange = { category = it })
                ConditionPicker(value = condition, onChange = { condition = it })

                OutlinedTextField(
                    value = price, onValueChange = { price = it },
                    label = { Text("Price (e.g., 12.34)") },
                    textStyle = LocalTextStyle.current.copy(color = cs.onSurface),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        focusedLabelColor = cs.primary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                OutlinedTextField(
                    value = place, onValueChange = { place = it },
                    label = { Text("Meeting place (e.g. Library)") },
                    textStyle = LocalTextStyle.current.copy(color = cs.onSurface),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        focusedLabelColor = cs.primary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                OutlinedTextField(
                    value = contact, onValueChange = { contact = it },
                    label = { Text("Preferred contact (email or phone)") },
                    textStyle = LocalTextStyle.current.copy(color = cs.onSurface),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        focusedLabelColor = cs.primary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                // Add photos
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { photoPicker.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cs.primary,
                        contentColor = cs.onPrimary
                    )
                ) { Text("Add photos") }

                if (selectedPhotos.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(selectedPhotos) { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(MaterialTheme.shapes.medium)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title, description, category, price, condition, place, contact, selectedPhotos) },
                enabled = title.isNotBlank() && price.isNotBlank(),
                colors = ButtonDefaults.textButtonColors(contentColor = cs.primary)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = cs.primary)
            ) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListingDialog(
    listing: Listing,
    onDismiss: () -> Unit,
    onSave: (Listing) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    var title by remember { mutableStateOf(listing.title) }
    var description by remember { mutableStateOf(listing.description ?: "") }
    var category by remember { mutableStateOf(listing.category) }
    var condition by remember { mutableStateOf(listing.condition) }
    var price by remember { mutableStateOf((listing.priceCents / 100.0).toString()) }
    var place by remember { mutableStateOf(listing.place) }
    var contact by remember { mutableStateOf(listing.contact) }

    var photos by remember { mutableStateOf(listing.photos) }

    val addPhotos = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val newUris = uris.map { it.toString() }.filter { it.isNotBlank() }
        if (newUris.isNotEmpty()) photos = photos + newUris
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit listing", color = cs.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Title") },
                    textStyle = LocalTextStyle.current.copy(color = cs.onSurface),
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        focusedLabelColor = cs.primary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    textStyle = LocalTextStyle.current.copy(color = cs.onSurface),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        focusedLabelColor = cs.primary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                CategoryPicker(value = category, onChange = { category = it })
                ConditionPicker(value = condition, onChange = { condition = it })

                OutlinedTextField(
                    value = price, onValueChange = { price = it },
                    label = { Text("Price (e.g., 12.34)") },
                    textStyle = LocalTextStyle.current.copy(color = cs.onSurface),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        focusedLabelColor = cs.primary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                OutlinedTextField(
                    value = place, onValueChange = { place = it },
                    label = { Text("Meeting place (e.g. Library)") },
                    textStyle = LocalTextStyle.current.copy(color = cs.onSurface),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        focusedLabelColor = cs.primary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                OutlinedTextField(
                    value = contact, onValueChange = { contact = it },
                    label = { Text("Preferred contact (email or phone)") },
                    textStyle = LocalTextStyle.current.copy(color = cs.onSurface),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        focusedLabelColor = cs.primary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Text("Photos", color = cs.primary, style = MaterialTheme.typography.titleSmall)

                if (photos.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(photos, key = { it }) { uri ->
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(MaterialTheme.shapes.medium)
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier.matchParentSize()
                                )
                                // Tiny x chip in the corner
                                Surface(
                                    color = cs.surface.copy(alpha = 0.92f),
                                    shape = MaterialTheme.shapes.small,
                                    shadowElevation = 2.dp,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(2.dp)
                                ) {
                                    IconButton(
                                        onClick = { photos = photos.filter { it != uri }.toMutableList() },
                                        modifier = Modifier.size(22.dp),
                                        content = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = cs.onSurface
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = { addPhotos.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.primary)
                ) { Text("Add photos") }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cents = (price.toDoubleOrNull()?.times(100))?.toInt() ?: listing.priceCents
                    onSave(
                        listing.copy(
                            title = title.trim(),
                            description = description.ifBlank { null },
                            category = category,
                            condition = condition,
                            priceCents = cents,
                            photos = photos.toList(),
                            place = place.trim(),
                            contact = contact.trim()
                        )
                    )
                },
                enabled = title.isNotBlank() && price.isNotBlank(),
                colors = ButtonDefaults.textButtonColors(contentColor = cs.primary)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = cs.primary)
            ) { Text("Cancel") }
        }
    )
}
