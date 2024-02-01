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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times

@Composable
public fun JsonTree2Ui(
    json: String,
    onLoading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
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

    Box(modifier = modifier) {
        when (val state = jsonParser.state.value) {
            is ParserState.Ready -> {
                JsonTreeList(
                    items = state.list,
                    colors = colors,
                    icon = icon,
                    iconSize = iconSize,
                    textStyle = textStyle,
                    showIndices = showIndices,
                    onClick = { jsonParser.expandOrCollapseItem(it) }
                )
            }
            is ParserState.Loading -> onLoading()
            is ParserState.Parsing.Error -> onError(state.throwable)
            is ParserState.Parsing.Parsed -> error("Unexpected state $state")
        }
    }
}

@Composable
private fun JsonTreeList(
    items: List<JsonTree>,
    colors: TreeColors,
    icon: ImageVector,
    iconSize: Dp,
    textStyle: TextStyle,
    showIndices: Boolean,
    onClick: (JsonTree) -> Unit,
) {
    LazyColumn {
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            when (item) {
                is JsonTree.CollapsableElement.ArrayElement -> {
                    val coloredText = rememberCollapsableText(
                        type = CollapsableType.ARRAY,
                        key = item.key,
                        childItemCount = item.children.size,
                        state = item.state,
                        colors = colors,
                        isLastItem = item.isLastItem,
                        showIndices = showIndices,
                        parentType = item.parentType
                    )

                    CollapsableElement(
                        state = item.state,
                        text = coloredText,
                        indent = if (index == 0 || index == items.lastIndex) {
                            0.dp
                        } else {
                            item.level * iconSize
                        },
                        colors = colors,
                        textStyle = textStyle,
                        icon = icon,
                        iconSize = iconSize,
                        onClick = { onClick(item) }
                    )
                }
                is JsonTree.CollapsableElement.ObjectElement -> {
                    val coloredText = rememberCollapsableText(
                        type = CollapsableType.OBJECT,
                        key = item.key,
                        childItemCount = item.children.size,
                        state = item.state,
                        colors = colors,
                        isLastItem = item.isLastItem,
                        showIndices = showIndices,
                        parentType = item.parentType
                    )

                    CollapsableElement(
                        state = item.state,
                        text = coloredText,
                        indent = if (index == 0 || index == items.lastIndex) {
                            0.dp
                        } else {
                            item.level * iconSize
                        },
                        colors = colors,
                        textStyle = textStyle,
                        icon = icon,
                        iconSize = iconSize,
                        onClick = { onClick(item) }
                    )
                }
                is JsonTree.PrimitiveElement -> {
                    val coloredText = rememberPrimitiveText(
                        key = item.key,
                        value = item.value,
                        colors = colors,
                        isLastItem = item.isLastItem,
                        showIndices = showIndices,
                        parentType = item.parentType
                    )

                    PrimitiveElement(
                        text = coloredText,
                        textStyle = textStyle,
                        indent = if (index == 0 || index == items.lastIndex) {
                            0.dp
                        } else {
                            (item.level * iconSize) + iconSize
                        },
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

@Composable
private fun CollapsableElement(
    state: TreeState,
    text: AnnotatedString,
    indent: Dp,
    colors: TreeColors,
    textStyle: TextStyle,
    icon: ImageVector,
    iconSize: Dp,
    onClick: () -> Unit,
) {
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

            Text(text = text, style = textStyle)
        }
    }
}

@Composable
private fun PrimitiveElement(
    text: AnnotatedString,
    textStyle: TextStyle,
    indent: Dp,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Spacer(modifier = Modifier.width(indent))
        Text(text = text, style = textStyle)
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
