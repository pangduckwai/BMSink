package org.sea9.android.bookmarks

import android.content.Context
import android.database.SQLException
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import org.junit.AfterClass

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.BeforeClass

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
			if (tags.isEmpty()) {
				Log.w("bookmarks.itest", "Adding new TAGS")
				DbContract.Tags.insert(helper, "Technology")	//4
				DbContract.Tags.insert(helper, "Entertainment")	//1
				DbContract.Tags.insert(helper, "Hobbies")		//2
				DbContract.Tags.insert(helper, "Resources")		//3
				DbContract.Tags.insert(helper, "Christian")		//0
				tags = DbContract.Tags.select(helper)
			} else {
				Log.w("bookmarks.itest", "${tags.size} tags already exists")
			}

			val bookmarks = DbContract.Bookmarks.select(helper)
			if (bookmarks.isEmpty()) {
				Log.w("bookmarks.itest", "Adding new BOOKMARKS")
				DbContract.Bookmarks.insert(helper, BookmarkRecord(-1,
						"https://martinfowler.com/eaaDev/EventSourcing.html",
						"Event Sourcing", mutableSetOf(tags[4]), 0))
				DbContract.Bookmarks.insert(helper, BookmarkRecord( -1,
						"https://thinkbeforecoding.com/post/2013/07/28/Event-Sourcing-vs-Command-Sourcing",
						"Event Sourcing vs Command Sourcing", mutableSetOf(tags[4]), 0))
				DbContract.Bookmarks.insert(helper, BookmarkRecord(-1,
						"https://medium.com/@hugo.oliveira.rocha/what-they-dont-tell-you-about-event-sourcing-6afc23c69e9a",
						"What they don’t tell you about event sourcing", mutableSetOf(tags[4]), 0))
				DbContract.Bookmarks.insert(helper, BookmarkRecord( -1,
						"https://medium.com/the-coding-matrix/ddd-101-the-5-minute-tour-7a3037cf53b8",
						"DDD 101 — The 5-Minute Tour", mutableSetOf(tags[4]), 0))
				DbContract.Bookmarks.insert(helper, BookmarkRecord(-1,
						"https://android.jlelse.eu/how-to-wrap-your-imperative-brain-around-functional-reactive-programming-in-rxjava-91ac89a4eccf",
						"How to wrap your imperative brain around functional reactive programming in RxJava",
						mutableSetOf(tags[4]), 0))
				DbContract.Bookmarks.insert(helper, BookmarkRecord(-1,
						"https://gist.github.com/staltz/868e7e9bc2a7b8c1f754",
						"The introduction to Reactive Programming you've been missing",
						mutableSetOf(tags[4]), 0))
				DbContract.Bookmarks.insert(helper,
					BookmarkRecord("{" +
							"	'id'      : -1," +
							"	'url'     : 'https://12factor.net/'," +
							"	'title'   : 'The Twelve-Factor App'," +
							"	'category': [" +
							"		{'id' : ${tags[4].rid}, 'tag' : 'xxx'}" +
							"	]" +
							"}"))
				DbContract.Bookmarks.insert(helper, BookmarkRecord(-1,
						"https://developer.microsoft.com/en-us/microsoft-edge/tools/vms/",
						"Free Virtual Machines from IE8 to MS Edge", mutableSetOf(tags[3], tags[4]), 0))
				DbContract.Bookmarks.insert(helper, BookmarkRecord(-1,
						"https://www.techradar.com/news/best-garmin-running-watches",
						"Best Garmin watch 2018", mutableSetOf(tags[2], tags[4]), 0))
				DbContract.Bookmarks.insert(helper, BookmarkRecord(-1,
						"http://playitagain.info/site/movie-index/",
						"Movie Index | Play It Again", mutableSetOf(tags[1]), 0))
				DbContract.Bookmarks.insert(helper, BookmarkRecord(-1,
						"https://danielpocock.com/quick-start-blender-video-editing",
						"Quick start using Blender for video editing", mutableSetOf(tags[2], tags[4]), 0))
				DbContract.Bookmarks.insert(helper, BookmarkRecord(-1,
						"https://www.anker.com/products/108/chargers",
						"Anker | chargers", mutableSetOf(tags[2]), 0))
				DbContract.Bookmarks.insert(helper,
					BookmarkRecord("{" +
							"	'id'      : -1," +
							"	'url'     : 'https://sea9.org'," +
							"	'title'   : 'SEA9.ORG'," +
							"	'category': [" +
							"		{'id' : ${tags[2].rid}, 'tag' : 'xxx', 'modified': 0}," +
							"		{'id' : ${tags[4].rid}, 'tag' : 'yyy'}" +
							"	]" +
							"}"))
			} else {
				Log.w("bookmarks.itest", "${bookmarks.size} bookmarks already exists")
			}
		}

		@AfterClass
		@JvmStatic
		fun cleanup() {
			helper.deleteDatabase()
		}
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

	@Test(expected = SQLException::class)
	fun testPrimaryKey() {
		Log.w("bookmarks.itest.testPrimaryKey", "Attempt to add duplicated tag 'Resources'")
		DbContract.Tags.insert(helper, "Resources")
	}

	@Test(expected = SQLException::class)
	fun testUniqueIndexUrl() {
		Log.w("bookmarks.itest.testUniqueIndex1", "Attempt to add bookmark with duplicated URL")
		DbContract.Bookmarks.insert(helper,
			BookmarkRecord("{" +
					"	'id'      : -1," +
					"	'url'     : 'https://sea9.org'," +
					"	'title'   : 'SEA9.COM'," +
					"	'category': [" +
					"		{'id' : ${tags[2].rid}, 'tag' : 'xxx', 'modified': 0}," +
					"		{'id' : ${tags[4].rid}, 'tag' : 'yyy'}" +
					"	]" +
					"}")
		)
	}

	@Test(expected = SQLException::class)
	fun testUniqueIndexTitle() {
		Log.w("bookmarks.itest.testUniqueIndex2", "Attempt to add bookmark with duplicated title")
		DbContract.Bookmarks.insert(helper,
			BookmarkRecord("{" +
					"	'id'      : -1," +
					"	'url'     : 'https://sea9.com'," +
					"	'title'   : 'SEA9.ORG'," +
					"	'category': [" +
					"		{'id' : ${tags[2].rid}, 'tag' : 'xxx', 'modified': 0}," +
					"		{'id' : ${tags[4].rid}, 'tag' : 'yyy'}" +
					"	]" +
					"}")
		)
	}

	@Test(expected = SQLException::class)
	fun testInsertUnknownTag() {
		Log.w("bookmarks.itest.testInsertUnknownTag", "Attempt to add bookmark with an unknown tag")
		DbContract.Bookmarks.insert(helper,
			BookmarkRecord("{" +
					"	'id'      : -1," +
					"	'url'     : 'https://sea9.com'," +
					"	'title'   : 'SEA9.COM'," +
					"	'category': [" +
					"		{'id' : 12345, 'tag' : 'xxx'}," +
					"		{'id' : ${tags[4].rid}, 'tag' : 'yyy'}" +
					"	]" +
					"}")
		)
	}

	@Test
	fun testInsert() {
		Log.w("bookmarks.itest.testInsert", "Add a new bookmark")
		DbContract.Bookmarks.insert(helper,
			BookmarkRecord("{" +
					"	'id'      : -1," +
					"	'url'     : 'https://sea9.net'," +
					"	'title'   : 'SEA9.NET'," +
					"	'category': [" +
					"		{'id' : ${tags[2].rid}, 'tag' : 'xxx', 'modified': 0}," +
					"		{'id' : ${tags[4].rid}, 'tag' : 'yyy'}" +
					"	]" +
					"}")
		)

		val tagList = DbContract.Tags.select(helper)
		Log.w("bookmarks.itest.testInsert", "No. of rows returned: ${tagList.size}")
		val bookmarks = DbContract.Bookmarks.select(helper)
		Log.w("bookmarks.itest.testInsert", "No. of rows returned: ${bookmarks.size}")
		bookmarks.forEachIndexed { index, record ->
			Log.w("bookmarks.itest.testInsert", ">>> TAG: $index : $record")
		}

		assertTrue(tagList.size == 4 && bookmarks.size == 14)
	}
}
