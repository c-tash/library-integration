package ru.umeta.libraryintegration.model

/**
 * Created by k.kosolapov on 11/26/2015.
 */
data class EnrichedDocumentLite(
        val id: Long,
        val authorId: Long,
        val titleId: Long,
        var nullIsbn: Boolean = true) {
    fun isbnIsNull(): Boolean {
        return nullIsbn
    }

    override fun hashCode() : Int {
        val prime = 31
        var result = id.hashCode()
        result = result*prime + authorId.hashCode()
        result = result*prime + titleId.hashCode()
        result = result*prime + nullIsbn.hashCode()
        return result
    }
}
