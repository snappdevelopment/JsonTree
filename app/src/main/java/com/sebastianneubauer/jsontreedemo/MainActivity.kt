package com.sebastianneubauer.jsontreedemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    var errorMessage: String? by remember { mutableStateOf(null) }
                    var json: String by remember { mutableStateOf(jsonChildString) }
                    var colors: TreeColors by remember { mutableStateOf(defaultLightColors) }
                    var initialState: TreeState by remember { mutableStateOf(TreeState.FIRST_ITEM_EXPANDED) }

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "🌳 JsonTree",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = {
                            json = if(json == jsonChildString) jsonObject else jsonChildString
                        }
                    ) {
                        Text(text = if(json == jsonObject) jsonObject else "1 child")
                    }

                    Button(
                        onClick = {
                            colors = if(colors == defaultLightColors) defaultDarkColors else defaultLightColors
                        }
                    ) {
                        Text(text = if(colors == defaultLightColors) "Light" else "Dark")
                    }

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

                    val pagerState = rememberPagerState(0)

                    //Pager to test leaving composition
                    HorizontalPager(
                        pageCount = 3,
                        state = pagerState
                    ) { pageIndex ->
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            when (pageIndex) {
                                0 -> {
                                    JsonTree(
                                        modifier = Modifier.background(
                                            if(colors == defaultLightColors) Color.White else Color.Black
                                        ),
                                        json = json,
                                        initialState = initialState,
                                        colors = colors,
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

    private val jsonChildString = """
    {
    	"object": {
            "longString": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
    	}
    }
""".trimIndent()

    private val jsonObject = "{}"

}