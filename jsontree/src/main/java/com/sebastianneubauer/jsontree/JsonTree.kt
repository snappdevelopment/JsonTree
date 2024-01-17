package com.sebastianneubauer.jsontree

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull

/**
 * Renders JSON data as a formatted tree with collapsable objects and arrays.
 * Collapsed items display the amount of child items inside them.
 *
 * @param modifier The Modifier for this Composable.
 * @param json The json data as a string.
 * @param initialState The initial state of the tree before user interaction. One of [TreeState].
 * @param colors The color palette the tree uses. [defaultLightColors], [defaultDarkColors] or a
 * custom instance of [TreeColors].
 * @param icon The icon which is shown in front of collapsable items. Default value is an arrow icon.
 * @param iconSize The size of the [icon]. This size is also used to calculate indents.
 * @param textStyle The style which is used for all texts in the tree.
 * @param onError A callback which is called when the json can't be parsed and thus won't
 * be rendered. Receives the throwable of the error.
 */
@Composable
public fun JsonTree(
    modifier: Modifier = Modifier,
    json: String,
    initialState: TreeState = TreeState.FIRST_ITEM_EXPANDED,
    colors: TreeColors = defaultLightColors,
    icon: ImageVector = ImageVector.vectorResource(R.drawable.jsontree_arrow_right),
    iconSize: Dp = 20.dp,
    textStyle: TextStyle = LocalTextStyle.current,
    onError: (Throwable) -> Unit = {}
) {
    val jsonElement: JsonElement? = remember(json) {
        runCatching {
            Json.parseToJsonElement(json)
        }.getOrElse { throwable ->
            onError(throwable)
            null
        }
    }

    var stateResetKey by remember { mutableStateOf(0) }
    LaunchedEffect(initialState) { stateResetKey += 1 }

    jsonElement?.let {
        Box(modifier = modifier) {
            ElementResolver(
                key = null,
                value = it,
                state = initialState,
                stateResetKey = stateResetKey,
                colors = colors,
                icon = icon,
                iconSize = iconSize,
                textStyle = textStyle,
                isLastItem = true,
                isOuterMostItem = true
            )
        }
    }
}

@Composable
private fun ElementResolver(
    key: String?,
    value: JsonElement,
    state: TreeState,
    stateResetKey: Int,
    colors: TreeColors,
    icon: ImageVector,
    iconSize: Dp,
    textStyle: TextStyle,
    isLastItem: Boolean,
    isOuterMostItem: Boolean = false
) {
    when (value) {
        is JsonPrimitive -> PrimitiveElement(
            key = key,
            value = value,
            colors = colors,
            textStyle = textStyle,
            indent = if (isOuterMostItem) 0.dp else iconSize * 2,
            isLastItem = isLastItem
        )
        is JsonNull -> PrimitiveElement(
            key = key,
            value = value,
            colors = colors,
            textStyle = textStyle,
            indent = if (isOuterMostItem) 0.dp else iconSize * 2,
            isLastItem = isLastItem
        )
        is JsonArray -> {
            val childElements = remember(value) {
                value.jsonArray
                    .mapIndexed { index, item -> Pair(index.toString(), item) }
                    .toMap()
            }

            var collapsableState by remember(stateResetKey) { mutableStateOf(state) }

            CollapsableElement(
                type = CollapsableType.ARRAY,
                key = key,
                childElements = childElements,
                state = collapsableState,
                stateResetKey = stateResetKey,
                indent = if (isOuterMostItem) 0.dp else iconSize,
                colors = colors,
                textStyle = textStyle,
                icon = icon,
                iconSize = iconSize,
                isLastItem = isLastItem,
                onClick = { newState -> collapsableState = newState },
            )
        }
        is JsonObject -> {
            val childElements = remember(value) {
                value.jsonObject.entries.associate { it.toPair() }
            }

            var collapsableState by remember(stateResetKey) { mutableStateOf(state) }

            CollapsableElement(
                type = CollapsableType.OBJECT,
                key = key,
                childElements = childElements,
                state = collapsableState,
                stateResetKey = stateResetKey,
                indent = if (isOuterMostItem) 0.dp else iconSize,
                colors = colors,
                textStyle = textStyle,
                icon = icon,
                iconSize = iconSize,
                isLastItem = isLastItem,
                onClick = { newState -> collapsableState = newState },
            )
        }
    }
}

@Composable
private fun CollapsableElement(
    type: CollapsableType,
    key: String?,
    childElements: Map<String, JsonElement>,
    state: TreeState,
    stateResetKey: Int,
    indent: Dp,
    colors: TreeColors,
    textStyle: TextStyle,
    icon: ImageVector,
    iconSize: Dp,
    isLastItem: Boolean,
    onClick: (TreeState) -> Unit,
) {
    val openBracket = if (type == CollapsableType.OBJECT) "{" else "["
    val closingBracket = if (type == CollapsableType.OBJECT) "}" else "]"
    val itemCount = pluralStringResource(R.plurals.jsontree_collapsable_items, childElements.size, childElements.size)

    val coloredText = remember(key, state, colors, isLastItem, itemCount, type) {
        buildAnnotatedString {
            key?.let {
                withStyle(SpanStyle(color = colors.keyColor)) {
                    append("\"$it\"")
                }
                withStyle(SpanStyle(color = colors.symbolColor)) {
                    append(": ")
                }
            }

            withStyle(SpanStyle(color = colors.symbolColor)) {
                append(openBracket)
            }

            if (state == TreeState.COLLAPSED) {
                withStyle(SpanStyle(color = colors.symbolColor)) {
                    append(itemCount)
                }

                withStyle(SpanStyle(color = colors.symbolColor)) {
                    append(if (!isLastItem) "$closingBracket," else closingBracket)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null
                ) {
                    val newState = when (state) {
                        TreeState.COLLAPSED -> TreeState.EXPANDED
                        TreeState.EXPANDED, TreeState.FIRST_ITEM_EXPANDED -> TreeState.COLLAPSED
                    }
                    onClick(newState)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .size(iconSize)
                    .graphicsLayer(rotationZ = if (state == TreeState.COLLAPSED) 0F else 90F),
                imageVector = icon,
                tint = colors.iconColor,
                contentDescription = null
            )

            Text(text = coloredText, style = textStyle)
        }

        if (state != TreeState.COLLAPSED) {
            Column {
                childElements.forEach { (key, entry) ->
                    ElementResolver(
                        key = if (type == CollapsableType.ARRAY) null else key,
                        value = entry,
                        state = TreeState.COLLAPSED,
                        stateResetKey = stateResetKey,
                        colors = colors,
                        textStyle = textStyle,
                        icon = icon,
                        iconSize = iconSize,
                        isLastItem = key == childElements.keys.lastOrNull()
                    )
                }

                Text(
                    modifier = Modifier.padding(start = indent),
                    text = if (!isLastItem) "$closingBracket," else closingBracket,
                    color = colors.symbolColor,
                    style = textStyle
                )
            }
        }
    }
}

@Composable
private fun PrimitiveElement(
    key: String?,
    value: JsonPrimitive,
    colors: TreeColors,
    textStyle: TextStyle,
    indent: Dp,
    isLastItem: Boolean,
) {
    val valueColor = remember(value) {
        when {
            value.doubleOrNull != null ||
                value.intOrNull != null ||
                value.floatOrNull != null ||
                value.longOrNull != null -> colors.numberValueColor
            value.booleanOrNull != null -> colors.booleanValueColor
            value.isString -> colors.stringValueColor
            else -> colors.nullValueColor
        }
    }

    val coloredText = remember(key, value, colors, isLastItem) {
        buildAnnotatedString {
            key?.let {
                withStyle(SpanStyle(color = colors.keyColor)) {
                    append("\"$it\"")
                }
                withStyle(SpanStyle(color = colors.symbolColor)) {
                    append(": ")
                }
            }

            withStyle(SpanStyle(color = valueColor)) {
                append(value.toString())
            }

            if (!isLastItem) {
                withStyle(SpanStyle(color = colors.symbolColor)) {
                    append(",")
                }
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Spacer(modifier = Modifier.width(indent))
        Text(text = coloredText, style = textStyle)
    }
}

@Preview
@Composable
private fun JsonTreeDarkPreview() {
    Box(modifier = Modifier.background(color = Color.Black)) {
        JsonTree(
            json = jsonString,
            colors = defaultDarkColors,
            initialState = TreeState.EXPANDED
        )
    }
}

@Preview
@Composable
private fun JsonTreeLightPreview() {
    Box(modifier = Modifier.background(color = Color.White)) {
        JsonTree(
            json = jsonString,
            colors = defaultLightColors,
            initialState = TreeState.FIRST_ITEM_EXPANDED
        )
    }
}

private val jsonString = """
    {
    	"string": "hello world",
    	"int": 42,
    	"float": 42.5,
        "boolean": true,
        "null": null,
    	"object": {
    		"string": "hello world",
    	    "int": 42,
    	    "float": 42.5,
            "boolean": true,
            "null": null,
            "nestedObject": {
                "string": "hello world",
    	        "int": 42,
    	        "float": 42.5,
                "boolean": true,
                "nestedArray": [
                    "hello world",
                    "hello world"
                ],
                "arrayOfObjects with a really long key name to create two lines": [
                    {
                        "string": "hello world"
                    },
                    {
                        "int": 42,
                        "anotherInt": 52
                    },
                    {
                    
                    }
                ]
            }
    	},
    	"topLevelArray": [
    		"hello",
    		"world"
    	],
    	"longString with a really long key name as well to create two lines": "developing extraordinary exercises mall finnish oclc loading radios impressed outcome harvey reputation surround robinson fight hanging championship moreover kde ensures",
        "anotherBoolean": false
    }
""".trimIndent()
