package com.sebastianneubauer.jsontree

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
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

private val iconSize = 20.dp

@Composable
public fun JsonTree(
    json: String,
    initialState: TreeState = TreeState.EXPANDED,
    colors: ElementColors = ElementColors(
        keyColor = Color.Blue,
        stringValueColor = Color.Red,
        numberValueColor = Color.Green,
        booleanValueColor = Color.Yellow,
        nullValueColor = Color.Cyan,
        symbolColor = Color.Black
    ),
    icon: ImageVector = ImageVector.vectorResource(R.drawable.ic_arrow_right),
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

    jsonElement?.let {
        ElementResolver(
            key = null,
            value = it,
            state = initialState,
            colors = colors,
            icon = icon,
            isLastItem = true,
            isOuterMostItem = true
        )
    }
}

@Composable
private fun ElementResolver(
    key: String?,
    value: JsonElement,
    state: TreeState,
    colors: ElementColors,
    icon: ImageVector,
    isLastItem: Boolean,
    isOuterMostItem: Boolean = false
) {
    when (value) {
        is JsonPrimitive -> PrimitiveElement(
            key = key,
            value = value.toString(),
            keyColor = colors.keyColor,
            symbolColor = colors.symbolColor,
            valueColor = when {
                value.doubleOrNull != null ||
                    value.intOrNull != null ||
                    value.floatOrNull != null ||
                    value.longOrNull != null -> colors.numberValueColor
                value.booleanOrNull != null -> colors.booleanValueColor
                value.isString -> colors.stringValueColor
                else -> colors.nullValueColor
            },
            indent = if (isOuterMostItem) 0.dp else iconSize * 2,
            isLastItem = isLastItem
        )
        is JsonNull -> PrimitiveElement(
            key = key,
            value = value.toString(),
            keyColor = colors.keyColor,
            valueColor = colors.nullValueColor,
            symbolColor = colors.symbolColor,
            indent = if (isOuterMostItem) 0.dp else iconSize * 2,
            isLastItem = isLastItem
        )
        is JsonArray -> CollapsableElement(
            type = CollapsableType.ARRAY,
            key = key,
            initialState = state,
            indent = if (isOuterMostItem) 0.dp else iconSize,
            keyColor = colors.keyColor,
            symbolColor = colors.symbolColor,
            icon = icon,
            isLastItem = isLastItem,
        ) {
            Column {
                val entries = value.jsonArray.toList()
                entries.forEachIndexed { index, entry ->
                    ElementResolver(
                        key = null,
                        value = entry,
                        state = state,
                        colors = colors,
                        icon = icon,
                        isLastItem = index == entries.size - 1
                    )
                }
            }
        }
        is JsonObject -> CollapsableElement(
            type = CollapsableType.OBJECT,
            key = key,
            initialState = state,
            indent = if (isOuterMostItem) 0.dp else iconSize,
            keyColor = colors.keyColor,
            symbolColor = colors.symbolColor,
            icon = icon,
            isLastItem = isLastItem,
        ) {
            Column {
                val entries = value.jsonObject.entries
                entries.forEachIndexed { index, entry ->
                    ElementResolver(
                        key = entry.key,
                        value = entry.value,
                        state = state,
                        colors = colors,
                        icon = icon,
                        isLastItem = index == entries.size - 1
                    )
                }
            }
        }
    }
}

@Composable
private fun CollapsableElement(
    type: CollapsableType,
    key: String?,
    initialState: TreeState,
    indent: Dp,
    keyColor: Color,
    symbolColor: Color,
    icon: ImageVector,
    isLastItem: Boolean,
    content: @Composable () -> Unit,
) {
    var state by remember { mutableStateOf(initialState) }
    val openBracket = if (type == CollapsableType.OBJECT) "{" else "["
    val closingBracket = if (type == CollapsableType.OBJECT) "}" else "]"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    state = when (state) {
                        TreeState.COLLAPSED -> TreeState.EXPANDED
                        TreeState.EXPANDED -> TreeState.COLLAPSED
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .size(iconSize)
                    .rotate(if (state == TreeState.COLLAPSED) 0F else 90F),
                imageVector = icon,
                contentDescription = null
            )
            key?.let {
                Text(text = "\"$it\"", color = keyColor)
                Text(text = ": ", color = symbolColor)
            }
            Text(text = openBracket, color = symbolColor)
            if (state == TreeState.COLLAPSED) {
                Text(
                    text = if (!isLastItem) "$closingBracket," else closingBracket,
                    color = symbolColor
                )
            }
        }

        Column(
            modifier = Modifier
                .animateContentSize()
                .run {
                    // Using height instead of visibility to avoid leaving composition
                    // and thus losing the TreeState of child elements
                    if (state == TreeState.COLLAPSED) height(0.dp) else this
                }
        ) {
            content()

            Text(
                modifier = Modifier.padding(start = indent),
                text = if (!isLastItem) "$closingBracket," else closingBracket,
                color = symbolColor
            )
        }
    }
}

@Composable
private fun PrimitiveElement(
    key: String?,
    value: String,
    keyColor: Color,
    valueColor: Color,
    symbolColor: Color,
    indent: Dp,
    isLastItem: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.width(indent))
        key?.let {
            Text(text = "\"$it\"", color = keyColor)
            Text(text = ": ", color = symbolColor)
        }

        val coloredText = buildAnnotatedString {
            append(value)
            addStyle(SpanStyle(color = valueColor), 0, value.length)
            if (!isLastItem) {
                append(",")
                addStyle(SpanStyle(color = symbolColor), value.length + 1, value.length + 1)
            }
        }
        Text(text = coloredText)
    }
}

@Preview
@Composable
private fun JsonTreePreview() {
    Box(modifier = Modifier.background(color = Color.White)) {
        JsonTree(
            json = jsonString,
            colors = ElementColors(
                keyColor = Color.Blue,
                stringValueColor = Color.Red,
                numberValueColor = Color.Green,
                booleanValueColor = Color.Yellow,
                nullValueColor = Color.Cyan,
                symbolColor = Color.Black
            )
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
                    "hello world"
                ],
                "arrayOfObjects": [
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
    	"longString": "developing extraordinary exercises mall finnish oclc loading radios impressed outcome harvey reputation surround robinson fight hanging championship moreover kde ensures",
        "anotherBoolean": false
    }
""".trimIndent()
