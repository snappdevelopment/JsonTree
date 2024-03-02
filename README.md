# 🌳 JsonTree

![](https://img.shields.io/maven-central/v/com.sebastianneubauer.jsontree/jsontree) ![](https://img.shields.io/badge/Kotlin-1.9.22-orange) ![](https://img.shields.io/badge/SDK-21+-brightgreen) ![](https://img.shields.io/badge/Android_Weekly-Issue_584-yellow)
<br /><br />
JsonTree is an Android library to display JSON data in Compose with syntax highlighting and more.
<br /><br />

<p float="left">
<img src="screenshots/jsonTree.png" height="450">
<img src="screenshots/jsonTreeGif.gif" height="450">
</p>

## About

JsonTree is an Android library to display formatted JSON data in Compose.
Users can expand/collapse objects and arrays, which can also display additional info like item counts and item indices for arrays. 
JsonTree offers several customizations for visual appearance like syntax highlighting, text style and icons.

## Download

See `maven-central` tag at the top for the latest version.

```groovy
dependencies {
    implementation 'com.sebastianneubauer.jsontree:jsontree:latest-version'
}
```

## How to use

Add `JsonTree` to your Compose UI and customize it with the following options. Get started by only providing your JSON data and a Composable for the initial loading.
```kotlin
JsonTree(
    json = "{ \"key\": \"value\" }",
    onLoading = { Text(text = "Loading...") }
)
```
For more customization, use the following parameter.

```kotlin
JsonTree(
    // The modifier of JsonTree
    modifier = Modifier,
    // Your json data
    json = "{ \"key\": \"value\" }",
    // The Composable to rendered during the initial loading
    onLoading = { Text(text = "Loading...") },
    // The initial state of the tree. Expand the first item or expand/collapse all items
    initialState = TreeState.FIRST_ITEM_EXPANDED,
    // The color palette for your json tree
    colors = TreeColors(
        keyColor = Color.Blue,
        stringValueColor = Color.Red,
        numberValueColor = Color.Green,
        booleanValueColor = Color.Yellow,
        nullValueColor = Color.Yellow,
        indexColor = Color.Black,
        symbolColor = Color.Black,
        iconColor = Color.Black,
    ),
    // The arrow icon for collapsable items
    icon = ImageVector.vectorResource(R.drawable.my_arrow_icon),
    // The size of the arrow icon
    iconSize = 20.dp,
    // The TextStyle to use for the json tree
    textStyle = TextStyle(...),
    // If true, then array items will show their index.
    showIndices = false,
    // If true, then arrays and objects will show the amount of child items when collapsed.
    showItemCount = true,
    // A callback method which is called when the provided json data can't be parsed.
    onError = { throwable -> /* Do something */ }
)
```

## Minimum Requirements

- Min SDK 21
- Compile SDK 34
- JDK 17

## Tech Stack

- Compose UI
- Kotlinx Serialization
- Detekt (Linting)
- API validation
- GitHub Actions
- Gradle version catalog

## License

```
JsonTree
Copyright © 2024 SNAD

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and 
limitations under the License.
```
See [LICENSE](LICENSE.md) to read the full text.
