package com.sebastianneubauer.jsontreedemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.sebastianneubauer.jsontree.TreeColors
import com.sebastianneubauer.jsontree.JsonTree
import com.sebastianneubauer.jsontreedemo.ui.theme.JsonTreeTheme

internal class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            JsonTreeTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    JsonTree(
                        json = jsonString,
                    )
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
    		"string": "hello world",
    	    "int": 42,
    	    "float": 42.5,
            "boolean": true,
            "null": null,
            "nestedObject": {
                "string": "hello world",
    	        "int": 42,
    	        "float": 42.5,
                "boolean": true,
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
    	"topLevelArray": [
    		"hello",
    		"world"
    	],
    	"longString": "developing extraordinary exercises mall finnish oclc loading radios impressed outcome harvey reputation surround robinson fight hanging championship moreover kde ensures",
        "anotherBoolean": false
    }
""".trimIndent()

}