package com.sebastianneubauer.jsontree.diff

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.sebastianneubauer.jsontree.CollapsableType
import com.sebastianneubauer.jsontree.JsonTreeElement
import com.sebastianneubauer.jsontree.JsonTreeElement.Primitive.Type
import com.sebastianneubauer.jsontree.JsonTreeElement.ParentType

@Composable
internal fun rememberCollapsableDiffText(
    type: CollapsableType,
    key: String?,
    colors: JsonTreeDiffColors,
    highlightColor: Color,
    parentType: ParentType,
    diffIndices: List<Pair<Int, Int>>?,
): AnnotatedString {
    return remember(colors) {
        buildAnnotatedString {
            key?.let { key ->
                if (parentType != ParentType.ARRAY) {
                    withStyle(SpanStyle(color = colors.keyColor)) {
                        append("\"$key\"")
                    }

                    withStyle(SpanStyle(color = colors.symbolColor)) {
                        append(": ")
                    }
                }
            }

            withStyle(SpanStyle(color = colors.symbolColor)) {
                val openBracket = if (type == CollapsableType.OBJECT) "{" else "["
                append(openBracket)
            }

            diffIndices?.forEach { (start, end) ->
                addStyle(
                    style = SpanStyle(background = highlightColor),
                    start = start,
                    end = end
                )
            }
        }
    }
}

@Composable
internal fun rememberPrimitiveDiffText(
    key: String?,
    value: String,
    type: Type,
    colors: JsonTreeDiffColors,
    highlightColor: Color,
    isLastItem: Boolean,
    parentType: ParentType,
    diffIndices: List<Pair<Int, Int>>?,
): AnnotatedString {
    return remember(colors) {
        buildAnnotatedString {
            key?.let { key ->
                if (parentType != ParentType.ARRAY) {
                    withStyle(SpanStyle(color = colors.keyColor)) {
                        append("\"$key\"")
                    }

                    withStyle(SpanStyle(color = colors.symbolColor)) {
                        append(": ")
                    }
                }
            }

            val valueColor = when(type) {
                Type.STRING -> colors.stringValueColor
                Type.BOOLEAN -> colors.booleanValueColor
                Type.NUMBER -> colors.numberValueColor
                Type.OTHER -> colors.nullValueColor
            }

            withStyle(SpanStyle(color = valueColor)) {
                append(value)
            }

            if (!isLastItem) {
                withStyle(SpanStyle(color = colors.symbolColor)) {
                    append(",")
                }
            }

            diffIndices?.forEach { (start, end) ->
                addStyle(
                    style = SpanStyle(background = highlightColor),
                    start = start,
                    end = end
                )
            }
        }
    }
}

@Composable
internal fun rememberEndBracketDiffText(
    type: JsonTreeElement.EndBracket.Type,
    colors: JsonTreeDiffColors,
    isLastItem: Boolean,
): AnnotatedString {
    return remember(colors) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = colors.symbolColor)) {
                val endBracket = if (type == JsonTreeElement.EndBracket.Type.OBJECT) "}" else "]"
                append(if (!isLastItem) "$endBracket," else endBracket)
            }
        }
    }
}
