package com.sebastianneubauer.jsontree

import io.github.petertrr.diffutils.text.DiffRow
import io.github.petertrr.diffutils.text.DiffRowGenerator
import io.github.petertrr.diffutils.text.DiffTagGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json

internal class JsonTreeDiffer2 {

    val state = MutableStateFlow<JsonTreeDiffer2State>(JsonTreeDiffer2State.Loading)

    suspend fun diff(
        original: String,
        revised: String,
    ) {
        val originalParser = JsonTreeParser(
            json = original,
            defaultDispatcher = Dispatchers.Default,
            mainDispatcher = Dispatchers.Main
        ).also { it.init(TreeState.EXPANDED) }

        val revisedParser = JsonTreeParser(
            json = revised,
            defaultDispatcher = Dispatchers.Default,
            mainDispatcher = Dispatchers.Main
        ).also { it.init(TreeState.EXPANDED) }

        //todo  actually subscribe to the state
        val originalJsonTreeList = (originalParser.state.value as JsonTreeParserState.Ready).list
        val revisedJsonTreeList = (revisedParser.state.value as JsonTreeParserState.Ready).list

        val diffRowGenerator =  object : DiffTagGenerator {
            override fun generateClose(tag: DiffRow.Tag): String {
                return inlineDiffTagClosed
            }

            override fun generateOpen(tag: DiffRow.Tag): String {
                return inlineDiffTagOpen
            }
        }

        val diffRows = DiffRowGenerator(
            showInlineDiffs = true,
            newTag = diffRowGenerator,
            oldTag = diffRowGenerator,
        ).generateDiffRows(
            originalJsonTreeList.map { it.toDiffString() },
            revisedJsonTreeList.map { it.toDiffString() }
        )

        println(diffRows)

        val inlineDiffs = diffRows.map { diffRow ->
            if(diffRow.tag == DiffRow.Tag.CHANGE) {
                val oldLineIndices = diffRow.oldLine.trim().findBoldTagIndicesStripped()
                val newLineIndices = diffRow.newLine.trim().findBoldTagIndicesStripped()
                println("OldLine: ${diffRow.oldLine}, indices: $oldLineIndices, NewLine: ${diffRow.newLine}, indices: $newLineIndices")
                Pair(oldLineIndices, newLineIndices)
            } else {
                Pair(emptyList(), emptyList())
            }
        }

        println("InlineDiffs: $inlineDiffs")

        val strippedDiffRows = diffRows.map { diffRow ->
            diffRow.copy(
                oldLine = diffRow.oldLine.replace(inlineDiffTagOpen, "").replace(inlineDiffTagClosed, ""),
                newLine = diffRow.newLine.replace(inlineDiffTagOpen, "").replace(inlineDiffTagClosed, "")
            )
        }

        println("strippedDiffs: $strippedDiffRows")

//        val originalDiffJson = strippedDiffRows.fold("") { acc, row -> acc + row.oldLine }
//        val revisedDiffJson = strippedDiffRows.fold("") { acc, row -> acc + row.newLine }

        println(originalJsonTreeList)
        println(revisedJsonTreeList)
        val originalUsedIds = mutableListOf<String>()
        val originalDiffElements = strippedDiffRows.mapIndexed { index, diffRow ->
            when(diffRow.tag) {
                DiffRow.Tag.EQUAL -> {
                    val jsonTreeElement = originalJsonTreeList.first { jsonTreeElement ->
                        println("$diffRow -> ${jsonTreeElement.toDiffString()}")
                        diffRow.oldLine.trim() == jsonTreeElement.toDiffString() && jsonTreeElement.id !in originalUsedIds
                    }
                    originalUsedIds.add(jsonTreeElement.id)
                    JsonDiffElement.Equal(jsonTreeElement)
                }
                DiffRow.Tag.CHANGE -> {
                    val jsonTreeElement = originalJsonTreeList.first { jsonTreeElement ->
                        println(diffRow)
                        diffRow.oldLine.trim() == jsonTreeElement.toDiffString() && jsonTreeElement.id !in originalUsedIds
                    }
                    originalUsedIds.add(jsonTreeElement.id)
                    JsonDiffElement.Change(
                        jsonTreeElement = jsonTreeElement,
                        inlineDiffIndices = inlineDiffs[index].first
                    )
                }
                DiffRow.Tag.INSERT -> {
                    JsonDiffElement.Insertion(null)
                }
                DiffRow.Tag.DELETE -> {
                    val jsonTreeElement = originalJsonTreeList.first { jsonTreeElement ->
                        println(diffRow)
                        diffRow.oldLine.trim() == jsonTreeElement.toDiffString() && jsonTreeElement.id !in originalUsedIds
                    }
                    originalUsedIds.add(jsonTreeElement.id)
                    JsonDiffElement.Deletion(jsonTreeElement)
                }
            }
        }

        val revisedUsedIds = mutableListOf<String>()
        val revisedDiffElements = strippedDiffRows.mapIndexed { index, diffRow ->
            when(diffRow.tag) {
                DiffRow.Tag.EQUAL -> {
                    val jsonTreeElement = revisedJsonTreeList.first { jsonTreeElement ->
                        diffRow.newLine.trim() == jsonTreeElement.toDiffString() && jsonTreeElement.id !in revisedUsedIds
                    }
                    revisedUsedIds.add(jsonTreeElement.id)
                    JsonDiffElement.Equal(jsonTreeElement)
                }
                DiffRow.Tag.CHANGE -> {
                    val jsonTreeElement = revisedJsonTreeList.first { jsonTreeElement ->
                        diffRow.newLine.trim() == jsonTreeElement.toDiffString() && jsonTreeElement.id !in revisedUsedIds
                    }
                    revisedUsedIds.add(jsonTreeElement.id)
                    JsonDiffElement.Change(
                        jsonTreeElement = jsonTreeElement,
                        inlineDiffIndices = inlineDiffs[index].second
                    )
                }
                DiffRow.Tag.INSERT -> {
                    val jsonTreeElement = revisedJsonTreeList.first { jsonTreeElement ->
                        diffRow.newLine.trim() == jsonTreeElement.toDiffString() && jsonTreeElement.id !in revisedUsedIds
                    }
                    revisedUsedIds.add(jsonTreeElement.id)
                    JsonDiffElement.Insertion(jsonTreeElement)
                }
                DiffRow.Tag.DELETE -> JsonDiffElement.Deletion(null)
            }
        }

        println("OriginalDiffElements: $originalDiffElements")
        println("RevisedDiffElements: $revisedDiffElements")

        state.value = JsonTreeDiffer2State.Ready(
            originalJsonDiffElements = originalDiffElements,
            revisedJsonDiffElements = revisedDiffElements,
        )
    }

    private fun String.findBoldTagIndicesStripped(): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        var strippedIndex = 0
        var currentIndex = 0

        while (currentIndex < length) {
            val openTagIndex = indexOf(inlineDiffTagOpen, currentIndex)
            if (openTagIndex == -1) break

            // Add non-bold text length to stripped index
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

    internal val inlineDiffTagOpen = "<$$$$>"
    internal val inlineDiffTagClosed = "</$$$$>"

    internal sealed interface JsonDiffElement{
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

    internal enum class ChangeType {
        Change,
        Insertion,
        Deletion,
        Equal
    }
}

internal sealed interface JsonTreeDiffer2State {
    data object Loading: JsonTreeDiffer2State
    data class Ready(
        val originalJsonDiffElements: List<JsonTreeDiffer2.JsonDiffElement>,
        val revisedJsonDiffElements: List<JsonTreeDiffer2.JsonDiffElement>
    ): JsonTreeDiffer2State
}

internal fun JsonTreeElement.toDiffString(): String {
    return when(this) {
        is JsonTreeElement.Collapsable.Object -> if(key != null && parentType != JsonTreeElement.ParentType.ARRAY) {
            "\"$key\": {"
        } else {
            "{"
        }
        is JsonTreeElement.Collapsable.Array -> if(key != null && parentType != JsonTreeElement.ParentType.ARRAY) {
            "\"$key\": ["
        } else {
            "["
        }
        is JsonTreeElement.Primitive -> if(key != null && parentType != JsonTreeElement.ParentType.ARRAY) {
            "\"$key\": $value" + if(isLastItem) "" else ","
        } else {
            "$value" + if(isLastItem) "" else ","
        }
        is JsonTreeElement.EndBracket -> when(type) {
            JsonTreeElement.EndBracket.Type.ARRAY -> if (!isLastItem) "]," else "]"
            JsonTreeElement.EndBracket.Type.OBJECT -> if (!isLastItem) "}," else "}"
        }
    }
}