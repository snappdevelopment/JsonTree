package com.sebastianneubauer.jsontree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    item: JsonTreeElement,
    type: CollapsableType,
    key: String?,
    childItemCount: Int,
    state: TreeState,
    colors: TreeColors,
    isLastItem: Boolean,
    searchKeyValue: String?,
    jsonQuery: String?,
    showIndices: Boolean,
    showItemCount: Boolean,
    parentType: ParentType,
): AnnotatedString {
    val itemCount = pluralStringResource(Res.plurals.jsontree_collapsable_items, childItemCount, childItemCount)

    return remember(
        key, state, colors, isLastItem, itemCount, type,
        showIndices, showItemCount, searchKeyValue, jsonQuery
    ) {
        val openBracket = if (type == CollapsableType.OBJECT) "{" else "["
        val closingBracket = if (type == CollapsableType.OBJECT) "}" else "]"
        val childrenHasMatch = item.childrenHasMatch(searchKeyValue)

        buildAnnotatedString {
            key?.let {
                val (jsonQueryKey, jsonQueryIndex) = splitArrayNotation(jsonQuery)

                val (before, highlightedText, after) = searchText(
                    it,
                    jsonQueryKey ?: searchKeyValue,
                    !jsonQueryKey.isNullOrEmpty()
                )

                if (parentType == ParentType.ARRAY && showIndices) {
                    withStyle(
                        if (it == jsonQueryIndex) {
                            SpanStyle(
                                color = colors.highlightTextColor,
                                background = colors.highlightColor
                            )
                        } else {
                            SpanStyle(color = colors.indexColor)
                        }
                    ) {
                        append(it)
                    }
                    withStyle(SpanStyle(color = colors.symbolColor)) {
                        append(": ")
                    }
                } else if (parentType != ParentType.ARRAY) {
                    withStyle(SpanStyle(color = colors.keyColor)) {
                        if (highlightedText != null) {
                            append("\"$before")
                            withStyle(
                                SpanStyle(
                                    color = colors.highlightTextColor,
                                    background = colors.highlightColor
                                )
                            ) {
                                append(highlightedText)
                            }
                            append("$after\"")
                        } else {
                            append("\"$it\"")
                        }
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
                if (showItemCount) {
                    withStyle(
                        if (childrenHasMatch) {
                            SpanStyle(
                                color = colors.highlightTextColor,
                                background = colors.highlightColor
                            )
                        } else {
                            SpanStyle(color = colors.symbolColor)
                        }
                    ) {
                        append(itemCount)
                    }
                } else {
                    withStyle(
                        if (childrenHasMatch) {
                            SpanStyle(
                                color = colors.highlightTextColor,
                                background = colors.highlightColor
                            )
                        } else {
                            SpanStyle(color = colors.symbolColor)
                        }
                    ) {
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
    searchKeyValue: String?,
    jsonQuery: String?,
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

    return remember(key, value, colors, isLastItem, showIndices, searchKeyValue, jsonQuery) {
        buildAnnotatedString {
            key?.let {
                val (jsonQueryKey, jsonQueryIndex) = splitArrayNotation(jsonQuery)

                val (before, highlightedText, after) = searchText(
                    it,
                    jsonQueryKey ?: searchKeyValue,
                    jsonQueryKey != null
                )

                if (parentType == ParentType.ARRAY && showIndices) {
                    withStyle(
                        if (it == jsonQueryIndex) {
                            SpanStyle(
                                color = colors.highlightTextColor,
                                background = colors.highlightColor
                            )
                        } else {
                            SpanStyle(color = colors.indexColor)
                        }
                    ) {
                        append(it)
                    }
                    withStyle(SpanStyle(color = colors.symbolColor)) {
                        append(": ")
                    }
                } else if (parentType != ParentType.ARRAY) {
                    withStyle(SpanStyle(color = colors.keyColor)) {
                        if (highlightedText != null) {
                            append("\"$before")
                            withStyle(
                                SpanStyle(
                                    color = colors.highlightTextColor,
                                    background = colors.highlightColor
                                )
                            ) {
                                append(highlightedText)
                            }
                            append("$after\"")
                        } else {
                            append("\"$it\"")
                        }
                    }
                    withStyle(SpanStyle(color = colors.symbolColor)) {
                        append(": ")
                    }
                }
            }

            withStyle(SpanStyle(color = valueColor)) {
                val (before, highlightedText, after) = searchText(value.toString(), searchKeyValue)
                if (highlightedText != null) {
                    append(before)
                    withStyle(
                        SpanStyle(
                            color = colors.highlightTextColor,
                            background = colors.highlightColor
                        )
                    ) {
                        append(highlightedText)
                    }
                    append(after)
                } else {
                    append(before)
                }
            }

            if (!isLastItem) {
                withStyle(SpanStyle(color = colors.symbolColor)) {
                    append(",")
                }
            }
        }
    }
}
