package com.sebastianneubauer.jsontree.diff

import androidx.compose.runtime.Immutable
import com.sebastianneubauer.jsontree.JsonTreeElement

@Immutable
internal sealed interface JsonTreeDifferState {

    data object Loading: JsonTreeDifferState

    data class Ready(
        val originalJsonDiffElements: List<JsonDiffElement>,
        val revisedJsonDiffElements: List<JsonDiffElement>
    ): JsonTreeDifferState

    @Immutable
    sealed interface Error: JsonTreeDifferState {
        data class OriginalJsonError(val throwable: Throwable): Error
        data class RevisedJsonError(val throwable: Throwable): Error
    }

    @Immutable
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
