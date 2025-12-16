package ru.netology.nework.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.intl.Locale
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.type.Date
import java.io.File
import java.text.SimpleDateFormat

class CameraActivity : AppCompatActivity() {

    private lateinit var currentPhotoPath: String
    private var photoUri: Uri? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(
                this,
                "Для использования камеры необходимо разрешение",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(
                    this,
                    "Приложению нужен доступ к камере для съемки фото",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: Exception) {
                    null
                }

                photoFile?.also { file ->
                    photoUri = FileProvider.getUriForFile(
                        this,
                        "${packageName}.fileprovider",
                        file
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            } ?: run {
                Toast.makeText(
                    this,
                    "На устройстве не найдено приложение для камеры",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    @Throws(Exception::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir("Pictures")

        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val resultIntent = Intent().apply {
                        data = photoUri
                        putExtra("photo_path", currentPhotoPath)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                } else {
                    setResult(Activity.RESULT_CANCELED)
                }
                finish()
            }
        }
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }
}