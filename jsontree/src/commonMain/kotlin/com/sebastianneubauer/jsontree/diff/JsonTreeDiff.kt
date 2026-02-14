package com.sebastianneubauer.jsontree.diff

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.sebastianneubauer.jsontree.TreeColors
import com.sebastianneubauer.jsontree.defaultLightColors
import com.sebastianneubauer.jsontree.diff.JsonTreeDifferState.JsonDiffElement
import com.sebastianneubauer.jsontree.util.toRenderString
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
                items(jsonTreeDifferState.originalJsonDiffElements) { diffElement ->
                    val text = when(diffElement) {
                        is JsonDiffElement.Change -> diffElement.jsonTreeElement.toRenderString()
                        is JsonDiffElement.Deletion -> diffElement.jsonTreeElement!!.toRenderString()
                        is JsonDiffElement.Equal -> diffElement.jsonTreeElement.toRenderString()
                        is JsonDiffElement.Insertion -> ""
                    }
                    val annotatedText = buildAnnotatedString {
                        append(text)
                        if(diffElement is JsonDiffElement.Change) {
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
                                    is JsonDiffElement.Change -> Color.Red
                                    is JsonDiffElement.Deletion -> Color.Red
                                    is JsonDiffElement.Equal -> Color.Transparent
                                    is JsonDiffElement.Insertion -> Color.Transparent
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
                items(jsonTreeDifferState.revisedJsonDiffElements) { diffElement ->
                    val text = when(diffElement) {
                        is JsonDiffElement.Change -> diffElement.jsonTreeElement.toRenderString()
                        is JsonDiffElement.Deletion -> ""
                        is JsonDiffElement.Equal -> diffElement.jsonTreeElement.toRenderString()
                        is JsonDiffElement.Insertion -> diffElement.jsonTreeElement!!.toRenderString()
                    }
                    val annotatedText = buildAnnotatedString {
                        append(text)
                        if(diffElement is JsonDiffElement.Change) {
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
                                    is JsonDiffElement.Change -> Color.Green
                                    is JsonDiffElement.Deletion -> Color.Transparent
                                    is JsonDiffElement.Equal -> Color.Transparent
                                    is JsonDiffElement.Insertion -> Color.Green
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
