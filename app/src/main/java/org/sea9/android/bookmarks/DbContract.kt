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
	const val COL_IDX = "idx"
	const val COL_JSON = "json"
	const val COL_URL = "url"
	const val COL_TITLE = "title"
	const val COL_CATEGORY = "category"
	const val COL_MODIFIED = "modified"
	const val SEL_RID = "$COL_RID = ?"
	const val SQL_CONFIG = "PRAGMA foreign_keys=ON"
	val COLS_BOOKMARK = arrayOf(COL_RID, COL_JSON, COL_MODIFIED)

	const val SQL_CREATE_BOOKMARK =
		"create table $TBL_BOOKMARK (" +
		"$COL_RID integer primary key autoincrement," +
		"$COL_JSON text not null," +
		"$COL_MODIFIED integer)"
	const val SQL_CREATE_IDX_URL =
		"create table $TBL_IDX_URL (" +
		"$COL_URL text primary key," +
		"$COL_IDX integer not null)"
	const val SQL_CREATE_IDX_TITLE =
		"create table $TBL_IDX_TITLE (" +
		"$COL_TITLE text primary key," +
		"$COL_IDX integer not null)"
	const val SQL_CREATE_IDX_CATEGORY =
		"create table $TBL_IDX_CATEGORY (" +
		"$COL_CATEGORY text not null," +
		"$COL_IDX integer not null," +
		"PRIMARY KEY ($COL_CATEGORY, $COL_IDX))"

	const val SQL_DROP_BOOKMARK = "drop table if exists $TBL_BOOKMARK"
	const val SQL_DROP_IDX_URL = "drop table if exists $TBL_IDX_URL"
	const val SQL_DROP_IDX_TITLE = "drop table if exists $TBL_IDX_TITLE"
	const val SQL_DROP_IDX_CATEGORY = "drop table if exists $TBL_IDX_CATEGORY"

	class Bookmarks : BaseColumns {
		companion object {
			/**
			 * Select all bookmarks.
			 */
			fun select(helper: DbHelper?): List<BookmarkRecord>? {
				// Not using order-by in query because keys are encrypted as well
				val cursor = helper?.readableDatabase
					?.query(TBL_BOOKMARK, COLS_BOOKMARK, null, null, null, null, null)

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