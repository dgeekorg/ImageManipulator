package org.dgeek.imagemanipulator.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp


@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Composable
fun TapToOpenText() {
    Text(text = "Tap Image to open")
}

@Composable
fun OriginalImagePreview(imageDetail: String = "", onClick: () -> Unit, img: ImageBitmap,) {
    Row() {
        Image(
            modifier = Modifier
                .height(250.dp)
                .weight(1f)
                .clickable { onClick.invoke() },
            bitmap = img,
            contentDescription = "Original Image",
        )
        Text(text = imageDetail, modifier = Modifier.padding(10.dp))
    }
}

@Composable
fun ImageManipulator(text: String, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(text = text)
    }
}


@Composable
fun OutputImagePreview(imageDetail: String = "", img: ImageBitmap) {
    Row() {
        Image(
            modifier = Modifier
                .height(250.dp)
                .width(200.dp),
            bitmap = img,
            contentDescription = "Output Image"
        )
        Text(text = imageDetail, modifier = Modifier.padding(10.dp))
    }
}

