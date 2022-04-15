package com.example.pokecards

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import com.example.pokecards.collections.pkmn.PkmnAPICard
import com.example.pokecards.collections.pkmn.PkmnAPIDatabase
import com.example.pokecards.databinding.ActivityPhotoBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class PhotoActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityPhotoBinding

    private lateinit var cameraExecutor: ExecutorService

    private var imageCapture: ImageCapture? = null

    private var cardDatabase: PkmnAPIDatabase? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityPhotoBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return
        viewBinding.apply {
            imageCaptureButton.isEnabled = false
            holdStillText.visibility = View.VISIBLE
        }

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    runOnUiThread {
                        viewBinding.apply {
                            holdStillText.visibility = View.INVISIBLE
                            progressBar.visibility = View.VISIBLE
                        }
                    }

                    cameraExecutor.submit {
//                        displayPreviewCapture(image)

                        @SuppressLint("UnsafeOptInUsageError") // I don't know why this is necessary
                        val img: Image = image.image!!
                        val input = InputImage.fromMediaImage(img, image.imageInfo.rotationDegrees)
                        val ocr = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                        ocr.process(input).addOnSuccessListener { ocrText ->
                            cameraExecutor.submit {
                                val tokens = ocrText.text.lowercase()
                                    .replace(ocrCleanupRegex, " ")
                                    .split(Regex("\\s+"))
                                Log.i(TAG, "OCR Tokens:   " + tokens.joinToString(" "))

                                val likely: PkmnAPICard? = findMostLikelyCard(tokens)

                                runOnUiThread {
                                    viewBinding.apply {
                                        resultImageView.setImageDrawable(null)
                                        progressBar.visibility = View.INVISIBLE
                                        imageCaptureButton.isEnabled = true
                                    }
                                }

                                Log.i(TAG, "Photographed card is likely: $likely")
                                startActivity(
                                    Intent(
                                        this@PhotoActivity,
                                        APICardViewActivity::class.java
                                    ).putExtra("card", likely)
                                )
                            }
                        }.addOnFailureListener {
                            Log.e(TAG, "Failed OCR processing", it)
                        }.addOnCompleteListener {
                            image.close()
                            ocr.close()
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Failed to take picture", exception)
                }
            })
    }

    private fun displayPreviewCapture(image: ImageProxy) {
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer[bytes]
        var bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
        buffer.rewind()

        val matrix = Matrix().apply { postRotate(image.imageInfo.rotationDegrees.toFloat()) }
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)

        runOnUiThread { viewBinding.resultImageView.setImageBitmap(bmp) }
    }

    private fun findMostLikelyCard(tokens: List<String>): PkmnAPICard? {
        var maxSimilar = 0
        var likely: PkmnAPICard? = null

        val db = cardDatabase ?: PkmnAPIDatabase(this@PhotoActivity.applicationContext)
        val c = db.db.rawQuery("SELECT * FROM cards", null)
        while (c.moveToNext()) {
            var text = c.getString(1) + " " + c.getString(2)
            val subtypes = c.getStringOrNull(3)
            if (subtypes != null) text += " $subtypes"
            val level = c.getIntOrNull(4)?.toString()
            if (level != null) text += " $level"
            val hp = c.getIntOrNull(5)?.toString()
            if (hp != null) text += " $hp"
            val rules = c.getStringOrNull(14)
            if (rules != null) text += " ${rules.replace("\\", " ")}"
            val attacks = c.getStringOrNull(15)
            if (attacks != null) text += " ${attacks.replace("\\", " ")}"
            val flavortext = c.getStringOrNull(16)
            if (flavortext != null) text += " $flavortext"
            text = text.lowercase().replace(ocrCleanupRegex, " ")

            var i = 0
            for (w in tokens) {
                if (text.contains(w)) i++
            }
            if (i > maxSimilar) {
                maxSimilar = i
                likely = PkmnAPICard(c)
            }
            // TODO: Better token comparison?
        }
        c.close()
        db.close()

        return likely
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            imageCapture =
                ImageCapture.Builder().setCaptureMode(CAPTURE_MODE_MAXIMIZE_QUALITY).build()

//            val imageAnalyzer = ImageAnalysis.Builder()
//                .build()
//                .also {
//                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
//                        Log.d(TAG, "Average luminosity: $luma")
//                    })
//                }

            try {
                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalyzer)
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind camera to lifecycle", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdownNow()
        cardDatabase?.close()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "PokeCards"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
        private val ocrCleanupRegex = Regex("[\\\\().,:\"#/]|(\\s+)|(\\b'\\B)|(\\B'\\b)")
    }


    private class LuminosityAnalyzer(private val listener: (Double) -> Unit) :
        ImageAnalysis.Analyzer {

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        override fun analyze(image: ImageProxy) {

            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()

            listener(luma)

            image.close()
        }
    }
}