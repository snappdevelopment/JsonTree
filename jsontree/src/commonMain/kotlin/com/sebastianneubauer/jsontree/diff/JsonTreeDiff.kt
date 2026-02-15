package com.sebastianneubauer.jsontree.diff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sebastianneubauer.jsontree.CollapsableType
import com.sebastianneubauer.jsontree.JsonTreeElement
import com.sebastianneubauer.jsontree.TreeColors
import com.sebastianneubauer.jsontree.defaultLightColors
import com.sebastianneubauer.jsontree.diff.JsonTreeDifferState.JsonDiffElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
public fun JsonTreeDiff(
    originalJson: String,
    revisedJson: String,
    onLoading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    colors: TreeColors = defaultLightColors,
    textStyle: TextStyle = LocalTextStyle.current,
    onError: (Throwable) -> Unit = {}
) {


}

@Composable
public fun SideBySideDiff2(
    colors: JsonTreeDiffColors = defaultDarkDiffColors
) {
    val original = """
        {
            "topLevelObject": {
                "string": "stringValue",
                "nestedObject": {
                    "int": 42,
                    "nestedArray": [
                        "nestedArrayValue",
                        "nestedArrayValue"
                    ],
                    "arrayOfObjects": [
                        {
                            "anotherString": "anotherStringValue"
                        },
                        {
                            "anotherInt": 52
                        }
                    ]
                }
            },
            "topLevelArray": [
                "hello",
                "world"
            ],
            "emptyObject": {
    
            }
        }
    """.trimIndent()

    val revised = """
        {
            "topLevelObject": {
                "string": "stringValue",
                "nestedObject": {
                    "nestedArray": [
                        "nestedArrayValue",
                        "nestedArrayValue"
                    ],
                    "rrayOfa": [
                        {
                            "anotherString": "anotherStringValue"
                        },
                        {
                            "anotherInt": 52
                        },
                        {
                            "anotherFloat": 5.0
                        }
                    ]
                }
            },
            "topLevelArray": [
                "hello",
                "world"
            ],
            "emptyObject": {
    
            }
        }
    """.trimIndent()

    val jsonTreeDiffer = remember {
        JsonTreeDiffer(
            defaultDispatcher = Dispatchers.Default,
            mainDispatcher = Dispatchers.Main
        )
    }
    val jsonTreeDifferState = jsonTreeDiffer.state.collectAsState().value
    LaunchedEffect(original, revised) {
        jsonTreeDiffer.diff(original, revised)
    }

    val originalListState = rememberLazyListState()
    val revisedListState = rememberLazyListState()

    SyncScrollingEffect(
        originalListState = originalListState,
        revisedListState = revisedListState,
    )

    if(jsonTreeDifferState is JsonTreeDifferState.Ready) {
        Row(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = originalListState
            ) {
                itemsIndexed(jsonTreeDifferState.originalJsonDiffElements) { index, diffElement ->
                    val jsonTreeElement = when(diffElement) {
                        is JsonDiffElement.Change -> diffElement.jsonTreeElement
                        is JsonDiffElement.Deletion -> diffElement.jsonTreeElement!!
                        is JsonDiffElement.Equal -> diffElement.jsonTreeElement
                        is JsonDiffElement.Insertion -> null
                    }

                    val text = rememberText(
                        jsonTreeElement = jsonTreeElement,
                        diffIndices = if(diffElement is JsonDiffElement.Change) {
                            diffElement.inlineDiffIndices
                        } else null,
                        colors = colors,
                        highlightColor = colors.deletionHighlightColor,
                    )

                    DiffText(
                        backgroundColor = when(diffElement) {
                            is JsonDiffElement.Change -> colors.deletionBackgroundColor
                            is JsonDiffElement.Deletion -> colors.deletionBackgroundColor
                            is JsonDiffElement.Equal -> Color.Transparent
                            is JsonDiffElement.Insertion -> Color.Transparent
                        },
                        indent = if(jsonTreeElement != null && index > 0) {
                            20.dp * jsonTreeElement.level
                        } else {
                            0.dp
                        },
                        text = text,
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                state = revisedListState
            ) {
                itemsIndexed(jsonTreeDifferState.revisedJsonDiffElements) { index, diffElement ->
                    val jsonTreeElement = when(diffElement) {
                        is JsonDiffElement.Change -> diffElement.jsonTreeElement
                        is JsonDiffElement.Deletion -> null
                        is JsonDiffElement.Equal -> diffElement.jsonTreeElement
                        is JsonDiffElement.Insertion -> diffElement.jsonTreeElement!!
                    }
                    val text = rememberText(
                        jsonTreeElement = jsonTreeElement,
                        diffIndices = if(diffElement is JsonDiffElement.Change) {
                            diffElement.inlineDiffIndices
                        } else null,
                        colors = colors,
                        highlightColor = colors.insertionHighlightColor
                    )

                    DiffText(
                        backgroundColor = when(diffElement) {
                            is JsonDiffElement.Change -> colors.insertionBackgroundColor
                            is JsonDiffElement.Deletion -> Color.Transparent
                            is JsonDiffElement.Equal -> Color.Transparent
                            is JsonDiffElement.Insertion -> colors.insertionBackgroundColor
                        },
                        indent = if(jsonTreeElement != null && index > 0) {
                            20.dp * jsonTreeElement.level
                        } else {
                            0.dp
                        },
                        text = text
                    )
                }
            }
        }
    }
}

@Composable
private fun DiffText(
    backgroundColor: Color,
    indent: Dp,
    text: AnnotatedString,
) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = backgroundColor)
            .padding(start = indent),
        text = text
    )
}

@Composable
private fun rememberText(
    jsonTreeElement: JsonTreeElement?,
    diffIndices: List<Pair<Int, Int>>?,
    colors: JsonTreeDiffColors,
    highlightColor: Color,
): AnnotatedString {
    return when(jsonTreeElement) {
        is JsonTreeElement.Collapsable.Array -> rememberCollapsableDiffText(
            type = CollapsableType.ARRAY,
            key = jsonTreeElement.key,
            isLastItem = jsonTreeElement.isLastItem,
            parentType = jsonTreeElement.parentType,
            colors = colors,
            highlightColor = highlightColor,
            diffIndices = diffIndices,
        )
        is JsonTreeElement.Collapsable.Object -> rememberCollapsableDiffText(
            type = CollapsableType.OBJECT,
            key = jsonTreeElement.key,
            isLastItem = jsonTreeElement.isLastItem,
            parentType = jsonTreeElement.parentType,
            colors = colors,
            highlightColor = highlightColor,
            diffIndices = diffIndices,
        )
        is JsonTreeElement.Primitive -> rememberPrimitiveDiffText(
            key = jsonTreeElement.key,
            value = jsonTreeElement.value,
            isLastItem = jsonTreeElement.isLastItem,
            parentType = jsonTreeElement.parentType,
            colors = colors,
            highlightColor = highlightColor,
            diffIndices = diffIndices,
        )
        is JsonTreeElement.EndBracket -> rememberEndBracketDiffText(
            type = jsonTreeElement.type,
            isLastItem = jsonTreeElement.isLastItem,
            colors = colors
        )
        null -> AnnotatedString("")
    }
}

@Composable
private fun SyncScrollingEffect(
    originalListState: LazyListState,
    revisedListState: LazyListState,
) {
    val coroutineScope = rememberCoroutineScope()
    fun syncScroll(leading: LazyListState, following: LazyListState) {
        coroutineScope.launch {
            val scrollPosition = leading.firstVisibleItemScrollOffset
            following.scrollToItem(
                index = leading.firstVisibleItemIndex,
                scrollOffset = scrollPosition
            )
        }
    }

    // Observe scroll changes in the LEFT column
    LaunchedEffect(originalListState.firstVisibleItemIndex, originalListState.firstVisibleItemScrollOffset) {
        syncScroll(originalListState, revisedListState)
    }

    // Observe scroll changes in the RIGHT column
    LaunchedEffect(revisedListState.firstVisibleItemIndex, revisedListState.firstVisibleItemScrollOffset) {
        syncScroll(revisedListState, originalListState)
    }
}
