package com.sebastianneubauer.jsontree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

@Composable
internal fun rememberCollapsableText(
    type: CollapsableType,
    key: String?,
    childItemCount: Int,
    state: TreeState,
    colors: TreeColors,
    isLastItem: Boolean,
    showIndices: Boolean,
    parentType: ParentType,
): AnnotatedString {
    val itemCount = pluralStringResource(R.plurals.jsontree_collapsable_items, childItemCount, childItemCount)

    return remember(key, state, colors, isLastItem, itemCount, type) {
        val openBracket = if (type == CollapsableType.OBJECT) "{" else "["
        val closingBracket = if (type == CollapsableType.OBJECT) "}" else "]"

        buildAnnotatedString {
            key?.let {
                if (parentType == ParentType.ARRAY && showIndices) {
                    withStyle(SpanStyle(color = colors.indexColor)) {
                        append(it)
                    }
                    withStyle(SpanStyle(color = colors.symbolColor)) {
                        append(": ")
                    }
                } else if (parentType != ParentType.ARRAY) {
                    withStyle(SpanStyle(color = colors.keyColor)) {
                        append("\"$it\"")
                    }
                    withStyle(SpanStyle(color = colors.symbolColor)) {
                        append(": ")
                    }
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
}

@Composable
internal fun rememberPrimitiveText(
    key: String?,
    value: JsonPrimitive,
    colors: TreeColors,
    isLastItem: Boolean,
    showIndices: Boolean,
    parentType: ParentType,
): AnnotatedString {
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

    return remember(key, value, colors, isLastItem) {
        buildAnnotatedString {
            key?.let {
                if (parentType == ParentType.ARRAY && showIndices) {
                    withStyle(SpanStyle(color = colors.indexColor)) {
                        append(it)
                    }
                    withStyle(SpanStyle(color = colors.symbolColor)) {
                        append(": ")
                    }
                } else if (parentType != ParentType.ARRAY) {
                    withStyle(SpanStyle(color = colors.keyColor)) {
                        append("\"$it\"")
                    }
                    withStyle(SpanStyle(color = colors.symbolColor)) {
                        append(": ")
                    }
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
}
