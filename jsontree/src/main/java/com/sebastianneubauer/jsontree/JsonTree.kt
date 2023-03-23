package com.sebastianneubauer.jsontree

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

private val jsonString = """
    {
    	"_id": "ZTMREO26GOD0SUJV",
    	"name": "Cara Mcpherson",
    	"dob": "2020-08-12",
    	"addressObject": {
    		"street": "6410 Chislehurst Avenue",
    		"town": "Gatehouse of Fleet",
    		"postcode": 5555,
            "nestedObject": {
                "integer": 10,
                "string": "words",
                "double": 10.5,
                "boolean": false,
                "anotherArray": [
                    "hello world"
                ]
            }
    	},
    	"telephone": "+353-0817-812-287",
    	"petsArray": [
    		"bandit",
    		"Bentley"
    	],
    	"score": 7.5,
    	"email": null,
    	"url": "https://www.crucial.com",
    	"description": "developing extraordinary exercises mall finnish oclc loading radios impressed outcome harvey reputation surround robinson fight hanging championship moreover kde ensures",
    	"verified": true,
    	"salary": 46078
    }
""".trimIndent()

private enum class CollapsableType {
    OBJECT,
    ARRAY
}

public data class JsonColors(
    val keyColor: Color,
    val stringValueColor: Color,
    val numberValueColor: Color,
    val booleanValueColor: Color,
    val nullValueColor: Color,
    val symbolColor: Color,
)

@Preview
@Composable
private fun JsonTreePreview() {
    Box(modifier = Modifier.background(color = Color.White)) {
        JsonTree(
            json = jsonString,
            colors = JsonColors(
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

@Composable
public fun JsonTree(
    json: String,
    colors: JsonColors = JsonColors(
        keyColor = Color.Blue,
        stringValueColor = Color.Red,
        numberValueColor = Color.Green,
        booleanValueColor = Color.Yellow,
        nullValueColor = Color.Cyan,
        symbolColor = Color.Black
    ),
    indent: Dp = 16.dp,
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
            colors = colors,
            indent = indent,
            isLastItem = true,
            isOuterMostItem = true
        )
    }
}

@Composable
private fun ElementResolver(
    key: String?,
    value: JsonElement,
    colors: JsonColors,
    indent: Dp,
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
            indent = if (isOuterMostItem) 0.dp else indent,
            isLastItem = isLastItem
        )
        is JsonArray -> CollapsableElement(
            type = CollapsableType.ARRAY,
            key = key,
            indent = if (isOuterMostItem) 0.dp else indent,
            keyColor = colors.keyColor,
            symbolColor = colors.symbolColor,
            isLastItem = isLastItem,
        ) {
            val entries = value.jsonArray.toList()
            entries.forEachIndexed { index, entry ->
                ElementResolver(
                    key = null,
                    value = entry,
                    colors = colors,
                    indent = indent,
                    isLastItem = index == entries.size - 1
                )
            }
        }
        is JsonObject -> CollapsableElement(
            type = CollapsableType.OBJECT,
            key = key,
            indent = if (isOuterMostItem) 0.dp else indent,
            keyColor = colors.keyColor,
            symbolColor = colors.symbolColor,
            isLastItem = isLastItem,
        ) {
            val entries = value.jsonObject.entries
            entries.forEachIndexed { index, entry ->
                ElementResolver(
                    key = entry.key,
                    value = entry.value,
                    colors = colors,
                    indent = indent,
                    isLastItem = index == entries.size - 1
                )
            }
        }
        is JsonNull -> PrimitiveElement(
            key = key,
            value = value.toString(),
            keyColor = colors.keyColor,
            valueColor = colors.nullValueColor,
            symbolColor = colors.symbolColor,
            indent = if (isOuterMostItem) 0.dp else indent,
            isLastItem = isLastItem
        )
    }
}

@Composable
private fun CollapsableElement(
    type: CollapsableType,
    key: String?,
    indent: Dp,
    keyColor: Color,
    symbolColor: Color,
    isLastItem: Boolean,
    content: @Composable () -> Unit,
) {
    // Implement collapse mechanism
    val openBracket = if (type == CollapsableType.OBJECT) "{" else "["
    val closingBracket = if (type == CollapsableType.OBJECT) "}" else "]"

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.width(indent))

        Column {
            Row {
                key?.let {
                    Text(text = "\"$it\"", color = keyColor)
                    Text(text = ": ", color = symbolColor)
                }
                Text(text = openBracket, color = symbolColor)
            }
            content()
            Text(text = if (!isLastItem) "$closingBracket," else closingBracket, color = symbolColor)
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
