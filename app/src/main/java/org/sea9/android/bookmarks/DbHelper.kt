package org.sea9.android.bookmarks

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DbHelper(private val caller: Caller, isTest: Boolean):
	SQLiteOpenHelper(caller.getContext()
		, DB_NAME + (if (isTest) "_test" else "")
		, null
		, DB_VERN) {
	constructor(caller: Caller): this(caller, false)

	companion object {
		const val TAG = "bookmarks.db_helper"
		const val DB_NAME = DbContract.DATABASE
		const val DB_VERN = 4
	}

	override fun close() {
		super.close()
		ready = false
	}

	override fun onCreate(db: SQLiteDatabase) {
		db.execSQL(DbContract.Bookmarks.SQL_CREATE)
		db.execSQL(DbContract.Bookmarks.SQL_CREATE_IDX_URL)
		db.execSQL(DbContract.Bookmarks.SQL_CREATE_IDX_TITLE)
		Log.i(TAG, "Database ${db.path} version ${db.version} created")
	}

	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		Log.i(TAG, "Upgrading database from version $oldVersion to $newVersion, which will destroy all old data")
		db.execSQL(DbContract.Bookmarks.SQL_DROP_IDX_TITLE)
		db.execSQL(DbContract.Bookmarks.SQL_DROP_IDX_URL)
		db.execSQL(DbContract.Bookmarks.SQL_DROP)
		onCreate(db)
	}

	fun deleteDatabase() {
		val dbName = databaseName
		writableDatabase.execSQL(DbContract.Bookmarks.SQL_DROP_IDX_TITLE)
		writableDatabase.execSQL(DbContract.Bookmarks.SQL_DROP_IDX_URL)
		writableDatabase.execSQL(DbContract.Bookmarks.SQL_DROP)
		caller.getContext()?.deleteDatabase(databaseName)
		Log.i(TAG, "Database $dbName deleted")
	}

	var ready: Boolean = false
		private set

	override fun onOpen(db: SQLiteDatabase?) {
		super.onOpen(db)
		ready = true
		caller.onReady()
	}

	/*=========================================
	 * Access interfaces to the ContextFragment
	 */
	interface Caller {
		fun getContext(): Context?
		fun onReady()
	}
}