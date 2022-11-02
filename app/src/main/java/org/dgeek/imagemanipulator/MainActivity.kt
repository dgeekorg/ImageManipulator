package org.dgeek.imagemanipulator

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import org.dgeek.imagemanipulator.ui.screen.ImageManipulator
import org.dgeek.imagemanipulator.ui.screen.OriginalImagePreview
import org.dgeek.imagemanipulator.ui.screen.OutputImagePreview
import org.dgeek.imagemanipulator.ui.screen.TapToOpenText
import org.dgeek.imagemanipulator.ui.theme.ImageManuplatorTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File


class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageManuplatorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .fillMaxHeight(),
                    color = MaterialTheme.colors.background
                ) {
                    val inputImage = viewModel.inputImage.observeAsState()
                    val outputImage = viewModel.outputImage.observeAsState()
                    val sliderValue = viewModel.sliderValue.observeAsState()
                    val radioOptions = listOf("Compress", "Resize")
                    val sliderRange = remember {
                        mutableStateOf(1f..100f)
                    }
                    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
                    val resizeHeight: MutableState<Int> = remember {
                        mutableStateOf(1)
                    }
                    val resizeWidth: MutableState<Int> = remember {
                        mutableStateOf(1)
                    }
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TapToOpenText()
                        OriginalImagePreview(
                            onClick = {
                                openGallery()
                            },
                            imageDetail = inputImage.value?.info ?: "",
                            img = inputImage.value?.image?.asImageBitmap()
                                ?: ImageBitmap.imageResource(id = R.drawable.image)
                        )
                        inputImage.value?.image?.let { viewModel.extractImageInfo(it) }

                        radioOptions.forEach { text ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = text == selectedOption,
                                        onClick = {
                                            onOptionSelected(text)
                                            if (isResizeSelected(text)) {
                                                viewModel.updateSliderValue(1f)
                                                sliderRange.value = 1f..10f
                                            } else {
                                                viewModel.updateSliderValue(1f)
                                                sliderRange.value = 1f..100f
                                            }

                                        }
                                    )
                                    .padding(start = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = text == selectedOption,
                                    onClick = {
                                        onOptionSelected(text)
                                        if (isResizeSelected(text)) {
                                            viewModel.updateSliderValue(1f)
                                            sliderRange.value = 1f..10f
                                        } else {
                                            viewModel.updateSliderValue(1f)
                                            sliderRange.value = 1f..100f
                                        }
                                    }
                                )
                                Text(text = text, modifier = Modifier.padding(start = 16.dp))
                            }
                        }
                        if (selectedOption == radioOptions[1]) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Text(text = "Width:", modifier = Modifier.padding(end = 10.dp))
                                TextField(
                                    modifier = Modifier.width(100.dp),
                                    value = "${resizeWidth.value}",
                                    onValueChange = {
                                        resizeWidth.value = try {
                                            it.toInt()
                                        } catch (e: Exception) {
                                            0
                                        }
                                        println("value changed for")
                                    })
                                Text(
                                    text = "X Height:",
                                    modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                                )
                                TextField(
                                    modifier = Modifier.width(100.dp),
                                    value = "${resizeHeight.value}",
                                    onValueChange = {
                                        resizeHeight.value = try {
                                            it.toInt()
                                        } catch (e: Exception) {
                                            0
                                        }
                                    })
                            }

                        }
                        Row {
                            Slider(
                                modifier = Modifier.weight(1f),
                                value = sliderValue.value?.toFloat() ?: 0f,
                                onValueChange = {
                                    viewModel.updateSliderValue(it)
                                    resizeHeight.value = (viewModel.inputImage.value?.image?.height
                                        ?: 0) / (sliderValue.value ?: 1)
                                    resizeWidth.value = (viewModel.inputImage.value?.image?.width
                                        ?: 0) / (sliderValue.value ?: 1)
                                },
                                valueRange = sliderRange.value
                            )
                            Text(
                                text = "Value : ${sliderValue.value}",
                                modifier = Modifier.padding(10.dp)
                            )
                        }


                        ImageManipulator(text = "Do Magic") {
                            if (selectedOption == radioOptions[0])
                                viewModel.compressImage(
                                    contentResolver,
                                    100 - (sliderValue.value ?: 0)
                                )
                            else
                                viewModel.resizeImage(
                                    contentResolver,
                                    resizeHeight.value,
                                    resizeWidth.value
                                )

                        }
                        Spacer(modifier = Modifier.width(20.dp))



                        if (outputImage.value?.image == null && outputImage.value?.imagePath != null) {
                            extractImageFromUri(
                                outputImage.value?.imagePath!!,
                                viewModel::updateOutputImage
                            )
                            Toast.makeText(
                                this@MainActivity,
                                "Image saved. Check your gallery",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        OutputImagePreview(
                            imageDetail = outputImage.value?.info ?: "",
                            img = outputImage.value?.image?.asImageBitmap()
                                ?: ImageBitmap.imageResource(id = R.drawable.image)
                        )
                    }
                }
            }
        }
    }

    private fun isResizeSelected(text: String): Boolean {
        return text == "Resize"
    }

    private fun createOutputFileDir(): File? {
        var dir: File? =
            File(
                getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    .toString() + "/dgeek"
            )

        // Make sure the path directory exists.
        if (dir?.exists() == false) {
            // Make it, if it doesn't exit
            val success = dir.mkdirs()
            if (!success) {
                dir = null
            }
        }
        return dir
    }

    private fun openGallery() {
        registryForResult.launch("image/*")
    }

    private val registryForResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { it ->
                // The image was saved into the given Uri -> do something with it
                viewModel.updateImageExt(extractImageExt(it))
                extractImageFromUri(it, viewModel::updateInputImage)

            }
        }

    private fun extractImageFromUri(path: Uri, updateData: (ImageData) -> Unit) {
        val source = ImageDecoder.createSource(this.contentResolver, path)
        val bitmap = ImageDecoder.decodeBitmap(source)
        val imageData = viewModel.extractImageInfo(bitmap)
        updateData(ImageData(bitmap, imageData))
    }

    private fun extractImageExt(path: Uri): String {
        val imgExt = contentResolver.getType(path)
        return imgExt?.substring(imgExt.lastIndexOf("/") + 1) ?: ""
    }
}
