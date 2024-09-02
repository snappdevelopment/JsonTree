package com.sebastianneubauer.jsontreesample

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sebastianneubauer.jsontree.JsonTree
import com.sebastianneubauer.jsontree.TreeColors
import com.sebastianneubauer.jsontree.TreeState
import com.sebastianneubauer.jsontree.defaultDarkColors
import com.sebastianneubauer.jsontree.defaultLightColors
import com.sebastianneubauer.jsontreesample.ui.theme.JsonTreeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun App() = JsonTreeTheme {
    MainScreen()
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)
@Composable
private fun MainScreen() {
    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                title = {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = "🌳 JsonTree",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black
                    )
                },
            )
        },
        contentWindowInsets = WindowInsets(top = 60.dp),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            var errorMessage: String? by remember { mutableStateOf(null) }
            var json: String by remember { mutableStateOf(simpleJson) }
            var colors: TreeColors by remember { mutableStateOf(defaultLightColors) }
            var initialState: TreeState by remember { mutableStateOf(TreeState.FIRST_ITEM_EXPANDED) }
            var showIndices: Boolean by remember { mutableStateOf(true) }
            var showItemCount: Boolean by remember { mutableStateOf(true) }
            var expandSingleChildren: Boolean by remember { mutableStateOf(true) }

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White),
            ) {
                Button(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = {
                        errorMessage = null
                        json = when (json) {
                            emptyJson -> simpleJson
                            simpleJson -> complexJson
                            complexJson -> invalidJson
                            invalidJson -> emptyJson
                            else -> throw IllegalStateException("No JSON selected!")
                        }
                    }
                ) {
                    Text(
                        text = when (json) {
                            simpleJson -> "Simple Json"
                            emptyJson -> "Empty Json"
                            complexJson -> "Complex Json"
                            invalidJson -> "Invalid Json"
                            else -> throw IllegalStateException("No JSON selected!")
                        }
                    )
                }

                Button(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = {
                        val newState = when (initialState) {
                            TreeState.EXPANDED -> TreeState.COLLAPSED
                            TreeState.COLLAPSED -> TreeState.FIRST_ITEM_EXPANDED
                            TreeState.FIRST_ITEM_EXPANDED -> TreeState.EXPANDED
                        }
                        initialState = newState
                    }
                ) {
                    Text(text = initialState.name)
                }

                Button(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = { showIndices = !showIndices }
                ) {
                    Text(text = if (showIndices) "Hide indices" else "Show indices")
                }

                Button(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = { showItemCount = !showItemCount }
                ) {
                    Text(text = if (showItemCount) "Hide item count" else "Show item count")
                }

                Button(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = {
                        colors = if (colors == defaultLightColors) defaultDarkColors else defaultLightColors
                    }
                ) {
                    Text(text = if (colors == defaultLightColors) "Light" else "Dark")
                }

                Button(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = { expandSingleChildren = !expandSingleChildren }
                ) {
                    Text(text = if (expandSingleChildren) "Expand children" else "Don't expand children")
                }
            }

            val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

            //Pager to test leaving composition
            HorizontalPager(
                modifier = Modifier.fillMaxWidth().weight(1F),
                state = pagerState,
                verticalAlignment = Alignment.Top
            ) { pageIndex ->
                when (pageIndex) {
                    0 -> {
                        val error = errorMessage
                        if (error != null) {
                            Text(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        color = if (colors == defaultLightColors) Color.White else Color.Black
                                    ),
                                text = error,
                                color = if (colors == defaultLightColors) Color.Black else Color.White,
                            )
                        } else {
                            JsonTree(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .horizontalScroll(rememberScrollState())
                                    .background(
                                        if (colors == defaultLightColors) Color.White else Color.Black
                                    ),
                                contentPadding = PaddingValues(16.dp),
                                json = json,
                                onLoading = {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Loading...",
                                            color = if (colors == defaultLightColors) Color.Black else Color.White
                                        )
                                    }
                                },
                                initialState = initialState,
                                colors = colors,
                                showIndices = showIndices,
                                showItemCount = showItemCount,
                                expandSingleChildren = expandSingleChildren,
                                onError = { errorMessage = it.message },
                            )
                        }
                    }

                    1 -> {
                        Text(text = "Page 1")
                    }

                    2 -> {
                        Text(text = "Page 2")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewMainScreen() = JsonTreeTheme {
    MainScreen()
}