package com.sebastianneubauer.jsontree

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

/**
 * Renders JSON data as a formatted tree with collapsable objects and arrays.
 * Collapsed items display the amount of child items inside them.
 *
 * @param json The json data as a string.
 * @param onLoading A Composable which is shown during the initial loading of the tree.
 * @param modifier The Modifier for this Composable.
 * @param initialState The initial state of the tree before user interaction. One of [TreeState].
 * @param colors The color palette the tree uses. [defaultLightColors], [defaultDarkColors] or a
 * custom instance of [TreeColors].
 * @param icon The icon which is shown in front of collapsable items. Default value is an arrow icon.
 * @param iconSize The size of the [icon]. This size is also used to calculate indents.
 * @param textStyle The style which is used for all texts in the tree.
 * @param showIndices If true, arrays will show the index in front of each item.
 * @param onError A callback which is called when the json can't be parsed and thus won't
 * be rendered. Receives the throwable of the error.
 */
@Composable
public fun JsonTree(
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
    val jsonParser = remember(json) { JsonTreeParser(json) }

    LaunchedEffect(jsonParser, initialState) {
        jsonParser.init(initialState)
    }

    Box(modifier = modifier) {
        when (val state = jsonParser.state.value) {
            is JsonTreeParserState.Ready -> {
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
            is JsonTreeParserState.Loading -> onLoading()
            is JsonTreeParserState.Parsing.Error -> onError(state.throwable)
            is JsonTreeParserState.Parsing.Parsed -> error("Unexpected state $state")
        }
    }
}

@Composable
private fun JsonTreeList(
    items: List<JsonTreeElement>,
    colors: TreeColors,
    icon: ImageVector,
    iconSize: Dp,
    textStyle: TextStyle,
    showIndices: Boolean,
    onClick: (JsonTreeElement) -> Unit,
) {
    LazyColumn {
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            when (item) {
                is JsonTreeElement.Collapsable.Array -> {
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

                    Collapsable(
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
                is JsonTreeElement.Collapsable.Object -> {
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

                    Collapsable(
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
                is JsonTreeElement.Primitive -> {
                    val coloredText = rememberPrimitiveText(
                        key = item.key,
                        value = item.value,
                        colors = colors,
                        isLastItem = item.isLastItem,
                        showIndices = showIndices,
                        parentType = item.parentType
                    )

                    Primitive(
                        text = coloredText,
                        textStyle = textStyle,
                        indent = if (index == 0 || index == items.lastIndex) {
                            0.dp
                        } else {
                            (item.level * iconSize) + iconSize
                        },
                    )
                }
                is JsonTreeElement.EndBracket -> {
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
private fun Collapsable(
    state: TreeState,
    text: AnnotatedString,
    indent: Dp,
    colors: TreeColors,
    textStyle: TextStyle,
    icon: ImageVector,
    iconSize: Dp,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
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

@Composable
private fun Primitive(
    text: AnnotatedString,
    textStyle: TextStyle,
    indent: Dp,
) {
    Text(
        modifier = Modifier.padding(start = indent),
        text = text,
        style = textStyle
    )
}

@Composable
private fun Bracket(
    type: JsonTreeElement.EndBracket.Type,
    isLastItem: Boolean,
    indent: Dp,
    colors: TreeColors,
    textStyle: TextStyle,
) {
    val closingBracket = if (type == JsonTreeElement.EndBracket.Type.OBJECT) "}" else "]"

    Text(
        modifier = Modifier.padding(start = indent),
        text = if (!isLastItem) "$closingBracket," else closingBracket,
        color = colors.symbolColor,
        style = textStyle
    )
}
