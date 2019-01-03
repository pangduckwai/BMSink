package org.sea9.android.bookmarks

data class TagRecord(
	  var rid: Long
	, var tag: String
	, var modified: Long
) {
	override fun equals(other: Any?): Boolean {
		val value = other as TagRecord
		return (tag == value.tag)
	}

	override fun hashCode(): Int {
		return tag.hashCode()
	}
}