package com.example.mavmart

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray

class AppDatabase private constructor(ctx: Context) :
    SQLiteOpenHelper(ctx, DbContract.DB_NAME, null, DbContract.DB_VERSION) {

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppDatabase(context.applicationContext).also { INSTANCE = it }
            }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DbContract.Users.CREATE)
        db.execSQL(DbContract.Users.CREATE_INDEX_EMAIL)

        db.execSQL(DbContract.Listings.CREATE)
        db.execSQL(DbContract.Listings.CREATE_INDEX_SELLER)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // DEMO: destructive migration for any old version < 6
        if (oldVersion < 6) {
            db.execSQL("DROP TABLE IF EXISTS ${DbContract.Listings.TABLE}")
            db.execSQL("DROP TABLE IF EXISTS ${DbContract.Users.TABLE}")
            onCreate(db)
        }
    }

    /* ================= Users ================= */
    fun insertUser(user: User): Long {
        val cv = ContentValues().apply {
            put(DbContract.Users.COL_FIRST, user.first)
            put(DbContract.Users.COL_LAST,  user.last)
            put(DbContract.Users.COL_EMAIL, user.email)
            put(DbContract.Users.COL_PASSWORD, user.password)
            put(DbContract.Users.COL_ROLE, user.role.name)
        }
        return writableDatabase.insert(DbContract.Users.TABLE, null, cv)
    }

    fun getAllUsers(): List<User> {
        val out = mutableListOf<User>()
        val c = readableDatabase.query(
            DbContract.Users.TABLE,
            arrayOf(
                DbContract.Users.COL_ID,
                DbContract.Users.COL_FIRST,
                DbContract.Users.COL_LAST,
                DbContract.Users.COL_EMAIL,
                DbContract.Users.COL_PASSWORD,
                DbContract.Users.COL_ROLE
            ),
            null, null, null, null,
            "${DbContract.Users.COL_FIRST} COLLATE NOCASE ASC, ${DbContract.Users.COL_LAST} COLLATE NOCASE ASC"
        )
        c.use {
            while (it.moveToNext()) {
                out += User(
                    id = it.getLong(0),
                    first = it.getString(1),
                    last = it.getString(2),
                    email = it.getString(3),
                    password = it.getString(4),
                    role = Role.valueOf(it.getString(5))
                )
            }
        }
        return out
    }

    fun findUserByEmail(email: String): User? {
        val c = readableDatabase.query(
            DbContract.Users.TABLE,
            arrayOf(
                DbContract.Users.COL_ID,
                DbContract.Users.COL_FIRST,
                DbContract.Users.COL_LAST,
                DbContract.Users.COL_EMAIL,
                DbContract.Users.COL_PASSWORD,
                DbContract.Users.COL_ROLE
            ),
            "${DbContract.Users.COL_EMAIL}=?",
            arrayOf(email),
            null, null, null
        )
        c.use {
            if (it.moveToFirst()) {
                return User(
                    id = it.getLong(0),
                    first = it.getString(1),
                    last = it.getString(2),
                    email = it.getString(3),
                    password = it.getString(4),
                    role = Role.valueOf(it.getString(5))
                )
            }
        }
        return null
    }

    fun validateLogin(email: String, password: String, expectedRole: Role? = null): User? {
        val u = findUserByEmail(email) ?: return null
        if (u.password != password) return null
        if (expectedRole != null && u.role != expectedRole) return null
        return u
    }

    /* ================= Listings ================= */

    private fun photosToJson(photos: List<String>): String =
        JSONArray(photos).toString()

    private fun jsonToPhotos(json: String): List<String> =
        try {
            val arr = JSONArray(json)
            List(arr.length()) { i -> arr.optString(i) }.filter { it.isNotBlank() }
        } catch (_: Exception) { emptyList() }

    fun insertListing(listing: Listing): Long {
        val cv = ContentValues().apply {
            put(DbContract.Listings.COL_SELLER_ID, listing.sellerId)
            put(DbContract.Listings.COL_TITLE, listing.title)
            put(DbContract.Listings.COL_DESC, listing.description)
            put(DbContract.Listings.COL_CATEGORY, listing.category.name)
            put(DbContract.Listings.COL_PRICE_CENTS, listing.priceCents)
            put(DbContract.Listings.COL_CONDITION, listing.condition.name)
            put(DbContract.Listings.COL_PHOTOS_JSON, photosToJson(listing.photos))
            put(DbContract.Listings.COL_STATUS, listing.status.name)
            put(DbContract.Listings.COL_CREATED_AT, listing.createdAt)
        }
        return writableDatabase.insert(DbContract.Listings.TABLE, null, cv)
    }

    fun getAllListings(): List<Listing> {
        val out = mutableListOf<Listing>()
        val c = readableDatabase.query(
            DbContract.Listings.TABLE,
            arrayOf(
                DbContract.Listings.COL_ID,
                DbContract.Listings.COL_SELLER_ID,
                DbContract.Listings.COL_TITLE,
                DbContract.Listings.COL_DESC,
                DbContract.Listings.COL_CATEGORY,
                DbContract.Listings.COL_PRICE_CENTS,
                DbContract.Listings.COL_CONDITION,
                DbContract.Listings.COL_PHOTOS_JSON,
                DbContract.Listings.COL_STATUS,
                DbContract.Listings.COL_CREATED_AT
            ),
            null, null, null, null,
            "${DbContract.Listings.COL_CREATED_AT} DESC"
        )
        c.use {
            while (it.moveToNext()) {
                out += Listing(
                    id = it.getLong(0),
                    sellerId = it.getLong(1),
                    title = it.getString(2),
                    description = it.getString(3),
                    category = ListingCategory.valueOf(it.getString(4)),
                    priceCents = it.getInt(5),
                    condition = ItemCondition.valueOf(it.getString(6)),
                    photos = jsonToPhotos(it.getString(7)),
                    status = ListingStatus.valueOf(it.getString(8)),
                    createdAt = it.getLong(9)
                )
            }
        }
        return out
    }

    fun getListingsForSeller(sellerId: Long): List<Listing> {
        val out = mutableListOf<Listing>()
        val c = readableDatabase.query(
            DbContract.Listings.TABLE,
            arrayOf(
                DbContract.Listings.COL_ID,
                DbContract.Listings.COL_SELLER_ID,
                DbContract.Listings.COL_TITLE,
                DbContract.Listings.COL_DESC,
                DbContract.Listings.COL_CATEGORY,
                DbContract.Listings.COL_PRICE_CENTS,
                DbContract.Listings.COL_CONDITION,
                DbContract.Listings.COL_PHOTOS_JSON,
                DbContract.Listings.COL_STATUS,
                DbContract.Listings.COL_CREATED_AT
            ),
            "${DbContract.Listings.COL_SELLER_ID}=?",
            arrayOf(sellerId.toString()),
            null, null,
            "${DbContract.Listings.COL_CREATED_AT} DESC"
        )
        c.use {
            while (it.moveToNext()) {
                out += Listing(
                    id = it.getLong(0),
                    sellerId = it.getLong(1),
                    title = it.getString(2),
                    description = it.getString(3),
                    category = ListingCategory.valueOf(it.getString(4)),
                    priceCents = it.getInt(5),
                    condition = ItemCondition.valueOf(it.getString(6)),
                    photos = jsonToPhotos(it.getString(7)),
                    status = ListingStatus.valueOf(it.getString(8)),
                    createdAt = it.getLong(9)
                )
            }
        }
        return out
    }
}
