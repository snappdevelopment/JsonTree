package com.sebastianneubauer.jsontree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.sebastianneubauer.jsontree.JsonTreeElement.ParentType
import jsontree.jsontree.generated.resources.Res
import jsontree.jsontree.generated.resources.jsontree_collapsable_items
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import org.jetbrains.compose.resources.pluralStringResource

@Composable
internal fun rememberCollapsableText(
    type: CollapsableType,
    key: String?,
    childItemCount: Int,
    state: TreeState,
    colors: TreeColors,
    isLastItem: Boolean,
    searchOccurrence: SearchOccurrence?,
    searchOccurrenceSelectedRange: SearchOccurrence.Range?,
    showIndices: Boolean,
    showItemCount: Boolean,
    parentType: ParentType,
): AnnotatedString {
    val itemCount = pluralStringResource(Res.plurals.jsontree_collapsable_items, childItemCount, childItemCount)

    return remember(
        key,
        state,
        colors,
        isLastItem,
        itemCount,
        type,
        showIndices,
        showItemCount,
        searchOccurrence,
        searchOccurrenceSelectedRange
    ) {
        val openBracket = if (type == CollapsableType.OBJECT) "{" else "["
        val closingBracket = if (type == CollapsableType.OBJECT) "}" else "]"

        buildAnnotatedString {
            key?.let { key ->
                if (parentType == ParentType.ARRAY && showIndices) {
                    withStyle(SpanStyle(color = colors.indexColor)) {
                        append(key)
                    }
                } else if (parentType != ParentType.ARRAY) {
                    withStyle(SpanStyle(color = colors.keyColor)) {
                        append("\"$key\"")
                    }
                    // add 1 to the range because the value is rendered with quotes around it
                    // add 1 to the end because it is exclusive
                    searchOccurrence
                        ?.ranges
                        ?.filterIsInstance<SearchOccurrence.Range.Key>()
                        ?.forEach { keyRange ->
                            val color = if(keyRange == searchOccurrenceSelectedRange) Color.Blue else colors.highlightColor
                            addStyle(
                                style = SpanStyle(background = color),
                                start = keyRange.range.first + 1,
                                end = keyRange.range.last + 1 + 1
                            )
                        }
                }

                withStyle(SpanStyle(color = colors.symbolColor)) {
                    append(": ")
                }
            }

            withStyle(SpanStyle(color = colors.symbolColor)) {
                append(openBracket)
            }

            if (state == TreeState.COLLAPSED) {
                if (showItemCount) {
                    withStyle(SpanStyle(color = colors.symbolColor)) {
                        append(itemCount)
                    }
                } else {
                    withStyle(SpanStyle(color = colors.symbolColor)) {
                        append(" ... ")
                    }
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
    searchOccurrence: SearchOccurrence?,
    searchOccurrenceSelectedRange: SearchOccurrence.Range?,
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

    return remember(
        key,
        value,
        colors,
        isLastItem,
        showIndices,
        searchOccurrence,
        searchOccurrenceSelectedRange
    ) {
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
                    // add 1 to the range because the value is rendered with quotes around it
                    // add 1 to the end because it is exclusive
                    searchOccurrence
                        ?.ranges
                        ?.filterIsInstance<SearchOccurrence.Range.Key>()
                        ?.forEach { keyRange ->
                            val color = if(keyRange == searchOccurrenceSelectedRange) Color.Blue else colors.highlightColor
                            addStyle(
                                style = SpanStyle(background = color),
                                start = keyRange.range.first + 1,
                                end = keyRange.range.last + 1 + 1
                            )
                        }

                    withStyle(SpanStyle(color = colors.symbolColor)) {
                        append(": ")
                    }
                }
            }

            val keyOffset = this.length

            withStyle(SpanStyle(color = valueColor)) {
                append(value.toString())
            }

            searchOccurrence
                ?.ranges
                ?.filterIsInstance<SearchOccurrence.Range.Value>()
                ?.forEach { valueRange ->
                    val color = if(valueRange == searchOccurrenceSelectedRange) Color.Blue else colors.highlightColor
                    // add an offset for the key which is already appended to the string
                    // add 1 to the range if the value is a string because it has quotes around it
                    // add 1 to the end because it is exclusive
                    val stringQuoteOffset = if(value.isString) 1 else 0
                    addStyle(
                        style = SpanStyle(background = color),
                        start = keyOffset + valueRange.range.first + stringQuoteOffset,
                        end = keyOffset + valueRange.range.last + stringQuoteOffset + 1
                    )
                }

            if (!isLastItem) {
                withStyle(SpanStyle(color = colors.symbolColor)) {
                    append(",")
                }
            }
        }
    }
}
