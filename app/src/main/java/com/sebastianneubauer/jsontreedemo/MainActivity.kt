package com.sebastianneubauer.jsontreedemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sebastianneubauer.jsontree.TreeColors
import com.sebastianneubauer.jsontree.JsonTree
import com.sebastianneubauer.jsontree.TreeState
import com.sebastianneubauer.jsontree.defaultDarkColors
import com.sebastianneubauer.jsontree.defaultLightColors
import com.sebastianneubauer.jsontreedemo.ui.theme.JsonTreeTheme
import java.lang.IllegalStateException

internal class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            JsonTreeTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ) {
                    var errorMessage: String? by remember { mutableStateOf(null) }
                    var json: String by remember { mutableStateOf(simpleJson) }
                    var colors: TreeColors by remember { mutableStateOf(defaultLightColors) }
                    var initialState: TreeState by remember { mutableStateOf(TreeState.FIRST_ITEM_EXPANDED) }
                    var showIndices: Boolean by remember { mutableStateOf(true) }
                    var showItemCount: Boolean by remember { mutableStateOf(true) }

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "🌳 JsonTree",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Row {
                        Button(
                            onClick = {
                                json = when (json) {
                                    emptyJson -> simpleJson
                                    simpleJson -> complexJson
                                    complexJson -> emptyJson
                                    else -> throw IllegalStateException("No JSON selected!")
                                }
                            }
                        ) {
                            Text(
                                text = when (json) {
                                    simpleJson -> "Simple Json"
                                    emptyJson -> "Empty Json"
                                    complexJson -> "Complex Json"
                                    else -> throw IllegalStateException("No JSON selected!")
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                val newState = when(initialState) {
                                    TreeState.EXPANDED -> TreeState.COLLAPSED
                                    TreeState.COLLAPSED -> TreeState.FIRST_ITEM_EXPANDED
                                    TreeState.FIRST_ITEM_EXPANDED -> TreeState.EXPANDED
                                }
                                initialState = newState
                            }
                        ) {
                            Text(text = initialState.name)
                        }
                    }


                    Button(
                        onClick = { showIndices = !showIndices }
                    ) {
                        Text(text = if(showIndices) "Hide indices" else "Show indices")
                    }

                    Row {
                        Button(
                            onClick = { showItemCount = !showItemCount }
                        ) {
                            Text(text = if(showItemCount) "Hide item count" else "Show item count")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                colors = if(colors == defaultLightColors) defaultDarkColors else defaultLightColors
                            }
                        ) {
                            Text(text = if(colors == defaultLightColors) "Light" else "Dark")
                        }
                    }

                    val pagerState = rememberPagerState(
                        initialPage = 0,
                        pageCount = { 3 }
                    )

                    //Pager to test leaving composition
                    HorizontalPager(
                        state = pagerState
                    ) { pageIndex ->
                        Column(
                            modifier = Modifier.padding(vertical = 16.dp),
                        ) {
                            when (pageIndex) {
                                0 -> {
                                    JsonTree(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                if(colors == defaultLightColors) Color.White else Color.Black
                                            ),
                                        json = json,
                                        onLoading = {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "Loading...")
                                            }
                                        },
                                        initialState = initialState,
                                        colors = colors,
                                        showIndices = showIndices,
                                        showItemCount = showItemCount,
                                        onError = { errorMessage = it.localizedMessage },
                                    )
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

                    errorMessage?.let {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = it,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}