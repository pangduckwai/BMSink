package org.sea9.android.bookmarks.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import java.lang.RuntimeException
import java.util.*

object DbContract {
	const val DATABASE = "Bookmarks.db_contract"
	const val PKEY = BaseColumns._ID
	const val COMMON_MODF = "modified"
	const val COMMON_PKEY = "$PKEY = ?"
	const val SQL_CONFIG = "PRAGMA foreign_keys=ON"

	class Tags : BaseColumns {
		companion object {
			const val TABLE = "Tags"
			const val COL_TAG_NAME = "tagName"
			private const val IDX_TAG = "idxTag"

			private val COLUMNS = arrayOf(
				PKEY,
				COL_TAG_NAME,
				COMMON_MODF
			)

			const val SQL_CREATE =
				"create table $TABLE (" +
				"$PKEY integer primary key autoincrement," +
				"$COL_TAG_NAME text not null COLLATE NOCASE," +
				"$COMMON_MODF integer)"
			const val SQL_CREATE_IDX = "create unique index $IDX_TAG on $TABLE ($COL_TAG_NAME)"
			const val SQL_DROP = "drop table if exists $TABLE"
			const val SQL_DROP_IDX = "drop index if exists $IDX_TAG"

			private const val QUERY_SEARCH =
				"select $PKEY from $TABLE where $COL_TAG_NAME like ?"

			private const val QUERY_DELETE =
				"delete from $TABLE where $PKEY not in " +
				"(select nt.${BookmarkTags.COL_TID} from ${BookmarkTags.TABLE} as nt inner join $TABLE as t on t.$PKEY = nt.${BookmarkTags.COL_TID})"

			/**
			 * Select all tags.
			 */
			fun select(helper: DbHelper): List<TagRecord> {
				val cursor = helper.readableDatabase
					.query(
						TABLE,
						COLUMNS, null, null, null, null,
						COL_TAG_NAME
					)

				val result = mutableListOf<TagRecord>()
				cursor.use {
					with(it) {
						while (moveToNext()) {
							val rid = getLong(getColumnIndexOrThrow(PKEY))
							val name = getString(getColumnIndexOrThrow(COL_TAG_NAME))
							val modified = getLong(getColumnIndexOrThrow(COMMON_MODF))
							val item = TagRecord(rid, name, modified)
							result.add(item)
						}
					}
				}
				return result
			}

			/**
			 * Search by tag name if a record already exists or not.
			 */
			fun search(helper: DbHelper, tagName: String): List<Long> {
				val args = arrayOf("%$tagName%")
				val cursor = helper.readableDatabase.rawQuery(QUERY_SEARCH, args)

				val result = mutableListOf<Long>()
				cursor.use {
					with(it) {
						while (moveToNext()) {
							val rid = getLong(getColumnIndexOrThrow(PKEY))
							result.add(rid)
						}
					}
				}
				return result
			}

			/**
			 * Insert a tag.
			 */
			fun insert(helper: DbHelper, tagName: String): Long {
				val newRow = ContentValues().apply {
					put(COL_TAG_NAME, tagName)
					put(COMMON_MODF, Date().time)
				}
				return helper.writableDatabase.insertOrThrow(TABLE, null, newRow)
			}

			/**
			 * Delete any unused tags.
			 */
			fun delete(helper: DbHelper): Int {
				return helper.writableDatabase.compileStatement(QUERY_DELETE).executeUpdateDelete()
			}
		}
	}

	class Bookmarks : BaseColumns {
		companion object {
			const val TABLE = "Bookmarks"
			private const val COL_URL = "url"
			private const val COL_TITLE = "title"
			private const val COL_MODF = "modf"
			private const val IDX_URL = "idxUrl"
			private const val IDX_TITLE = "idxTitle"

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

			private const val QUERY_CONTENT =
				"select b.$PKEY, b.$COL_TITLE, b.$COL_URL, b.$COMMON_MODF" +
				"     , bt.${BookmarkTags.COL_TID}, t.${Tags.COL_TAG_NAME}, t.$COMMON_MODF as $COL_MODF" +
				"  from $TABLE as b" +
				" left outer join (" +
				"    ${BookmarkTags.TABLE} as bt" +
				"    inner join ${Tags.TABLE} as t on t.$PKEY = bt.${BookmarkTags.COL_TID}" +
				" ) on bt.${BookmarkTags.COL_BID} = b.$PKEY"


			/**
			 * Select bookmarks.
			 */
			fun select(helper: DbHelper): List<BookmarkRecord> {
				return select(
					helper.readableDatabase.rawQuery(
						QUERY_CONTENT + " order by b.$COL_TITLE, t.${Tags.COL_TAG_NAME}",
						null
					)
				)
			}
			fun select(helper: DbHelper, bid: Long): BookmarkRecord? {
				val result = select(
					helper.readableDatabase.rawQuery(
						QUERY_CONTENT + " where b.$PKEY = ? order by  t.${Tags.COL_TAG_NAME}",
						arrayOf(bid.toString())
					)
				)
				return if (result.isNotEmpty())
					result[0]
				else
					null
			}
			private fun select(cursor: Cursor): List<BookmarkRecord> {
				val result = mutableMapOf<String, BookmarkRecord>()
				cursor.use { c ->
					with(c) {
						while (moveToNext()) {
							val url = getString(getColumnIndexOrThrow(COL_URL))
							val tid = getLong(getColumnIndexOrThrow(BookmarkTags.COL_TID))
							val tag = getString(getColumnIndexOrThrow(Tags.COL_TAG_NAME))
							val mod = getLong(getColumnIndexOrThrow(COL_MODF))
							val trec = tag?.let { TagRecord(tid, tag, mod) }

							if (result.containsKey(url)) {
								trec?.let {
									result[url]?.category?.add(it)
								}
							} else {
								val pid = getLong((getColumnIndexOrThrow(PKEY)))
								val ttl = getString(getColumnIndexOrThrow(COL_TITLE))
								val modified = getLong(getColumnIndexOrThrow(COMMON_MODF))
								val cat = trec?.let {
									mutableSetOf(it)
								}
								result[url] =
										BookmarkRecord(pid, url, ttl, cat, modified)
							}
						}
					}
				}
				return result.values.toMutableList()
			}

			/**
			 * Insert a new bookmark.
			 */
			fun insert(helper: DbHelper, record: BookmarkRecord): Long {
				val db = helper.writableDatabase

				db.beginTransactionNonExclusive()
				try {
					val newRow = ContentValues().apply {
						put(COL_URL, record.url)
						put(COL_TITLE, record.title)
						put(COMMON_MODF, Date().time)
					}
					val bid = db.insertOrThrow(TABLE, null, newRow)
					if (bid >= 0) {
						record.category?.forEach { tag ->
							if (BookmarkTags.insert(
									helper,
									bid,
									tag.rid
								) < 0)
								throw RuntimeException("Persisting tag ${tag.rid} failed")
						}
						db.setTransactionSuccessful()
					}
					return bid
				} finally {
					db.endTransaction()
				}
			}

			/**
			 * Update the bookmark.
			 */
			fun update(helper: DbHelper, record: BookmarkRecord): Int {
				val rec = select(helper, record.rid) ?: return -1
				val args = arrayOf(record.rid.toString())
				val db = helper.writableDatabase
				db.beginTransactionNonExclusive()
				try {
					val newRow = ContentValues().apply {
						if (record.url != rec.url) put(COL_URL, record.url)
						if (record.title != rec.title) put(COL_TITLE, record.title)
						if (size() > 0) put(COMMON_MODF, Date().time)
					}
					var ret = if (newRow.size() > 0) db.update(
						TABLE, newRow,
						COMMON_PKEY, args) else 0

					if (ret >= 0) {
						if (((record.category != null) && (rec.category == null)) ||
							((record.category == null) && (rec.category != null)) ||
							((record.category != null) && (rec.category != null) && (record.category != rec.category))) {
							val count = db.delete(BookmarkTags.TABLE, "${BookmarkTags.COL_BID} = ?", args)
							if (count >= 0) {
								var error = false
								record.category?.forEach { tag ->
									if (BookmarkTags.insert(
											helper,
											record.rid,
											tag.rid
										) < 0)
										error = true
								}
								ret = if (error) -2 else 1
							}
						}
						if (ret > 0) db.setTransactionSuccessful()
					}
					return ret
				} finally {
					db.endTransaction()
				}
			}

			/**
			 * Delete a bookmark.
			 */
			fun delete(helper: DbHelper, bid: Long): Int {
				val args = arrayOf(bid.toString())
				val db = helper.writableDatabase

				db.beginTransactionNonExclusive()
				try {
					val count = db.delete(BookmarkTags.TABLE, "${BookmarkTags.COL_BID} = ?", args)
					if (count >= 0) {
						val ret = db.delete(
							TABLE,
							COMMON_PKEY, args)
						if (ret >= 0) {
							db.setTransactionSuccessful()
						}
						return ret
					}
					return -1
				} finally {
					db.endTransaction()
				}
			}
		}
	}

	class BookmarkTags : BaseColumns {
		companion object {
			const val TABLE = "BookmarkTags"
			const val COL_BID = "bookmarkId"
			const val COL_TID = "tagId"

			const val SQL_CREATE =
				"create table $TABLE (" +
				"$PKEY integer primary key autoincrement," +
				"$COL_BID integer not null," +
				"$COL_TID integer not null," +
				"$COMMON_MODF integer," +
				"foreign key($COL_BID) references ${Bookmarks.TABLE}($PKEY)," +
				"foreign key($COL_TID) references ${Tags.TABLE}($PKEY))"
			const val SQL_DROP = "drop table if exists $TABLE"

			private const val QUERY_CONTENT =
				"select bt.$COMMON_MODF" +
				"     , bt.$COL_TID, t.${Tags.COL_TAG_NAME}" +
				"     , bt.$COL_BID" +
				"  from $TABLE as bt" +
				" inner join ${Tags.TABLE} as t on bt.$COL_TID = t.$PKEY" +
				" where nt.$COL_BID = ?"

			/**
			 * Select one note by its ID and return all tags associate with it.
			 */
			fun select(helper: DbHelper, bid: Long): List<TagRecord> {
				val args = arrayOf(bid.toString())
				val cursor = helper.readableDatabase.rawQuery(
					QUERY_CONTENT +
						" order by t.${Tags.COL_TAG_NAME}", args)

				val result = mutableListOf<TagRecord>()
				cursor.use {
					with(it) {
						while (moveToNext()) {
							val tid = getLong(getColumnIndexOrThrow(COL_TID))
							val tag = getString(getColumnIndexOrThrow(Tags.COL_TAG_NAME))
							val mod = getLong(getColumnIndexOrThrow(COMMON_MODF))
							result.add(TagRecord(tid, tag, mod))
						}
					}
				}
				return result
			}

			/**
			 * Add a note/tag relationship.
			 */
			fun insert(helper: DbHelper, bid: Long, tid: Long): Long {
				return insert(
					helper.writableDatabase,
					bid,
					tid
				)
			}
			private fun insert(db: SQLiteDatabase, bid: Long, tid: Long): Long {
				val newRow = ContentValues().apply {
					put(COL_BID, bid)
					put(COL_TID, tid)
					put(COMMON_MODF, Date().time)
				}
				return db.insertOrThrow(TABLE, null, newRow)
			}

		}
	}
}