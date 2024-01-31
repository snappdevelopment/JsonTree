package com.sebastianneubauer.jsontree

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

@Composable
public fun JsonTree2Ui(
    modifier: Modifier = Modifier,
    json: String,
    initialState: TreeState = TreeState.FIRST_ITEM_EXPANDED,
    colors: TreeColors = defaultLightColors,
    icon: ImageVector = ImageVector.vectorResource(R.drawable.jsontree_arrow_right),
    iconSize: Dp = 20.dp,
    textStyle: TextStyle = LocalTextStyle.current,
    showIndices: Boolean = false,
    onError: (Throwable) -> Unit = {}
) {
    val jsonParser = remember(json) { JsonViewModel(json) }

    LaunchedEffect(jsonParser, initialState) {
        jsonParser.initJsonParser(initialState)
    }

    when (val state = jsonParser.state.value) {
        is ParserState.Ready -> {
            JsonTreeList(
                modifier = modifier,
                items = state.list,
                colors = colors,
                icon = icon,
                iconSize = iconSize,
                textStyle = textStyle,
                showIndices = showIndices,
                onClick = { jsonParser.expandOrCollapseItem(it) }
            )
        }
        is ParserState.Loading -> Text(text = "Loading...")
        is ParserState.Parsing.Error -> onError(state.throwable)
        is ParserState.Parsing.Parsed -> error("Unexpected state $state")
    }
}

@Composable
private fun JsonTreeList(
    modifier: Modifier,
    items: List<JsonTree>,
    colors: TreeColors,
    icon: ImageVector,
    iconSize: Dp,
    textStyle: TextStyle,
    showIndices: Boolean,
    onClick: (JsonTree) -> Unit,
) {
    Box(modifier = modifier) {
        LazyColumn {
            itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                when (item) {
                    is JsonTree.CollapsableElement.ArrayElement -> {
                        CollapsableElement(
                            type = CollapsableType.ARRAY,
                            key = item.key,
                            childElements = item.children,
                            state = item.state,
                            indent = if (index == 0 || index == items.lastIndex) {
                                0.dp
                            } else {
                                item.level * iconSize
                            },
                            colors = colors,
                            textStyle = textStyle,
                            icon = icon,
                            iconSize = iconSize,
                            isLastItem = item.isLastItem,
                            showIndices = showIndices,
                            parentType = item.parentType,
                            onClick = { onClick(item) }
                        )
                    }
                    is JsonTree.CollapsableElement.ObjectElement -> {
                        CollapsableElement(
                            type = CollapsableType.OBJECT,
                            key = item.key,
                            childElements = item.children,
                            state = item.state,
                            indent = if (index == 0 || index == items.lastIndex) {
                                0.dp
                            } else {
                                item.level * iconSize
                            },
                            colors = colors,
                            textStyle = textStyle,
                            icon = icon,
                            iconSize = iconSize,
                            isLastItem = item.isLastItem,
                            showIndices = showIndices,
                            parentType = item.parentType,
                            onClick = { onClick(item) }
                        )
                    }
                    is JsonTree.PrimitiveElement -> {
                        PrimitiveElement(
                            key = item.key,
                            value = item.value,
                            colors = colors,
                            textStyle = textStyle,
                            indent = if (index == 0 || index == items.lastIndex) {
                                0.dp
                            } else {
                                (item.level * iconSize) + iconSize
                            },
                            isLastItem = item.isLastItem,
                            showIndices = showIndices,
                            parentType = item.parentType,
                        )
                    }
                    is JsonTree.NullElement -> {
                        PrimitiveElement(
                            key = item.key,
                            value = item.value,
                            colors = colors,
                            textStyle = textStyle,
                            indent = if (index == 0 || index == items.lastIndex) {
                                0.dp
                            } else {
                                (item.level * iconSize) + iconSize
                            },
                            isLastItem = item.isLastItem,
                            showIndices = showIndices,
                            parentType = item.parentType,
                        )
                    }
                    is JsonTree.EndBracket -> {
                        Bracket(
                            type = item.type,
                            colors = colors,
                            textStyle = textStyle,
                            indent = if (index == 0 || index == items.lastIndex) {
                                0.dp
                            } else {
                                (item.level * iconSize) + iconSize
                            },
                            isLastItem = item.isLastItem
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollapsableElement(
    type: CollapsableType,
    key: String?,
    childElements: Map<String, JsonTree>,
    state: TreeState,
    indent: Dp,
    colors: TreeColors,
    textStyle: TextStyle,
    icon: ImageVector,
    iconSize: Dp,
    isLastItem: Boolean,
    showIndices: Boolean,
    parentType: ParentType,
    onClick: () -> Unit,
) {
    val openBracket = if (type == CollapsableType.OBJECT) "{" else "["
    val closingBracket = if (type == CollapsableType.OBJECT) "}" else "]"
    val itemCount = pluralStringResource(R.plurals.jsontree_collapsable_items, childElements.size, childElements.size)

    val coloredText = remember(key, state, colors, isLastItem, itemCount, type) {
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
                    indication = null,
                    onClick = onClick
                ),
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
    showIndices: Boolean,
    parentType: ParentType,
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

    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Spacer(modifier = Modifier.width(indent))
        Text(text = coloredText, style = textStyle)
    }
}

@Composable
private fun Bracket(
    type: JsonTree.EndBracket.Type,
    isLastItem: Boolean,
    indent: Dp,
    colors: TreeColors,
    textStyle: TextStyle,
) {
    val closingBracket = if (type == JsonTree.EndBracket.Type.OBJECT) "}" else "]"

    Text(
        modifier = Modifier.padding(start = indent),
        text = if (!isLastItem) "$closingBracket," else closingBracket,
        color = colors.symbolColor,
        style = textStyle
    )
}
