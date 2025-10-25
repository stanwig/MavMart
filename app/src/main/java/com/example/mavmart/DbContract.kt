package com.example.mavmart

object DbContract {
    const val DB_NAME = "mavmart.db"
    const val DB_VERSION = 6

    object Users {
        const val TABLE = "users"
        const val COL_ID = "_id"
        const val COL_FIRST = "first"
        const val COL_LAST = "last"
        const val COL_EMAIL = "email"
        const val COL_PASSWORD = "password"
        const val COL_ROLE = "role"

        const val CREATE = """
            CREATE TABLE $TABLE (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FIRST TEXT NOT NULL,
                $COL_LAST TEXT NOT NULL,
                $COL_EMAIL TEXT NOT NULL UNIQUE,
                $COL_PASSWORD TEXT NOT NULL,
                $COL_ROLE TEXT NOT NULL
            );
        """
        const val INDEX_EMAIL = "idx_users_email"
        const val CREATE_INDEX_EMAIL =
            "CREATE UNIQUE INDEX $INDEX_EMAIL ON $TABLE($COL_EMAIL);"
    }

    object Listings {
        const val TABLE = "listings"
        const val COL_ID = "_id"
        const val COL_SELLER_ID = "seller_id"
        const val COL_TITLE = "title"
        const val COL_DESC = "description"
        const val COL_CATEGORY = "category"
        const val COL_PRICE_CENTS = "price_cents"
        const val COL_CONDITION = "condition"
        const val COL_PHOTOS_JSON = "photos_json"    // JSON array in TEXT
        const val COL_STATUS = "status"
        const val COL_CREATED_AT = "created_at"

        const val CREATE = """
            CREATE TABLE $TABLE (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SELLER_ID INTEGER NOT NULL,
                $COL_TITLE TEXT NOT NULL,
                $COL_DESC TEXT,
                $COL_CATEGORY TEXT NOT NULL,
                $COL_PRICE_CENTS INTEGER NOT NULL,
                $COL_CONDITION TEXT NOT NULL,
                $COL_PHOTOS_JSON TEXT NOT NULL,
                $COL_STATUS TEXT NOT NULL,
                $COL_CREATED_AT INTEGER NOT NULL,
                FOREIGN KEY($COL_SELLER_ID) REFERENCES ${Users.TABLE}(${Users.COL_ID}) ON DELETE CASCADE
            );
        """
        const val INDEX_SELLER = "idx_listings_seller"
        const val CREATE_INDEX_SELLER =
            "CREATE INDEX $INDEX_SELLER ON $TABLE($COL_SELLER_ID);"
    }
}