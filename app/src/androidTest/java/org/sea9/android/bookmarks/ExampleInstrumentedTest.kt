package org.sea9.android.bookmarks

import android.content.Context
import android.database.SQLException
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.BeforeClass
import org.sea9.android.bookmarks.data.BookmarkRecord
import org.sea9.android.bookmarks.data.DbContract
import org.sea9.android.bookmarks.data.DbHelper
import org.sea9.android.bookmarks.data.TagRecord

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
	companion object {
		private lateinit var context: Context
		private lateinit var helper: DbHelper
		private lateinit var tags: List<TagRecord>
		private var bids = longArrayOf(-1, -1, -1, -1, -1)

		@BeforeClass
		@JvmStatic
		fun prepare() {
			context = InstrumentationRegistry.getTargetContext()
			helper = DbHelper(object : DbHelper.Caller {
				override fun getContext(): Context? {
					return context
				}

				override fun onReady() {
					Log.w("bookmarks.itest", "DB test connection ready")
				}
			})
			helper.writableDatabase.execSQL(DbContract.SQL_CONFIG)

			tags = DbContract.Tags.select(helper)
			val bookmarks = DbContract.Bookmarks.select(helper)
			if (tags.isNotEmpty() || bookmarks.isNotEmpty()) {
				if (tags.isNotEmpty()) Log.w("bookmarks.itest", "${tags.size} tags already exists")
				if (bookmarks.isNotEmpty()) Log.w("bookmarks.itest", "${bookmarks.size} bookmarks already exists")

				helper.writableDatabase.beginTransactionNonExclusive()
				try {
					helper.writableDatabase.execSQL("delete from ${DbContract.BookmarkTags.TABLE}")
					helper.writableDatabase.execSQL("delete from ${DbContract.Bookmarks.TABLE}")
					helper.writableDatabase.execSQL("delete from ${DbContract.Tags.TABLE}")
					helper.writableDatabase.setTransactionSuccessful()
				} finally {
					helper.writableDatabase.endTransaction()
				}
			}

			Log.w("bookmarks.itest", "Adding new TAGS")
			DbContract.Tags.insert(helper, "Technology")	//4
			DbContract.Tags.insert(helper, "Entertainment")	//1
			DbContract.Tags.insert(helper, "Hobbies")		//2
			DbContract.Tags.insert(helper, "Resources")		//3
			DbContract.Tags.insert(helper, "Christian")		//0
			tags = DbContract.Tags.select(helper)

			Log.w("bookmarks.itest", "Adding new BOOKMARKS")
			DbContract.Bookmarks.insert(helper,
				BookmarkRecord(
					-1,
					"https://martinfowler.com/eaaDev/EventSourcing.html",
					"Event Sourcing", mutableSetOf(tags[4]), 0
				)
			)
			DbContract.Bookmarks.insert(helper,
				BookmarkRecord(
					-1,
					"https://thinkbeforecoding.com/post/2013/07/28/Event-Sourcing-vs-Command-Sourcing",
					"Event Sourcing vs Command Sourcing", mutableSetOf(tags[4]), 0
				)
			)
			DbContract.Bookmarks.insert(helper,
				BookmarkRecord(
					-1,
					"https://medium.com/@hugo.oliveira.rocha/what-they-dont-tell-you-about-event-sourcing-6afc23c69e9a",
					"What they don’t tell you about event sourcing", mutableSetOf(tags[4]), 0
				)
			)
			DbContract.Bookmarks.insert(helper,
				BookmarkRecord(
					-1,
					"https://medium.com/the-coding-matrix/ddd-101-the-5-minute-tour-7a3037cf53b8",
					"DDD 101 — The 5-Minute Tour", mutableSetOf(tags[4]), 0
				)
			)
			DbContract.Bookmarks.insert(helper,
				BookmarkRecord(
					-1,
					"https://android.jlelse.eu/how-to-wrap-your-imperative-brain-around-functional-reactive-programming-in-rxjava-91ac89a4eccf",
					"How to wrap your imperative brain around functional reactive programming in RxJava",
					mutableSetOf(tags[4]), 0
				)
			)
			DbContract.Bookmarks.insert(helper,
				BookmarkRecord(
					-1,
					"https://gist.github.com/staltz/868e7e9bc2a7b8c1f754",
					"The introduction to Reactive Programming you've been missing",
					mutableSetOf(tags[4]), 0
				)
			)
			bids[4] = DbContract.Bookmarks.insert(helper,
				BookmarkRecord(
					"{" +
							"	'id'      : -1," +
							"	'url'     : 'https://12factor.net/'," +
							"	'title'   : 'The Twelve-Factor App'," +
							"	'category': [" +
							"		{'id' : ${tags[4].rid}, 'tag' : 'xxx'}" +
							"	]" +
							"}"
				)
			)
			DbContract.Bookmarks.insert(helper,
				BookmarkRecord(
					-1,
					"https://developer.microsoft.com/en-us/microsoft-edge/tools/vms/",
					"Free Virtual Machines from IE8 to MS Edge", mutableSetOf(tags[3], tags[4]), 0
				)
			)
			DbContract.Bookmarks.insert(helper,
				BookmarkRecord(
					-1,
					"https://www.techradar.com/news/best-garmin-running-watches",
					"Best Garmin watch 2018", mutableSetOf(tags[2], tags[4]), 0
				)
			)
			bids[3] = DbContract.Bookmarks.insert(helper,
				BookmarkRecord(
					-1,
					"http://playitagain.info/site/movie-index/",
					"Movie Index | Play It Again", mutableSetOf(tags[1]), 0
				)
			)
			bids[2] = DbContract.Bookmarks.insert(helper,
				BookmarkRecord(
					-1,
					"https://danielpocock.com/quick-start-blender-video-editing",
					"Quick start using Blender for video editing", mutableSetOf(tags[2], tags[4]), 0
				)
			)
			bids[1] = DbContract.Bookmarks.insert(helper,
				BookmarkRecord(
					-1,
					"https://www.anker.com/products/108/chargers",
					"Anker | chargers", mutableSetOf(tags[2]), 0
				)
			)
			bids[0] = DbContract.Bookmarks.insert(helper,
				BookmarkRecord(
					"{" +
							"	'id'      : -1," +
							"	'url'     : 'https://sea9.org'," +
							"	'title'   : 'SEA9.ORG'," +
							"	'category': [" +
							"		{'id' : ${tags[2].rid}, 'tag' : 'xxx', 'modified': 0}," +
							"		{'id' : ${tags[4].rid}, 'tag' : 'yyy'}" +
							"	]" +
							"}"
				)
			)
		}

//		@AfterClass
//		@JvmStatic
//		fun cleanup() {
//			helper.deleteDatabase()
//		}
	}

	@Test
	fun useAppContext() {
		// Context of the app under test.
		val appContext = InstrumentationRegistry.getTargetContext()
		assertEquals("org.sea9.android.bookmarks", appContext.packageName)
	}

	@Test
	fun testDeleteUnusedTags() {
		val count = DbContract.Tags.delete(helper)
		Log.w("bookmarks.itest.testDeleteUnusedTags", "Row deleted: $count")
		val tagList = DbContract.Tags.select(helper)
		tagList.forEachIndexed { index, tagRecord ->
			Log.w("bookmarks.itest.testDeleteUnusedTags", ">>> TAG: $index : $tagRecord")
		}
		assertTrue(tagList.size == 4)
	}

	@Test
	fun testSearchTags() {
		var result = DbContract.Tags.search(helper, "games")
		Log.w("bookmarks.itest.testPrimaryKey", "Search result 1: ${result.size}")
		assertTrue(result.isEmpty())
		result = DbContract.Tags.search(helper, "es")
		Log.w("bookmarks.itest.testPrimaryKey", "Search result 2: ${result.size}")
		assertTrue(result.size == 2)
		result = DbContract.Tags.search(helper, "TECH")
		Log.w("bookmarks.itest.testPrimaryKey", "Search result 3: ${result.size}")
		assertTrue(result.size == 1)
	}

	@Test(expected = SQLException::class)
	fun testPrimaryKey() {
		Log.w("bookmarks.itest.testPrimaryKey", "Attempt to add duplicated tag 'Resources'")
		DbContract.Tags.insert(helper, "Resources")
	}

	@Test(expected = SQLException::class)
	fun testUniqueIndexUrl() {
		Log.w("bookmarks.itest.testUniqueIndex1", "Attempt to add bookmark with duplicated URL")
		DbContract.Bookmarks.insert(helper,
			BookmarkRecord(
				"{" +
						"	'id'      : -1," +
						"	'url'     : 'https://sea9.org'," +
						"	'title'   : 'SEA9.COM'," +
						"	'category': [" +
						"		{'id' : ${tags[2].rid}, 'tag' : 'xxx', 'modified': 0}," +
						"		{'id' : ${tags[4].rid}, 'tag' : 'yyy'}" +
						"	]" +
						"}"
			)
		)
	}

	@Test(expected = SQLException::class)
	fun testUniqueIndexTitle() {
		Log.w("bookmarks.itest.testUniqueIndex2", "Attempt to add bookmark with duplicated title")
		DbContract.Bookmarks.insert(helper,
			BookmarkRecord(
				"{" +
						"	'id'      : -1," +
						"	'url'     : 'https://sea9.com'," +
						"	'title'   : 'SEA9.ORG'," +
						"	'category': [" +
						"		{'id' : ${tags[2].rid}, 'tag' : 'xxx', 'modified': 0}," +
						"		{'id' : ${tags[4].rid}, 'tag' : 'yyy'}" +
						"	]" +
						"}"
			)
		)
	}

	@Test(expected = SQLException::class)
	fun testInsertUnknownTag() {
		Log.w("bookmarks.itest.testInsertUnknownTag", "Attempt to add bookmark with an unknown tag")
		DbContract.Bookmarks.insert(helper,
			BookmarkRecord(
				"{" +
						"	'id'      : -1," +
						"	'url'     : 'https://sea9.com'," +
						"	'title'   : 'SEA9.COM'," +
						"	'category': [" +
						"		{'id' : 12345, 'tag' : 'xxx'}," +
						"		{'id' : ${tags[4].rid}, 'tag' : 'yyy'}" +
						"	]" +
						"}"
			)
		)
	}

	@Test
	fun testInsert() {
		Log.w("bookmarks.itest.testInsert", "Add a new bookmark")
		DbContract.Bookmarks.insert(helper,
			BookmarkRecord(
				"{" +
						"	'id'      : -1," +
						"	'url'     : 'https://sea9.net'," +
						"	'title'   : 'SEA9.NET'," +
						"	'category': [" +
						"		{'id' : ${tags[2].rid}, 'tag' : 'xxx', 'modified': 0}," +
						"		{'id' : ${tags[4].rid}, 'tag' : 'yyy'}" +
						"	]" +
						"}"
			)
		)

		val tagList = DbContract.Tags.select(helper)
		Log.w("bookmarks.itest.testInsert", "No. of rows returned: ${tagList.size}")
		val bookmarks = DbContract.Bookmarks.select(helper)
		Log.w("bookmarks.itest.testInsert", "No. of rows returned: ${bookmarks.size}")
		bookmarks.forEachIndexed { index, record ->
			Log.w("bookmarks.itest.testInsert", ">>> Bookmark: $index : $record")
		}

		assertTrue(tagList.size == 4 && bookmarks.size == 13)
	}

	@Test
	fun testUpdateNoChange() {
		Log.w("bookmarks.itest.testUpdateNoChange", "Update a bookmark")
		val result = DbContract.Bookmarks.update(helper,
			BookmarkRecord(
				"{" +
						"	'id'      : ${bids[4]}," +
						"	'url'     : 'https://12factor.net/'," +
						"	'title'   : 'The Twelve-Factor App'," +
						"	'category': [" +
						"		{'id' : ${tags[4].rid}, 'tag' : 'Technology'}" +
						"	]" +
						"}"
			)
		)
		assertTrue(result == 0)
	}

	@Test(expected = SQLException::class)
	fun testUpdateThrow() {
		Log.w("bookmarks.itest.testUpdateThrow", "Update a bookmark")
		val result = DbContract.Bookmarks.update(helper,
			BookmarkRecord(
				"{" +
						"	'id'      : ${bids[0]}," +
						"	'url'     : 'https://sea9.org'," +
						"	'title'   : 'Event Sourcing'," +
						"	'category': [" +
						"		{'id' : ${tags[2].rid}, 'tag' : 'Hobbies'}," +
						"		{'id' : ${tags[4].rid}, 'tag' : 'Technology'}" +
						"	]" +
						"}"
			)
		)
		assertTrue(result == 0)
	}

	@Test
	fun testUpdateBookmarkChanged() {
		Log.w("bookmarks.itest.testUpdateBookmarkChanged", "Update a bookmark")
		val result = DbContract.Bookmarks.update(helper,
			BookmarkRecord(
				"{" +
						"	'id'      : ${bids[1]}," +
						"	'url'     : 'https://www.anker.com/products/108/chargers'," +
						"	'title'   : 'Anker || chargers'," +
						"	'category': [" +
						"		{'id' : ${tags[2].rid}, 'tag' : 'Hobbies'}" +
						"	]" +
						"}"
			)
		)
		assertTrue(result == 1)
	}

	@Test
	fun testUpdateTagChanged() {
		Log.w("bookmarks.itest.testUpdateTagChanged", "Update a bookmark")
		val result = DbContract.Bookmarks.update(helper,
			BookmarkRecord(
				"{" +
						"	'id'      : ${bids[2]}," +
						"	'url'     : 'https://danielpocock.com/quick-start-blender-video-editing'," +
						"	'title'   : 'Quick start using Blender for video editing'," +
						"	'category': [" +
						"		{'id' : ${tags[2].rid}, 'tag' : 'Hobbies'}" +
						"	]" +
						"}"
			)
		)
		assertTrue(result == 1)
	}

	@Test
	fun testDeleteBookmark() {
		Log.w("bookmarks.itest.testDeleteBookmark", "Delete a bookmark")
		val result = DbContract.Bookmarks.delete(helper, bids[3])
		Log.w("bookmarks.itest.testDeleteBookmark", "Delete result $result")
		assertTrue(result == 1)
	}
}
