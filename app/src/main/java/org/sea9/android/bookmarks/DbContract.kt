package org.sea9.android.bookmarks

import android.content.ContentValues
import android.provider.BaseColumns
import java.util.*

object DbContract {
	const val DATABASE = "Bookmarks.db_contract"
	const val TBL_BOOKMARK = "Bookmarks"
	const val TBL_IDX_URL = "IdxUrl"
	const val TBL_IDX_TITLE = "idxTitle"
	const val TBL_IDX_CATEGORY = "idxCategory"
	const val COL_RID = BaseColumns._ID
	const val COL_JSON = "json"
	const val COL_URL = "url"
	const val COL_TITLE = "title"
	const val COL_CATEGORY = "category"
	const val COL_MODIFIED = "modified"
	const val SEL_RID = "$COL_RID = ?"
	const val SQL_CONFIG = "PRAGMA foreign_keys=ON"
	val COLS_BOOKMARK = arrayOf(COL_RID, COL_JSON, COL_MODIFIED)

	class Bookmarks : BaseColumns {
		companion object {
			const val SQL_CREATE =
				"create table $TBL_BOOKMARK (" +
						"$COL_RID integer primary key autoincrement," +
						"$COL_JSON text not null," +
						"$COL_MODIFIED integer)"
			const val SQL_DROP = "drop table if exists $TBL_BOOKMARK"

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
							val pid = getLong((getColumnIndexOrThrow(COL_RID)))
							val modified = getLong(getColumnIndexOrThrow(COL_MODIFIED))
							val url = getString(getColumnIndexOrThrow(COL_URL))
							val ttl = getString(getColumnIndexOrThrow(COL_TITLE))

							val rec = BookmarkRecord(pid, url, ttl, null, modified)
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
						put(COL_MODIFIED, Date().time)
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
						put(COL_MODIFIED, Date().time)
					}
					ret = db.update(TABLE, newRow, SEL_RID, args)
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
					ret = db.delete(TABLE, SEL_RID, args)
				}
				return ret
			}
		}
	}
}