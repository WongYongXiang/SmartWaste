package com.example.smartwaste

import android.content.Context
import android.graphics.Bitmap
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// Constants for the model
// These are the stats used during training in the VS code (refer to the code in VS for the parameters)
private const val MEAN_R = 0.485f
private const val MEAN_G = 0.456f
private const val MEAN_B = 0.406f
private const val STD_R = 0.229f
private const val STD_G = 0.224f
private const val STD_B = 0.225f
private const val INPUT_WIDTH = 224
private const val INPUT_HEIGHT = 224

class ImageClassifier(context: Context) {

    private var model: Module? = null
    private var labels: List<String> = listOf()

    init {
        try {
            // Load the model
            model = Module.load(assetFilePath(context, "waste_model.ptl"))

            // Load the labels
            labels = context.assets.open("labels.txt").bufferedReader().useLines { it.toList() }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Helper function to get the absolute path of the model file
    // This copies the file from 'assets' to a place the app can read
    @Throws(IOException::class)
    private fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }

        context.assets.open(assetName).use { `is` ->
            FileOutputStream(file).use { os ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (`is`.read(buffer).also { read = it } != -1) {
                    os.write(buffer, 0, read)
                }
                os.flush()
            }
            return file.absolutePath
        }
    }

    /**
     * Main function to run classification.
     * It takes a Bitmap (from the camera) and returns the top prediction.
     */
    fun classify(bitmap: Bitmap): String {
        if (model == null || labels.isEmpty()) {
            return "Model or labels not loaded"
        }

        // Preprocessing of image
        // Scale the bitmap to the model's input size (224x224)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_WIDTH, INPUT_HEIGHT, false)

        // Convert the bitmap to a Tensor, applying the normalization
        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            resizedBitmap,
            floatArrayOf(MEAN_R, MEAN_G, MEAN_B),
            floatArrayOf(STD_R, STD_G, STD_B)
        )

        // To run the result
        val outputTensor: Tensor = model!!.forward(IValue.from(inputTensor)).toTensor()

        // Process the result and obtain the scores
        val scores: FloatArray = outputTensor.dataAsFloatArray

        // Find the index with the highest score
        var maxScore = -Float.MAX_VALUE
        var maxScoreIdx = -1
        for (i in scores.indices) {
            if (scores[i] > maxScore) {
                maxScore = scores[i]
                maxScoreIdx = i
            }
        }

        // Return the corresponding label with the highest score
        return if (maxScoreIdx != -1 && maxScoreIdx < labels.size) {
            labels[maxScoreIdx]
        } else {
            "Unknown"
        }
    }
}