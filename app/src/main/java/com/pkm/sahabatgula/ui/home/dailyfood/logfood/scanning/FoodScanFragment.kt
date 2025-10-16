package com.pkm.sahabatgula.ui.home.dailyfood.logfood.scanning

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Typeface
import android.graphics.text.LineBreaker
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentFoodScanBinding
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
            if (!allPermissionsGranted()) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                } else {
                    showCameraPermissionDialog()
                }
                return@setOnClickListener
            }
            takePhoto()
        }
    }
    private fun openAppSettings() {
        val intent = android.content.Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireContext().packageName, null)
        )
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
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
                        sendUriToParent(uri)
                    } else {
                        Toast.makeText(requireContext(), "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(exc: ImageCaptureException) {
                    super.onError(exc)
                    Toast.makeText(requireContext(), "Gagal mengambil gambar", Toast.LENGTH_SHORT).show()
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

    private fun showCameraPermissionDialog() {
        val context = requireContext()

        val imageView = ImageView(context).apply {
            setImageResource(R.drawable.glubby_progress)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            val size = context.resources.getDimensionPixelSize(R.dimen.dialog_image_size)
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                gravity = android.view.Gravity.CENTER
                bottomMargin = 16
                topMargin = 24
            }
        }

        val titleText = SpannableString("Izin Kamera").apply {
            setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        val titleView = TextView(context).apply {
            text = titleText
            gravity = android.view.Gravity.CENTER
            textSize = 18f
            setTextColor(Color.BLACK)
            typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_semibold)
            setPadding(16, 0, 16, 8)
        }

        val messageView = TextView(context).apply {
            text = "Izin kamera belum aktif. Aktifkan melalui pengaturan untuk melanjutkan."
            gravity = android.view.Gravity.CENTER
            textSize = 14f
            setTextColor(Color.BLACK)
            typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_regular)
            setPadding(32, 8, 32, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                justificationMode = LineBreaker.JUSTIFICATION_MODE_NONE
            }
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            setBackgroundColor(ContextCompat.getColor(context, R.color.md_theme_onPrimary))
            setPadding(24, 24, 24, 16)
            addView(imageView)
            addView(titleView)
            addView(messageView)
        }

        val dialog = MaterialAlertDialogBuilder(context)
            .setView(container)
            .setPositiveButton("Buka Pengaturan") { d, _ ->
                openAppSettings()
                d.dismiss()
            }
            .setNegativeButton("Batal") { d, _ ->
                d.dismiss()
            }
            .create()

        dialog.show()

        val onPrimaryColor = ContextCompat.getColor(context, R.color.md_theme_onPrimary)

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        val buttonParent = positiveButton.parent as? View
        buttonParent?.setBackgroundColor(onPrimaryColor)

        positiveButton.setBackgroundColor(Color.TRANSPARENT)
        negativeButton.setBackgroundColor(Color.TRANSPARENT)
        positiveButton.setTextColor(Color.BLACK)
        negativeButton.setTextColor(Color.BLACK)

        val customTypeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_semibold)
        positiveButton.typeface = customTypeface
        negativeButton.typeface = customTypeface
        positiveButton.setTypeface(customTypeface, Typeface.BOLD)
        negativeButton.setTypeface(customTypeface, Typeface.BOLD)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }
}