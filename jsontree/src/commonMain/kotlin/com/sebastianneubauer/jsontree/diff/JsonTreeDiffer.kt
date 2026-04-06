package com.sebastianneubauer.jsontree.diff

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.util.fastMap
import com.sebastianneubauer.jsontree.JsonTreeElement
import com.sebastianneubauer.jsontree.JsonTreeElement.ParentType
import com.sebastianneubauer.jsontree.TreeState
import com.sebastianneubauer.jsontree.diff.JsonTreeDifferState.Error
import com.sebastianneubauer.jsontree.diff.JsonTreeDifferState.JsonDiffElement
import com.sebastianneubauer.jsontree.diff.JsonTreeDifferState.Loading
import com.sebastianneubauer.jsontree.diff.JsonTreeDifferState.Ready
import com.sebastianneubauer.jsontree.util.IdGenerator
import com.sebastianneubauer.jsontree.util.toJsonTreeElement
import com.sebastianneubauer.jsontree.util.toList
import com.sebastianneubauer.jsontree.util.toRenderString
import io.github.petertrr.diffutils.text.DiffLineNormalizer
import io.github.petertrr.diffutils.text.DiffRow
import io.github.petertrr.diffutils.text.DiffRowGenerator
import io.github.petertrr.diffutils.text.DiffTagGenerator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

internal class JsonTreeDiffer(
    val defaultDispatcher: CoroutineDispatcher,
    val mainDispatcher: CoroutineDispatcher
) {
    private val differState = mutableStateOf<JsonTreeDifferState>(Loading)
    val state: State<JsonTreeDifferState> = differState

    suspend fun diff(
        original: String,
        revised: String,
        showInlineDiffs: Boolean,
    ) = withContext(defaultDispatcher) {
        val originalJsonTreeListDeferred = async { getJsonTreeList(original) }
        val revisedJsonTreeListDeferred = async { getJsonTreeList(revised) }

        val originalJsonTreeListResult = originalJsonTreeListDeferred.await()
        val revisedJsonTreeListResult = revisedJsonTreeListDeferred.await()

        val (originalJsonTreeList, revisedJsonTreeList) = when {
            originalJsonTreeListResult is ParsingResult.Failure -> {
                withContext(mainDispatcher) {
                    differState.value = Error.OriginalJsonError(originalJsonTreeListResult.throwable)
                }
                return@withContext
            }
            revisedJsonTreeListResult is ParsingResult.Failure -> {
                withContext(mainDispatcher) {
                    differState.value = Error.RevisedJsonError(revisedJsonTreeListResult.throwable)
                }
                return@withContext
            }
            else -> {
                (originalJsonTreeListResult as ParsingResult.Success).list to
                        (revisedJsonTreeListResult as ParsingResult.Success).list
            }
        }

        val originalDiffRowInput = async { originalJsonTreeList.fastMap { it.toRenderString() } }
        val revisedDiffRowInput = async { revisedJsonTreeList.fastMap { it.toRenderString() } }

        val originalJsonTreeMapDeferred = async {
            originalJsonTreeList
                .groupBy { it.toRenderString() }
                .mapValues { it.value.toMutableList() }
        }
        val revisedJsonTreeMapDeferred = async {
            revisedJsonTreeList
                .groupBy { it.toRenderString() }
                .mapValues { it.value.toMutableList() }
        }

        val diffTagGenerator = object : DiffTagGenerator {
            override fun generateClose(tag: DiffRow.Tag): String = inlineDiffTagClosed
            override fun generateOpen(tag: DiffRow.Tag): String = inlineDiffTagOpen
        }

        val diffRows = DiffRowGenerator(
            columnWidth = 0, // Needs to be 0, otherwise HTML linebreaks will be inserted in diff lines.
            showInlineDiffs = showInlineDiffs,
            lineNormalizer = DiffLineNormalizer { line -> line },
            newTag = diffTagGenerator,
            oldTag = diffTagGenerator,
        ).generateDiffRows(
            originalDiffRowInput.await(),
            revisedDiffRowInput.await()
        )

        val diffInfoDeferred = async {
            // A change is both a deletion in the original json and a insertion in the revised json
            val insertions = diffRows.count { it.tag == DiffRow.Tag.INSERT || it.tag == DiffRow.Tag.CHANGE }
            val deletions = diffRows.count { it.tag == DiffRow.Tag.DELETE || it.tag == DiffRow.Tag.CHANGE }

            JsonTreeDiffInfo(
                changeInfo = ChangeInfo(
                    insertions = insertions,
                    deletions = deletions
                )
            )
        }

        val originalJsonTreeMap = originalJsonTreeMapDeferred.await()
        val revisedJsonTreeMap = revisedJsonTreeMapDeferred.await()

        val diffElements = diffRows.fastMap { diffRow ->
            val (oldLineDiffIndices, newLineDiffIndices) = if (diffRow.tag == DiffRow.Tag.CHANGE && showInlineDiffs) {
                val oldLineIndices = diffRow.oldLine.findInlineDiffTagIndices()
                val newLineIndices = diffRow.newLine.findInlineDiffTagIndices()
                Pair(oldLineIndices, newLineIndices)
            } else {
                Pair(emptyList(), emptyList())
            }

            val strippedTagDiffRow = if (diffRow.tag != DiffRow.Tag.EQUAL && showInlineDiffs) {
                diffRow.copy(
                    oldLine = diffRow.oldLine.replace(inlineDiffTagOpen, "").replace(inlineDiffTagClosed, ""),
                    newLine = diffRow.newLine.replace(inlineDiffTagOpen, "").replace(inlineDiffTagClosed, "")
                )
            } else {
                diffRow
            }

            val originalDiffElementDeferred = async {
                getOriginalDiffElement(
                    diffRow = strippedTagDiffRow,
                    originalJsonTreeMap = originalJsonTreeMap,
                    inlineDiffsIndices = oldLineDiffIndices
                )
            }

            val revisedDiffElementDeferred = async {
                getRevisedDiffElement(
                    diffRow = strippedTagDiffRow,
                    revisedJsonTreeMap = revisedJsonTreeMap,
                    inlineDiffsIndices = newLineDiffIndices
                )
            }

            Pair(originalDiffElementDeferred.await(), revisedDiffElementDeferred.await())
        }

        val diffInfo = diffInfoDeferred.await()

        withContext(mainDispatcher) {
            differState.value = Ready(
                diffElements = diffElements,
                diffInfo = diffInfo
            )
        }
    }

    private fun getOriginalDiffElement(
        diffRow: DiffRow,
        originalJsonTreeMap: Map<String, MutableList<JsonTreeElement>>,
        inlineDiffsIndices: List<Pair<Int, Int>>
    ): JsonDiffElement {
        fun findAndRemoveJsonTreeElement(): JsonTreeElement {
            val jsonTreeElements = originalJsonTreeMap.getValue(diffRow.oldLine)
            return jsonTreeElements.removeAt(0)
        }

        return when (diffRow.tag) {
            DiffRow.Tag.EQUAL -> {
                JsonDiffElement.Equal(findAndRemoveJsonTreeElement())
            }
            DiffRow.Tag.CHANGE -> {
                JsonDiffElement.Change(
                    jsonTreeElement = findAndRemoveJsonTreeElement(),
                    inlineDiffIndices = inlineDiffsIndices
                )
            }
            DiffRow.Tag.INSERT -> {
                JsonDiffElement.Insertion(null)
            }
            DiffRow.Tag.DELETE -> {
                JsonDiffElement.Deletion(findAndRemoveJsonTreeElement())
            }
        }
    }

    private fun getRevisedDiffElement(
        diffRow: DiffRow,
        revisedJsonTreeMap: Map<String, MutableList<JsonTreeElement>>,
        inlineDiffsIndices: List<Pair<Int, Int>>
    ): JsonDiffElement {
        fun findAndRemoveJsonTreeElement(): JsonTreeElement {
            val jsonTreeElements = revisedJsonTreeMap.getValue(diffRow.newLine)
            return jsonTreeElements.removeAt(0)
        }

        return when (diffRow.tag) {
            DiffRow.Tag.EQUAL -> {
                JsonDiffElement.Equal(findAndRemoveJsonTreeElement())
            }
            DiffRow.Tag.CHANGE -> {
                JsonDiffElement.Change(
                    jsonTreeElement = findAndRemoveJsonTreeElement(),
                    inlineDiffIndices = inlineDiffsIndices
                )
            }
            DiffRow.Tag.INSERT -> {
                JsonDiffElement.Insertion(findAndRemoveJsonTreeElement())
            }
            DiffRow.Tag.DELETE -> JsonDiffElement.Deletion(null)
        }
    }

    private fun getJsonTreeList(json: String): ParsingResult {
        return runCatching {
            ParsingResult.Success(
                Json
                    .parseToJsonElement(json)
                    .toJsonTreeElement(
                        idGenerator = IdGenerator(),
                        state = TreeState.EXPANDED,
                        level = 0,
                        key = null,
                        isLastItem = true,
                        parentType = ParentType.NONE
                    )
                    .toList()
            )
        }.getOrElse {
            ParsingResult.Failure(throwable = it)
        }
    }

    private fun String.findInlineDiffTagIndices(): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        var strippedIndex = 0
        var currentIndex = 0

        while (currentIndex < length) {
            val openTagIndex = indexOf(inlineDiffTagOpen, currentIndex)
            if (openTagIndex == -1) break

            strippedIndex += (openTagIndex - currentIndex)

            val contentStart = openTagIndex + inlineDiffTagOpen.length
            val closeTagIndex = indexOf(inlineDiffTagClosed, contentStart)

            if (closeTagIndex == -1) break

            val contentLength = closeTagIndex - contentStart
            result.add(strippedIndex to (strippedIndex + contentLength))

            strippedIndex += contentLength
            currentIndex = closeTagIndex + inlineDiffTagClosed.length
        }

        return result
    }

    private sealed interface ParsingResult {
        data class Success(val list: List<JsonTreeElement>) : ParsingResult
        data class Failure(val throwable: Throwable) : ParsingResult
    }

    private val inlineDiffTagOpen = "JSON_TREE_DIFF_START_TAG"
    private val inlineDiffTagClosed = "JSON_TREE_DIFF_END_TAG"
}
