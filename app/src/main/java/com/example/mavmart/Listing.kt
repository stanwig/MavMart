package com.example.mavmart

enum class ListingCategory(val label: String) {
    GENERAL("GENERAL"),
    ENGINEERING("ENGINEERING"),
    PRE_MED("PRE MED"),
    HUMANITIES_ART("HUMANITIES/ART");
}

enum class ItemCondition(val label: String) {
    NEW("NEW"),
    LIKE_NEW("LIKE NEW"),
    GOOD("GOOD"),
    FAIR("FAIR"),
    POOR("POOR");
}

enum class ListingStatus {
    ACTIVE, SOLD, ARCHIVED
}

data class Listing(
    val id: Long = 0L,
    val sellerId: Long,                 // user id
    val title: String,
    val description: String?,
    val category: ListingCategory,
    val priceCents: Int,                // store money as cents
    val condition: ItemCondition,
    val photos: List<String>,           // URIs/URLs; stored as JSON in DB
    val status: ListingStatus = ListingStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis()
)