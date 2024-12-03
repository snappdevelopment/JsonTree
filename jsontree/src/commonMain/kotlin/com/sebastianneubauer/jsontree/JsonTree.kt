package com.sebastianneubauer.jsontree

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import jsontree.jsontree.generated.resources.Res
import jsontree.jsontree.generated.resources.jsontree_arrow_right
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.vectorResource

/**
 * Renders JSON data as a formatted tree with collapsable objects and arrays.
 * Collapsed items display the amount of child items inside them.
 *
 * @param json The json data as a string.
 * @param onLoading A Composable which is shown during the initial loading of the tree.
 * @param modifier The Modifier for this Composable.
 * @param initialState The initial state of the tree before user interaction. One of [TreeState].
 * @param contentPadding The content padding for the scrollable container.
 * @param colors The color palette the tree uses. [defaultLightColors], [defaultDarkColors] or a
 * custom instance of [TreeColors].
 * @param icon The icon which is shown in front of collapsable items. Default value is an arrow icon.
 * @param iconSize The size of the [icon]. This size is also used to calculate indents.
 * @param textStyle The style which is used for all texts in the tree.
 * @param showIndices If true, arrays will show the index in front of each item.
 * @param showItemCount If true, arrays and objects will show their amount of child items when collapsed.
 * @param expandSingleChildren If true, children of collapsable items that have no siblings will be
 * automatically expanded with their parent.
 * @param onError A callback which is called when the json can't be parsed and thus won't
 * be rendered. Receives the throwable of the error.
 */
@Composable
public fun JsonTree(
    json: String,
    onLoading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    initialState: TreeState = TreeState.FIRST_ITEM_EXPANDED,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    colors: TreeColors = defaultLightColors,
    icon: ImageVector = vectorResource(Res.drawable.jsontree_arrow_right),
    iconSize: Dp = 20.dp,
    textStyle: TextStyle = LocalTextStyle.current,
    showIndices: Boolean = false,
    showItemCount: Boolean = true,
    expandSingleChildren: Boolean = false,
    jsonSearchResultState: JsonSearchResultState = rememberJsonSearchResultState(),
    onError: (Throwable) -> Unit = {}
) {
    val jsonParser = remember(json) {
        JsonTreeParser(
            json = json,
            defaultDispatcher = Dispatchers.Default,
            mainDispatcher = Dispatchers.Main
        )
    }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(jsonParser, initialState) {
        jsonParser.init(initialState)
    }

    val listState = rememberLazyListState()

    val searchKeyValue = jsonSearchResultState.state.searchKeyValue

    when (val state = jsonParser.state.value) {
        is JsonTreeParserState.Ready -> {
            Box(modifier = modifier) {
                JsonTreeList(
                    state = listState,
                    items = state.list,
                    contentPadding = contentPadding,
                    colors = colors,
                    icon = icon,
                    iconSize = iconSize,
                    textStyle = textStyle,
                    showIndices = showIndices,
                    showItemCount = showItemCount,
                    searchKeyValue = searchKeyValue,
                    onClick = {
                        coroutineScope.launch {
                            jsonParser.expandOrCollapseItem(
                                item = it,
                                expandSingleChildren = expandSingleChildren
                            )
                        }
                    }
                )

                if (!searchKeyValue.isNullOrEmpty()) {
                    handleJsonSearchResult(
                        jsonSearchResultState,
                        jsonParser,
                        state,
                        searchKeyValue,
                        listState
                    )
                }
            }
        }
        is JsonTreeParserState.Loading -> onLoading()
        is JsonTreeParserState.Parsing.Error -> onError(state.throwable)
        is JsonTreeParserState.Parsing.Parsed -> error("Unexpected state $state")
    }
}

@Composable
private fun handleJsonSearchResult(
    jsonSearchResultState: JsonSearchResultState,
    jsonTreeParser: JsonTreeParser,
    state: JsonTreeParserState.Ready,
    searchKeyValue: String?,
    listState: LazyListState,
) {
    LaunchedEffect(searchKeyValue) {
        jsonSearchResultState.state = JsonSearchResult(searchKeyValue)
        withContext(Dispatchers.Default) {
            state.list.forEach {
                if (it is JsonTreeElement.Collapsable &&
                    it.state == TreeState.COLLAPSED &&
                    it.childrenHasMatch(searchKeyValue)
                ) {
                    jsonTreeParser.expandOrCollapseItem(it, true)
                }
            }

            val highlightedLines = state.list.mapIndexed { index, jsonTreeElement ->
                if (jsonTreeElement.hasMatch(searchKeyValue)) index else null
            }.filterNotNull()
            jsonSearchResultState.state = JsonSearchResult(
                searchKeyValue,
                state.list.size,
                highlightedLines
            )
        }
    }

    LaunchedEffect(state.list.size) {
        if (jsonSearchResultState.state.totalListSize == state.list.size) return@LaunchedEffect

        jsonSearchResultState.state = JsonSearchResult()
        withContext(Dispatchers.Default) {
            val highlightedLines = state.list.mapIndexed { index, jsonTreeElement ->
                if (jsonTreeElement.hasMatch(searchKeyValue)) index else null
            }.filterNotNull()
            jsonSearchResultState.state =
                JsonSearchResult(searchKeyValue, state.list.size, highlightedLines)
        }
    }

    val currentHighlightedLine = jsonSearchResultState.state.currentHighlightedLine
    val highlightedLines = jsonSearchResultState.state.highlightedLines

    LaunchedEffect(currentHighlightedLine) {
        if (currentHighlightedLine > -1 && highlightedLines.isNotEmpty() && !listState.isScrollInProgress) {
            listState.scrollToItem(highlightedLines[currentHighlightedLine])
        }
    }
}

@Composable
private fun JsonTreeList(
    items: List<JsonTreeElement>,
    contentPadding: PaddingValues,
    colors: TreeColors,
    icon: ImageVector,
    iconSize: Dp,
    textStyle: TextStyle,
    showIndices: Boolean,
    showItemCount: Boolean,
    searchKeyValue: String?,
    state: LazyListState = rememberLazyListState(),
    onClick: (JsonTreeElement) -> Unit,
) {
    LazyColumn(
        state = state,
        contentPadding = contentPadding
    ) {
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            when (item) {
                is JsonTreeElement.Collapsable.Array -> {
                    val coloredText = rememberCollapsableText(
                        item = item,
                        type = CollapsableType.ARRAY,
                        key = item.key,
                        childItemCount = item.children.size,
                        state = item.state,
                        colors = colors,
                        isLastItem = item.isLastItem,
                        showIndices = showIndices,
                        showItemCount = showItemCount,
                        searchKeyValue = searchKeyValue,
                        parentType = item.parentType,
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
                        item = item,
                        type = CollapsableType.OBJECT,
                        key = item.key,
                        childItemCount = item.children.size,
                        state = item.state,
                        colors = colors,
                        isLastItem = item.isLastItem,
                        searchKeyValue = searchKeyValue,
                        showIndices = showIndices,
                        showItemCount = showItemCount,
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
                        searchKeyValue = searchKeyValue,
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
                            iconSize
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
