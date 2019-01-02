package org.sea9.android.bookmarks

import android.content.ContentValues
import android.provider.BaseColumns
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

object DbContract {
	const val DATABASE = "Bookmarks.db_contract"
	const val TBL_BOOKMARK = "Bookmarks"
	const val TBL_INDEX = "Indexes"
	const val COL_RID = BaseColumns._ID
	const val COL_JSON = "json"
	const val COL_TYPE = "type"
	const val COL_VALUE = "value"
	const val COL_MODIFIED = "modified"
	const val SEL_RID = "$COL_RID = ?"
	const val SQL_CONFIG = "PRAGMA foreign_keys=ON"
	val COLS_BOOKMARK = arrayOf(COL_RID, COL_JSON, COL_MODIFIED)
	val COLS_INDEX = arrayOf(COL_TYPE, COL_VALUE, COL_RID)

	const val SQL_CREATE_BOOKMARK =
		"create table $TBL_BOOKMARK (" +
		"$COL_RID integer primary key autoincrement," +
		"$COL_JSON text not null," +
		"$COL_MODIFIED integer)"
	const val SQL_CREATE_INDEX =
		"create table $TBL_INDEX (" +
		"$COL_TYPE text not null," +
		"$COL_VALUE text not null," +
		"$COL_RID integer not null" +
		"PRIMARY KEY ($COL_TYPE, $COL_VALUE))"

	const val SQL_DROP_BOOKMARK = "drop table if exists $TBL_BOOKMARK"
	const val SQL_DROP_INDEX = "drop table if exists $TBL_INDEX"

	class Bookmarks : BaseColumns {
		companion object {
			/**
			 * Select all bookmarks.
			 */
			fun select(helper: DbHelper?): JSONArray {
				// Not using order-by in query because keys are encrypted as well
				val cursor = helper?.readableDatabase
					?.query(TBL_BOOKMARK, COLS_BOOKMARK, null, null, null, null, null)

				val result = JSONArray()
				cursor?.use { c ->
					with(c) {
						while (moveToNext()) {
							val json = getString(getColumnIndexOrThrow(COL_JSON))
							val rid = getLong((getColumnIndexOrThrow(COL_RID)))
							val modified = getLong(getColumnIndexOrThrow(COL_MODIFIED))

							val rec = JSONObject(json)
							rec.put(COL_RID, rid)
							rec.put(COL_MODIFIED, modified)
							result.put(rec)
						}
					}
				}
				return result
			}

			/**
			 * Insert a new bookmark.
			 */
			fun insert(helper: DbHelper?, json: JSONObject): Long {
				val db = helper?.writableDatabase
				if (db != null) {
					db.beginTransactionNonExclusive()
					try {
						val newRow = ContentValues().apply {
							put(COL_JSON, json.toString())
							put(COL_MODIFIED, Date().time)
						}
						val rid = db.insertOrThrow(TBL_BOOKMARK, null, newRow)
						if (rid >= 0) {
							json.keys().forEach { key ->
								json.optJSONArray(key)?.let {

								} ?: run {
									val idxRow = ContentValues().apply {
										put(COL_TYPE, key)
										put(COL_VALUE, json.optString(key))
										put(COL_RID, rid)
									}
									if (db.insertOrThrow(TBL_INDEX, null, idxRow) >= 0)
										db.setTransactionSuccessful()
								}
							}
						}

						return rid
					} finally {
						db.endTransaction()
					}
				}
				return -1
			}

			/**
			 * Update the title of a bookmark.
			 */
			fun update(helper: DbHelper?, rid: Long, json: JSONObject): Int {
				val args = arrayOf(rid.toString())
				val db = helper?.writableDatabase
				var ret = -1

				if (db != null) {
					val newRow = ContentValues().apply {
						put(COL_JSON, json.toString())
						put(COL_MODIFIED, Date().time)
					}
					ret = db.update(TBL_BOOKMARK, newRow, SEL_RID, args)
				}
				return ret
			}

			/**
			 * Delete a bookmark.
			 */
			fun delete(helper: DbHelper?, rid: Long): Int {
				val args = arrayOf(rid.toString())
				val db = helper?.writableDatabase
				var ret = -1

				if (db != null) {
					ret = db.delete(TBL_BOOKMARK, SEL_RID, args)
				}
				return ret
			}
		}
	}
}