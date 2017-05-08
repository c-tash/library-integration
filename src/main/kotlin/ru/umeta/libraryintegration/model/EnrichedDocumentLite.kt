package ru.umeta.libraryintegration.model

/**
 * Created by k.kosolapov on 11/26/2015.
 */
data class EnrichedDocumentLite(
        val id: Int,
        val authorId: Int,
        val authorHash: Int,
        val titleId: Int,
        val titleHash: Int,
        val isbn: Int,
        val publishYear: Int,
        var ratio: Double) {

    fun titleToLong(): Long {
        return twoIntsToLong(titleId, titleHash)
    }

    fun authorToLong(): Long {
        return twoIntsToLong(authorId, authorHash)
    }

    fun isbnYearToLong(): Long {
        return twoIntsToLong(isbn, publishYear)
    }

    private fun twoIntsToLong(int1:Int, int2:Int): Long {
        if (int1 == -1) {

        }
        var long: Long = int1.toLong()
        long = long shl 32
        long += Integer.toUnsignedLong(int2)
        return long
    }

    fun clone(): EnrichedDocumentLite {
        return EnrichedDocumentLite(id, authorId, authorHash, titleId, titleHash, isbn, publishYear, ratio)
    }
}
