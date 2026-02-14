package com.sebastianneubauer.jsontree.diff

import com.sebastianneubauer.jsontree.JsonTreeElement

internal sealed interface JsonTreeDifferState {

    data object Loading: JsonTreeDifferState

    data class Ready(
        val originalJsonDiffElements: List<JsonDiffElement>,
        val revisedJsonDiffElements: List<JsonDiffElement>
    ): JsonTreeDifferState

    sealed interface Error: JsonTreeDifferState {
        data class OriginalJsonError(val throwable: Throwable): Error
        data class RevisedJsonError(val throwable: Throwable): Error
    }

    sealed interface JsonDiffElement{
        data class Change(
            val jsonTreeElement: JsonTreeElement,
            val inlineDiffIndices: List<Pair<Int, Int>>
        ): JsonDiffElement

        data class Insertion(
            val jsonTreeElement: JsonTreeElement?,
        ): JsonDiffElement

        data class Deletion(
            val jsonTreeElement: JsonTreeElement?,
        ): JsonDiffElement

        data class Equal(
            val jsonTreeElement: JsonTreeElement,
        ): JsonDiffElement
    }
}
