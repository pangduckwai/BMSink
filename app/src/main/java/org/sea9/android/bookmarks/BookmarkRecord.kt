package org.sea9.android.bookmarks

data class BookmarkRecord(
	  var rid: Long
	, var url: String
	, var title: String
	, var category: MutableSet<TagRecord>?
	, var modified: Long
) {
	override fun equals(other: Any?): Boolean {
		val value = other as BookmarkRecord
		return (url == value.url)
	}

	override fun hashCode(): Int {
		return url.hashCode()
	}
}