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
import com.sebastianneubauer.jsontree.JsonColors
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
                        colors = JsonColors(
                            keyColor = Color.Blue,
                            stringValueColor = Color.Red,
                            numberValueColor = Color.Green,
                            booleanValueColor = Color.Yellow,
                            nullValueColor = Color.Cyan,
                            symbolColor = Color.Black
                        )
                    )
                }
            }
        }
    }

    private val jsonString = """
        {
            "_id": "ZTMREO26GOD0SUJV",
            "name": "Cara Mcpherson",
            "dob": "2020-08-12",
            "addressObject": {
                "street": "6410 Chislehurst Avenue",
                "town": "Gatehouse of Fleet",
                "postcode": 5555,
                "nestedObject": {
                    "integer": 10,
                    "string": "words",
                    "double": 10.5,
                    "boolean": false,
                    "anotherArray": [
                        "hello world"
                    ]
                }
            },
            "telephone": "+353-0817-812-287",
            "petsArray": [
                "bandit",
                "Bentley"
            ],
            "score": 7.5,
            "email": null,
            "url": "https://www.crucial.com",
            "description": "developing extraordinary exercises mall finnish oclc loading radios 
            impressed outcome harvey reputation surround robinson fight hanging championship 
            moreover kde ensures",
            "verified": true,
            "salary": 46078
        }
    """.trimIndent()

}