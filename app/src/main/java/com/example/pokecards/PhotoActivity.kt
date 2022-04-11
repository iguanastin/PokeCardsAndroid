package com.example.pokecards

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
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
        runOnUiThread { viewBinding.imageCaptureButton.isEnabled = false }

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                cameraExecutor.submit {
                    @SuppressLint("UnsafeOptInUsageError") // I don't know why this is necessary
                    val img: Image = image.image!!
                    val input = InputImage.fromMediaImage(img, image.imageInfo.rotationDegrees)
                    val ocr = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                    ocr.process(input).addOnSuccessListener { ocrText ->
                        runOnUiThread { viewBinding.progressBar.visibility = View.VISIBLE }
                        cameraExecutor.submit {
                            val tokens = ocrText.text.lowercase()
                                .replace(ocrCleanupRegex, " ")
                                .split(Regex("\\s+"))
                            Log.i(TAG, "OCR Tokens:   " + tokens.joinToString(" "))

                            val likely: PkmnCardInfo? =  findMostLikelyCard(tokens)

                            runOnUiThread { viewBinding.progressBar.visibility = View.INVISIBLE }

                            Log.i(TAG, "Photographed card is likely: $likely")
                            startActivity(
                                Intent(
                                    this@PhotoActivity,
                                    TempCardViewActivity::class.java
                                ).putExtra("url", likely?.imageLarge)
                            )
                        }
                    }.addOnFailureListener {
                        Log.e(TAG, "Failed OCR processing", it)
                    }.addOnCompleteListener {
                        image.close()
                        ocr.close()
                        runOnUiThread { viewBinding.imageCaptureButton.isEnabled = true }
                    }
                }

//                val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
//                labeler.process(input).addOnSuccessListener { results ->
//                    Toast.makeText(baseContext, results.joinToString { it.text }, Toast.LENGTH_LONG).show()
//                }.addOnFailureListener {
//                    Log.e(TAG, "Failed to process image with ML", it)
//                }.addOnCompleteListener {
//                    image.close()
//                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Failed to take picture", exception)
            }
        })

//        // Create time stamped name and MediaStore entry.
//        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
//            .format(System.currentTimeMillis())
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
//            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
//            }
//        }
//
//        // Create output options object which contains file + metadata
//        val outputOptions = ImageCapture.OutputFileOptions
//            .Builder(contentResolver,
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                contentValues)
//            .build()
//
//        // Set up image capture listener, which is triggered after photo has
//        // been taken
//        imageCapture.takePicture(
//            outputOptions,
//            ContextCompat.getMainExecutor(this),
//            object : ImageCapture.OnImageSavedCallback {
//                override fun onError(exc: ImageCaptureException) {
//                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
//                }
//
//                override fun
//                        onImageSaved(output: ImageCapture.OutputFileResults){
//                    val msg = "Photo capture succeeded: ${output.savedUri}"
//                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                    Log.d(TAG, msg)
//                }
//            }
//        )
    }

    private fun findMostLikelyCard(tokens: List<String>): PkmnCardInfo? {
        var maxSimilar = 0
        var likely: PkmnCardInfo? = null

        val db = PkmnCardDatabase(this@PhotoActivity.applicationContext)
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
            text = text.lowercase().replace(ocrCleanupRegex," ")

            var i = 0
            for (w in tokens) {
                if (text.contains(w)) i++
            }
            if (i > maxSimilar) {
                maxSimilar = i
                likely = PkmnCardInfo.fromDatabaseCursor(c)
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

            imageCapture = ImageCapture.Builder().build()

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
        cameraExecutor.shutdown()
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



    private class LuminosityAnalyzer(private val listener: (Double) -> Unit) : ImageAnalysis.Analyzer {

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