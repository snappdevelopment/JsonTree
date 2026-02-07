package com.sebastianneubauer.jsontree

import io.github.petertrr.diffutils.text.DiffRow
import io.github.petertrr.diffutils.text.DiffRowGenerator
import kotlinx.coroutines.Dispatchers

internal class JsonTreeDiffer2 {

    val original = """
        {
            "topLevelObject": {
                "string": "stringValue",
                "nestedObject": {
                    "int": 42,
                    "nestedArray": [
                        "nestedArrayValue",
                        "nestedArrayValue"
                    ],
                    "arrayOfObjects": [
                        {
                            "anotherString": "anotherStringValue"
                        },
                        {
                            "anotherInt": 52
                        }
                    ]
                }
            },
            "topLevelArray": [
                "hello",
                "world"
            ],
            "emptyObject": {
    
            }
        }
    """.trimIndent()

    val revised = """
        {
            "topLevelObject": {
                "string": "stringValue",
                "nestedObject": {
                    "nestedArray": [
                        "nestedArrayValue",
                        "nestedArrayValue"
                    ],
                    "rrayOfa": [
                        {
                            "anotherString": "anotherStringValue"
                        },
                        {
                            "anotherInt": 52
                        },
                        {
                            "anotherFloat": 5.0
                        }
                    ]
                }
            },
            "topLevelArray": [
                "hello",
                "world"
            ],
            "emptyObject": {
    
            }
        }
    """.trimIndent()

    suspend fun diff(
        original: String,
        revised: String,
    ) {
        val diffRows = DiffRowGenerator(
//            showInlineDiffs = true,
//            newTag = object : DiffTagGenerator {
//                override fun generateClose(tag: DiffRow.Tag): String {
//                    return "</b>"//if(tag == DiffRow.Tag.CHANGE) "</b>" else ""
//                }
//
//                override fun generateOpen(tag: DiffRow.Tag): String {
//                    return "<b>"//if(tag == DiffRow.Tag.CHANGE) "<b>" else ""
//                }
//            },
//            oldTag = object : DiffTagGenerator {
//                override fun generateClose(tag: DiffRow.Tag): String {
//                    return "</b>"//if(tag == DiffRow.Tag.CHANGE) "</b>" else ""
//                }
//
//                override fun generateOpen(tag: DiffRow.Tag): String {
//                    return "<b>"//if(tag == DiffRow.Tag.CHANGE) "<b>" else ""
//                }
//            },
        ).generateDiffRows(original.lines(), revised.lines())

        println(diffRows)

        val originalDiffJson = diffRows.fold("") { acc, row -> acc + row.oldLine }
        val revisedDiffJson = diffRows.fold("") { acc, row -> acc + row.newLine }

//        val originalJsonElement = Json.parseToJsonElement(originalDiffJson)
//        val revisedJsonElement = Json.parseToJsonElement(revisedDiffJson)

        val originalParser = JsonTreeParser(
            json = originalDiffJson,
            defaultDispatcher = Dispatchers.Default,
            mainDispatcher = Dispatchers.Main
        ).also { it.init(TreeState.EXPANDED) }

        val revisedParser = JsonTreeParser(
            json = revisedDiffJson,
            defaultDispatcher = Dispatchers.Default,
            mainDispatcher = Dispatchers.Main
        ).also { it.init(TreeState.EXPANDED) }

        //todo  actually subscribe to the state
        val originalJsonTreeList = (originalParser.state.value as JsonTreeParserState.Ready).list
        val revisedJsonTreeList = (revisedParser.state.value as JsonTreeParserState.Ready).list

        println(originalJsonTreeList)
        println(revisedJsonTreeList)
        val originalUsedIds = mutableListOf<String>()
        val originalDiffElements = originalJsonTreeList.map { jsonTreeElement ->
            println(jsonTreeElement.toDiffString())
            val row = diffRows.first { diffRow ->
                diffRow.oldLine.trim() == jsonTreeElement.toDiffString() && jsonTreeElement.id !in originalUsedIds
            }
            originalUsedIds.add(jsonTreeElement.id)
            JsonDiffElement(
                jsonTreeElement = jsonTreeElement,
                changeType = when(row.tag) {
                    DiffRow.Tag.EQUAL -> ChangeType.Equal
                    DiffRow.Tag.CHANGE -> ChangeType.Change
                    DiffRow.Tag.INSERT -> ChangeType.Insertion
                    DiffRow.Tag.DELETE -> ChangeType.Deletion
                }
            )
        }

        val revisedUsedIds = mutableListOf<String>()
        val revisedDiffElements = revisedJsonTreeList.map { jsonTreeElement ->
            println(jsonTreeElement.toDiffString())
            val row = diffRows.first { diffRow ->
                diffRow.newLine.trim() == jsonTreeElement.toDiffString() && jsonTreeElement.id !in revisedUsedIds
            }
            revisedUsedIds.add(jsonTreeElement.id)
            JsonDiffElement(
                jsonTreeElement = jsonTreeElement,
                changeType = when(row.tag) {
                    DiffRow.Tag.EQUAL -> ChangeType.Equal
                    DiffRow.Tag.CHANGE -> ChangeType.Change
                    DiffRow.Tag.INSERT -> ChangeType.Insertion
                    DiffRow.Tag.DELETE -> ChangeType.Deletion
                }
            )
        }
        // TODO: überlegen wie man inline diffs unterstützen kann
    }

    private fun JsonTreeElement.toDiffString(): String {
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
                JsonTreeElement.EndBracket.Type.ARRAY -> "]"
                JsonTreeElement.EndBracket.Type.OBJECT -> "}"
            }
        }
    }

    internal data class JsonDiffElement(
        val jsonTreeElement: JsonTreeElement,
        val changeType: ChangeType
    )

    internal enum class ChangeType {
        Change,
        Insertion,
        Deletion,
        Equal
    }
}