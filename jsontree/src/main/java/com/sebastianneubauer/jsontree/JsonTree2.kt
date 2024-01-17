package com.sebastianneubauer.jsontree

import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.util.UUID

// private fun main() {
//    jsonTree2(json = nestedJson)
// }

// @Composable
public fun jsonTree2(
//    modifier: Modifier = Modifier,
    json: String,
    initialState: TreeState = TreeState.FIRST_ITEM_EXPANDED,
//    colors: TreeColors = defaultLightColors,
//    icon: ImageVector = ImageVector.vectorResource(R.drawable.jsontree_arrow_right),
//    iconSize: Dp = 20.dp,
//    textStyle: TextStyle = LocalTextStyle.current,
    onError: (Throwable) -> Unit = {}
) {
    val jsonElement: JsonElement? = // remember(json) {
        runCatching {
            Json.parseToJsonElement(json)
        }.getOrElse { throwable ->
            onError(throwable)
            null
        }
    // }

    jsonElement?.let {
        val jsonTree = it.toJsonTree(
            state = initialState,
            level = 0,
            key = null
        )
        JsonViewModel(jsonTree)
    }
}

private class JsonViewModel(
    private val jsonTree: JsonTree
) {
    private var fullList: MutableList<JsonTree> = mutableListOf()

    var renderList = mutableStateOf<List<JsonTree>>(emptyList())

    init {
        jsonTree.treeToList(fullList)
        println("Full list:")
        fullList.forEach { println(it) }

        val collapsedItems = mutableListOf<JsonTree>()
        fullList.forEach {
            when (it) {
                is JsonTree.PrimitiveElement -> {}
                is JsonTree.NullElement -> {}
                is JsonTree.ArrayElement -> {
                    if (it.state == TreeState.COLLAPSED) {
                        collapsedItems.addAll(it.children.values)
                    }
                }
                is JsonTree.ObjectElement -> {
                    if (it.state == TreeState.COLLAPSED) {
                        collapsedItems.addAll(it.children.values)
                    }
                }
            }
        }
        println()
        println("Removed items:")
        collapsedItems.forEach { println(it) }
        renderList.value = fullList.toMutableList().apply { removeAll(collapsedItems) }
        println()
        println("Render list:")
        renderList.value.forEach { println(it) }

        renderList.value = expandOrCollapseItemWithId(id = fullList[1].id)
        println()
        println("Expand item Render list:")
        renderList.value.forEach { println(it) }

        renderList.value = expandOrCollapseItemWithId(id = fullList[1].id)
        println()
        println("Collapse Render list:")
        renderList.value.forEach { println(it) }
    }

    fun JsonTree.treeToList(list: MutableList<JsonTree>) {
        when (this) {
            is JsonTree.PrimitiveElement -> list.add(this)
            is JsonTree.NullElement -> list.add(this)
            is JsonTree.ArrayElement -> {
                list.add(this)
                this.children.forEach {
                    it.value.treeToList(list)
                }
            }
            is JsonTree.ObjectElement -> {
                list.add(this)
                this.children.forEach {
                    it.value.treeToList(list)
                }
            }
        }
    }

    fun expandOrCollapseItemWithId(id: String): MutableList<JsonTree> {
        val item = fullList.first { it.id == id }
        return when (item) {
            is JsonTree.PrimitiveElement -> error("PrimitiveElement can't be clicked")
            is JsonTree.NullElement -> error("NullElement can't be clicked")
            is JsonTree.ArrayElement -> {
                when (item.state) {
                    TreeState.EXPANDED -> {
                        fullList.toMutableList().apply {
                            val index = indexOf(item)
                            remove(item)
                            add(index, item.copy(state = TreeState.COLLAPSED))
                            removeAll(item.children.values)
                        }
                    }
                    TreeState.COLLAPSED -> {
                        fullList.toMutableList().apply {
                            val index = indexOf(item)
                            remove(item)
                            add(index, item.copy(state = TreeState.EXPANDED))
                            addAll(index + 1, item.children.values)
                        }
                    }
                    TreeState.FIRST_ITEM_EXPANDED -> {
                        fullList.toMutableList().apply {
                            val index = indexOf(item)
                            remove(item)
                            add(index, item.copy(state = TreeState.COLLAPSED))
                            removeAll(item.children.values)
                        }
                    }
                }
            }
            is JsonTree.ObjectElement -> {
                when (item.state) {
                    TreeState.EXPANDED -> {
                        fullList.toMutableList().apply {
                            val index = indexOf(item)
                            remove(item)
                            add(index, item.copy(state = TreeState.COLLAPSED))
                            removeAll(item.children.values)
                        }
                    }
                    TreeState.COLLAPSED -> {
                        fullList.toMutableList().apply {
                            val index = indexOf(item)
                            remove(item)
                            add(index, item.copy(state = TreeState.EXPANDED))
                            addAll(index + 1, item.children.values)
                        }
                    }
                    TreeState.FIRST_ITEM_EXPANDED -> {
                        fullList.toMutableList().apply {
                            val index = indexOf(item)
                            remove(item)
                            add(index, item.copy(state = TreeState.COLLAPSED))
                            removeAll(item.children.values)
                        }
                    }
                }
            }
        }
    }
}

private fun JsonElement.toJsonTree(
    state: TreeState,
    level: Int,
    key: String?
): JsonTree {
    return when (this) {
        is JsonPrimitive -> {
            JsonTree.PrimitiveElement(
                id = UUID.randomUUID().toString(),
                level = level,
                key = key,
                value = this
            )
        }
        is JsonNull -> {
            JsonTree.NullElement(
                id = UUID.randomUUID().toString(),
                level = level,
                key = key,
                value = this
            )
        }
        is JsonArray -> {
            val childElements = jsonArray
                .mapIndexed { index, item -> Pair(index.toString(), item) }
                .toMap()
                .mapValues {
                    it.value.toJsonTree(
                        state = TreeState.COLLAPSED,
                        level = level + 1,
                        key = it.key
                    )
                }

            JsonTree.ArrayElement(
                id = UUID.randomUUID().toString(),
                level = level,
                state = state,
                key = key,
                children = childElements
            )
        }
        is JsonObject -> {
            val childElements = jsonObject.entries
                .associate { it.toPair() }
                .mapValues {
                    it.value.toJsonTree(
                        state = TreeState.COLLAPSED,
                        level = level + 1,
                        key = it.key
                    )
                }

            JsonTree.ObjectElement(
                id = UUID.randomUUID().toString(),
                level = level,
                state = state,
                key = key,
                children = childElements
            )
        }
    }
}

private sealed interface JsonTree {
    val id: String
    val level: Int

    data class PrimitiveElement(
        override val id: String,
        override val level: Int,
        val key: String?,
        val value: JsonPrimitive,
    ) : JsonTree

    data class NullElement(
        override val id: String,
        override val level: Int,
        val key: String?,
        val value: JsonPrimitive,
    ) : JsonTree

    data class ObjectElement(
        override val id: String,
        override val level: Int,
        val state: TreeState,
        val key: String?,
        val children: Map<String, JsonTree>,
    ) : JsonTree

    data class ArrayElement(
        override val id: String,
        override val level: Int,
        val state: TreeState,
        val key: String?,
        val children: Map<String, JsonTree>,
    ) : JsonTree
}

internal val nestedJson = """
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
