import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.core.ImageProxy
import android.util.Size
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.view.PreviewView
import com.example.keepfresh.R
import com.google.mlkit.vision.barcode.BarcodeScanner

class BarcodeScannerDialogFragment : DialogFragment() {

    private var cameraExecutor: ExecutorService? = null
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var preview: Preview
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var cameraSelector: CameraSelector
    private lateinit var barcodeScanner: BarcodeScanner
    private lateinit var viewFinder: PreviewView
    private lateinit var btnCancel: Button
    private lateinit var btnConfirm: Button
    private lateinit var barcodeText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_barcode_scanner, container, false)

        // Initialize views
        viewFinder = view.findViewById(R.id.viewFinder)
        btnCancel = view.findViewById(R.id.btnCancel)
        btnConfirm = view.findViewById(R.id.btnConfirm)
        barcodeText = view.findViewById(R.id.barcodeView)

        cameraExecutor = Executors.newSingleThreadExecutor()
        barcodeScanner = BarcodeScanning.getClient()

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnConfirm.setOnClickListener {
            val result = Bundle().apply {
                putString("barcode", barcodeText.text.toString())
            }
            parentFragmentManager.setFragmentResult("barcode_result", result)
            dismiss()
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 1)
        }

        return view
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {
            // Camera provider is now available, bind camera lifecycle
            cameraProvider = cameraProviderFuture.get()

            // Set up CameraX
            preview = Preview.Builder().build()
            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .build()
                .also {
                    cameraExecutor?.let { it1 ->
                        it.setAnalyzer(it1, { image ->
                            processImageProxy(image)
                        })
                    }
                }

            cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
                preview.setSurfaceProvider(viewFinder.surfaceProvider)

            } catch (e: Exception) {
                Log.e("BarcodeScanner", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)

            // Process the barcode with ML Kit
            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val value = barcode.displayValue
                        barcodeText.text = value
                        //Log.d("BarcodeScanner", "Detected barcode: $value")
                    }
                }
                .addOnFailureListener {
                    Log.e("BarcodeScanner", "Barcode scanning failed", it)
                }
                .addOnCompleteListener {
                    image.close()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor?.shutdown()
    }
}
