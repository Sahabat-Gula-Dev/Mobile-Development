package com.pkm.sahabatgula.ui.home.dailyfood.logfood.scanning

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentFoodScanBinding
import com.pkm.sahabatgula.databinding.FragmentScanBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class FoodScanFragment : Fragment() {

    private var _binding: FragmentFoodScanBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    // Launcher untuk meminta izin kamera
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Izin kamera ditolak.", Toast.LENGTH_SHORT).show()
            }
        }

    // Launcher untuk memilih gambar dari galeri
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            sendUriToParent(uri)
        } else {
            Log.d("Photo Picker", "Tidak ada media yang dipilih")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFoodScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cek izin saat fragment dibuat
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.btnUploadPhoto.setOnClickListener {
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnCheckNutritions.setOnClickListener {
            takePhoto()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.camScanFood.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(requireContext(), "Gagal memulai kamera.", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {

                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    val bitmap = imageProxyToBitmap(image)
                    image.close()

                    val uri = saveBitmapToCache(bitmap)

                    if(uri!= null) {
                        Toast.makeText(requireContext(), "Gambar berhasil diambil!", Toast.LENGTH_SHORT).show()
                        sendUriToParent(uri)
                    } else {
                        Toast.makeText(requireContext(), "Gagal menyimpan gambar temporer.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(exc: ImageCaptureException) {
                    super.onError(exc)
                    Toast.makeText(requireContext(), "Gagal mengambil gambar: ${exc.message}", Toast.LENGTH_SHORT).show()
                    Log.e("CameraX", "Photo capture failed: ${exc.message}", exc)
                }

            }
        )
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun sendUriToParent(uri: Uri?) {
        if (uri == null) {
            Toast.makeText(requireContext(), "Gambar tidak ditemukan. Coba lagi.", Toast.LENGTH_SHORT).show()
            Log.d("DEBUG_NAV", "FoodScanFragment: URI null, tidak ada gambar yang dikirim.")
            return
        }

        Log.d("DEBUG_NAV", "FoodScanFragment: Mengirim URI ke parent. URI: $uri")
        val bundle = Bundle().apply {
            putParcelable("uri", uri)
        }
        parentFragmentManager.setFragmentResult("scanResultKey", bundle)
    }


    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val planeProxy = image.planes[0]
        val buffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val initialBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        val matrix = Matrix().apply {
            postRotate(image.imageInfo.rotationDegrees.toFloat())
        }
        return Bitmap.createBitmap(initialBitmap, 0, 0, initialBitmap.width, initialBitmap.height, matrix, true)
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(requireContext().cacheDir, "images")
            cachePath.mkdirs()

            val timeStamp =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
            val file = File(cachePath, "IMG_${timeStamp}.jpg")

            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            stream.close()

            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }
}