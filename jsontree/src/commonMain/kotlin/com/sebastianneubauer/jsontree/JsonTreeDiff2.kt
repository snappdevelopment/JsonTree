package com.sebastianneubauer.jsontree

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jsontree.jsontree.generated.resources.Res
import jsontree.jsontree.generated.resources.jsontree_arrow_right
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource

@Composable
public fun JsonTreeDiff2(
    originalJson: String,
    revisedJson: String,
    onLoading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    colors: TreeColors = defaultLightColors,
    icon: ImageVector = vectorResource(Res.drawable.jsontree_arrow_right),
    iconSize: Dp = 20.dp,
    textStyle: TextStyle = LocalTextStyle.current,
    showIndices: Boolean = false,
    showItemCount: Boolean = true,
    lazyListState: LazyListState = rememberLazyListState(),
    onError: (Throwable) -> Unit = {}
) {


}

@Composable
public fun SideBySideDiff2() {
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

    val jsonTreeDiffer2 = remember {
        JsonTreeDiffer2(
            defaultDispatcher = Dispatchers.Default,
            mainDispatcher = Dispatchers.Main
        )
    }
    val jsonTreeDiffer2State = jsonTreeDiffer2.state.collectAsState().value
    LaunchedEffect(original, revised) {
        jsonTreeDiffer2.diff(original, revised)
    }

    val originalListState = rememberLazyListState()
    val revisedListState = rememberLazyListState()

    SyncScrollingEffect(
        originalListState = originalListState,
        revisedListState = revisedListState,
    )

    if(jsonTreeDiffer2State is JsonTreeDiffer2State.Ready) {
        Row(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = originalListState
            ) {
                items(jsonTreeDiffer2State.originalJsonDiffElements) { diffElement ->
                    val text = when(diffElement) {
                        is JsonTreeDiffer2.JsonDiffElement.Change -> diffElement.jsonTreeElement.toDiffString()
                        is JsonTreeDiffer2.JsonDiffElement.Deletion -> diffElement.jsonTreeElement!!.toDiffString()
                        is JsonTreeDiffer2.JsonDiffElement.Equal -> diffElement.jsonTreeElement.toDiffString()
                        is JsonTreeDiffer2.JsonDiffElement.Insertion -> ""
                    }
                    val annotatedText = buildAnnotatedString {
                        append(text)
                        if(diffElement is JsonTreeDiffer2.JsonDiffElement.Change) {
                            diffElement.inlineDiffIndices.forEach { (start, end) ->
                                addStyle(
                                    style = SpanStyle(background = Color.Blue),
                                    start = start,
                                    end = end
                                )
                            }
                        }
                    }

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = when(diffElement) {
                                    is JsonTreeDiffer2.JsonDiffElement.Change -> Color.Red
                                    is JsonTreeDiffer2.JsonDiffElement.Deletion -> Color.Red
                                    is JsonTreeDiffer2.JsonDiffElement.Equal -> Color.Transparent
                                    is JsonTreeDiffer2.JsonDiffElement.Insertion -> Color.Transparent
                                }
                            ),
                        text = annotatedText
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                state = revisedListState
            ) {
                items(jsonTreeDiffer2State.revisedJsonDiffElements) { diffElement ->
                    val text = when(diffElement) {
                        is JsonTreeDiffer2.JsonDiffElement.Change -> diffElement.jsonTreeElement.toDiffString()
                        is JsonTreeDiffer2.JsonDiffElement.Deletion -> ""
                        is JsonTreeDiffer2.JsonDiffElement.Equal -> diffElement.jsonTreeElement.toDiffString()
                        is JsonTreeDiffer2.JsonDiffElement.Insertion -> diffElement.jsonTreeElement!!.toDiffString()
                    }
                    val annotatedText = buildAnnotatedString {
                        append(text)
                        if(diffElement is JsonTreeDiffer2.JsonDiffElement.Change) {
                            diffElement.inlineDiffIndices.forEach { (start, end) ->
                                addStyle(
                                    style = SpanStyle(background = Color.Blue),
                                    start = start,
                                    end = end
                                )
                            }
                        }
                    }

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = when(diffElement) {
                                    is JsonTreeDiffer2.JsonDiffElement.Change -> Color.Green
                                    is JsonTreeDiffer2.JsonDiffElement.Deletion -> Color.Transparent
                                    is JsonTreeDiffer2.JsonDiffElement.Equal -> Color.Transparent
                                    is JsonTreeDiffer2.JsonDiffElement.Insertion -> Color.Green
                                }
                            ),
                        text = annotatedText
                    )
                }
            }
        }
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
