package com.sebastianneubauer.jsontreedemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.sebastianneubauer.jsontreedemo.ui.theme.JsonTreeTheme

internal class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            JsonTreeTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .background(Color.Black)
                        .padding(16.dp),
                ) {
                    var errorMessage: String? by remember { mutableStateOf(null) }

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "🌳 JsonTree",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    JsonTree(
                        json = jsonString,
                        initialState = TreeState.FIRST_ITEM_EXPANDED,
                        colors = TreeColors(
                            keyColor = Color(0xFF73c8a9),
                            stringValueColor = Color(0xFFbd5532),
                            numberValueColor = Color(0xFFe1b866),
                            booleanValueColor = Color(0xFFdee1b6),
                            nullValueColor = Color(0xFFdee1b6),
                            symbolColor = Color(0xFF798199),
                            iconColor = Color(0xFF798199),
                        ),
                        onError = { errorMessage = it.localizedMessage },
                    )

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

    private val jsonString = """
    {
    	"string": "hello world",
    	"int": 42,
    	"float": 42.5,
        "boolean": true,
        "null": null,
    	"object": {
            "longString": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
            "nestedObject": {
                "string": "hello world",
                "nestedArray": [
                    "hello world"
                ],
                "arrayOfObjects": [
                    {
                        "string": "hello world"
                    },
                    {
                        "int": 42,
                        "anotherInt": 52
                    },
                    {
                    
                    }
                ]
            }
    	},
    	"array": [
    		"hello",
    		"world"
    	]
    }
""".trimIndent()

}