package org.sea9.android.bookmarks

import android.content.ContentValues
import android.provider.BaseColumns
import java.util.*

object DbContract {
	const val DATABASE = "Bookmarks.db_contract"
	const val PKEY = BaseColumns._ID
	const val COMMON_MODF = "modified"
	const val COMMON_PKEY = "$PKEY = ?"
	const val SQL_CONFIG = "PRAGMA foreign_keys=ON"

	class Bookmarks : BaseColumns {
		companion object {
			private const val TABLE = "Bookmarks"
			private const val COL_URL = "url"
			private const val COL_TITLE = "title"
			private const val IDX_URL = "idxUrl"
			private const val IDX_TITLE = "idxTitle"

			private val COLUMNS = arrayOf(PKEY, COL_URL, COL_TITLE, COMMON_MODF)

			const val SQL_CREATE =
				"create table $TABLE (" +
						"$PKEY integer primary key autoincrement," +
						"$COL_URL text not null," +
						"$COL_TITLE text not null," +
						"$COMMON_MODF integer)"
			const val SQL_DROP = "drop table if exists $TABLE"
			const val SQL_CREATE_IDX_URL = "create unique index $IDX_URL on $TABLE ($COL_URL)"
			const val SQL_DROP_IDX_URL = "drop index if exists $IDX_URL"
			const val SQL_CREATE_IDX_TITLE = "create unique index $IDX_TITLE on $TABLE ($COL_TITLE)"
			const val SQL_DROP_IDX_TITLE = "drop index if exists $IDX_TITLE"

			/**
			 * Select all bookmarks.
			 */
			fun select(helper: DbHelper?): List<BookmarkRecord>? {
				// Not using order-by in query because keys are encrypted as well
				val cursor = helper?.readableDatabase
					?.query(TABLE, COLUMNS, null, null, null, null, "$COL_TITLE")

				val result = mutableSetOf<BookmarkRecord>()
				cursor?.use { c ->
					with(c) {
						while (moveToNext()) {
							val pid = getLong((getColumnIndexOrThrow(PKEY)))
							val modified = getLong(getColumnIndexOrThrow(COMMON_MODF))
							val url = getString(getColumnIndexOrThrow(COL_URL))
							val ttl = getString(getColumnIndexOrThrow(COL_TITLE))

							val rec = BookmarkRecord(pid, url, ttl, modified)
							result.add(rec)
						}
					}
				}
				return result.toMutableList()
			}

			/**
			 * Insert a new bookmark.
			 */
			fun insert(helper: DbHelper?, url: String, title: String): Long? {
				val db = helper?.writableDatabase
				if (db != null) {
					val newRow = ContentValues().apply {
						put(COL_URL, url)
						put(COL_TITLE, title)
						put(COMMON_MODF, Date().time)
					}
					return db.insertOrThrow(TABLE, null, newRow)
				}
				return null
			}

			/**
			 * Update the title of a bookmark.
			 */
			fun update(helper: DbHelper?, bid: Long, title: String): Int {
				val args = arrayOf(bid.toString())
				val db = helper?.writableDatabase
				var ret = -1

				if (db != null) {
					val newRow = ContentValues().apply {
						put(COL_TITLE, title)
						put(COMMON_MODF, Date().time)
					}
					ret = db.update(TABLE, newRow, COMMON_PKEY, args)
				}
				return ret
			}

			/**
			 * Delete a bookmark.
			 */
			fun delete(helper: DbHelper?, bid: Long): Int {
				val args = arrayOf(bid.toString())
				val db = helper?.writableDatabase
				var ret = -1

				if (db != null) {
					ret = db.delete(TABLE, COMMON_PKEY, args)
				}
				return ret
			}
		}
	}
}