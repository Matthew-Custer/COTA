package com.example.cota

//import com.google.android.gms.tflite.client.TfLiteInitializationOptions
//import org.tensorflow.lite.task.gms.vision.TfLiteVision
import android.Manifest
import android.R.attr.bitmap
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.opengl.ETC1
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.cota.ml.CotaModel
import com.example.cota.ml.SsdMobilenetV11Metadata1
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.util.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import com.example.cota.ml.CotaMobilenet


class HomeFragment : Fragment(R.layout.fragment_home) {

    lateinit var currentPhotoPath: String
    val paint = Paint()
    lateinit var globalBitmap: Bitmap
    lateinit var model : SsdMobilenetV11Metadata1
    lateinit var cota_ssd_model : CotaMobilenet
    lateinit var cota_model : CotaModel
    lateinit var labels: List<String>
    val imageProcessor = ImageProcessor.Builder().add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR)).build()

    private val CAMERA_REQUEST_CODE = 1

//    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        labels = FileUtil.loadLabels(requireContext().applicationContext, "cota_labels.txt")
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        Log.d("log", "On create in home fragment")
        val button = view.findViewById<Button>(R.id.button_open_camera)
//        val predict_button = view.findViewById<Button>(R.id.button_get_predictions)

        button.setOnClickListener {
            Log.d("log", "Should be opening camera?")

            val permission = Manifest.permission.CAMERA
            val permissionCheck = ContextCompat.checkSelfPermission(requireContext(), permission)

            //Check whether we have permission to use the camera or not
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                // request permission
                requestPermissions(arrayOf(permission), CAMERA_REQUEST_CODE)
            } else {
                // permission granted, proceed with the operation
                dispatchTakePictureIntent()
            }
        }

//        predict_button.setOnClickListener {
//            Log.d("log", "Should be running predictions?")
//            get_predictions()
//        }
//        val model_file = loadModelFile()
//        Log.d("log", "Model file: " + model_file.toString())

        return view
    }

    private fun dispatchTakePictureIntent() {
        Log.d("log", "Open camera method")

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        val photoFile = createImageFile()

        // Continue only if the File was successfully created
        photoFile?.also {
            val photoURI = FileProvider.getUriForFile(requireContext(), requireContext().getApplicationContext().getPackageName() + ".provider", photoFile)
            Log.d("log", "Full photoURI: " + photoURI.toString())
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
        }

    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer? {
//        val fileDescriptor: AssetFileDescriptor? = getActivity()?.getApplicationContext()?.getAssets()?.openFd("lite-model_efficientdet_lite0_detection_metadata_1.tflite")
//        val inputStream = FileInputStream(fileDescriptor?.fileDescriptor)
//        val fileChannel: FileChannel = inputStream.getChannel()
//        val startOffset = fileDescriptor!!.startOffset
//        val declareLength = fileDescriptor.declaredLength
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declareLength)
        return null
    }

    private fun runInferences(){

//        https://www.tensorflow.org/lite/android/quickstart <--- 1
//        https://developers.google.com/ml-kit/vision/object-detection/custom-models/android <-- !!!
//        https://www.tensorflow.org/lite/inference_with_metadata/codegen  --?
//        https://www.tensorflow.org/lite/api_docs/java/org/tensorflow/lite/InterpreterApi -- ?

//

    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getActivity()?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            context?.sendBroadcast(mediaScanIntent)
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {

            Log.d("log", "Photo URI from activityResult: " + currentPhotoPath)
            val imgFile = File(currentPhotoPath)

            if (imgFile.exists()){
                val imageBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                globalBitmap = imageBitmap
                val imageView = view?.findViewById<ImageView>(R.id.image_view)
                imageView?.setImageBitmap(imageBitmap)

                get_predictions()

            }

            runInferences()

//            val imageBitmap = data?.extras?.get("data") as Bitmap
//
//            // Do something with the image (e.g. display it in an ImageView)
//            val imageView = view?.findViewById<ImageView>(R.id.image_view)
//            imageView?.setImageBitmap(imageBitmap)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cota_model.close()
        model.close()
    }

    fun get_predictions(){

        Log.d("log", "Made it to predicitons")

        val scaledBmap = Bitmap.createScaledBitmap(globalBitmap, 1, 1, true)
        val tImg = TensorImage.fromBitmap(scaledBmap)
        val imageSize = scaledBmap.rowBytes * scaledBmap.height

        Log.d("log", "Loaded Bitmap")
        Log.d("Image bytes", "img rowBytes: " + scaledBmap.rowBytes)
        Log.d("Image height", "img height: " + scaledBmap.height)
        Log.d("Image width", "img width: " + scaledBmap.width)
        Log.d("Image size", "img size: " + imageSize)

        cota_model = CotaModel.newInstance(requireContext().applicationContext)
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 1, 1, 3), DataType.UINT8)
        inputFeature0.loadBuffer(tImg.buffer)

//        val cotaOutputs = cota_model.process(inputFeature0)

        //God Jesus please for fucks sake jesus christ ugh
//        val cota_locations = cotaOutputs.outputFeature0AsTensorBuffer.floatArray
//        val cota_classes = cotaOutputs.outputFeature1AsTensorBuffer.floatArray
//        val cota_scores = cotaOutputs.outputFeature2AsTensorBuffer.floatArray
//        val cota_numberOfDetections = cotaOutputs.outputFeature3AsTensorBuffer.floatArray
//        val outputFeature4 = cotaOutputs.outputFeature4AsTensorBuffer.floatArray
//        val outputFeature5 = cotaOutputs.outputFeature5AsTensorBuffer.floatArray
//        val outputFeature6 = cotaOutputs.outputFeature6AsTensorBuffer.floatArray
//        val outputFeature7 = cotaOutputs.outputFeature7AsTensorBuffer.floatArray

//        Log.d("Cota locations:", "arr: " + Arrays.toString(cota_locations))
//        Log.d("Cota classes:", "arr: " + Arrays.toString(cota_classes))
//        Log.d("Cota scores:", "arr: " + Arrays.toString(cota_scores))
//        Log.d("Cota numberOfDetections:", "arr: " + Arrays.toString(cota_numberOfDetections))
//        Log.d("Cota outputFeature4:", "arr: " + Arrays.toString(outputFeature4))
//        Log.d("Cota outputFeature5:", "arr: " + Arrays.toString(outputFeature5))
//        Log.d("Cota outputFeature6:", "arr: " + Arrays.toString(outputFeature6))
//        Log.d("Cota outputFeature7:", "arr: " + Arrays.toString(outputFeature7))


        cota_ssd_model = CotaMobilenet.newInstance(requireContext().applicationContext)
        val cota_ssd_outputs = cota_ssd_model.process(inputFeature0)

        model = SsdMobilenetV11Metadata1.newInstance(requireContext().applicationContext)
        val outputs = model.process(tImg)
        val locations = outputs.locationsAsTensorBuffer.floatArray
        val classes = outputs.classesAsTensorBuffer.floatArray
        val scores = outputs.scoresAsTensorBuffer.floatArray
        val numberOfDetections = outputs.numberOfDetectionsAsTensorBuffer.floatArray

        //Create a mutable bitmap, and then create a canvas from the bitmap so we can draw on it
        var mutable = globalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)

        //Get height and width values from the bitamp
        val h = mutable.height
        val w = mutable.width

        //Adjust the font/stroke size so it's readable
        paint.textSize = h/15f
        paint.strokeWidth = h/85f

        //Change color from black to white
        val color = ContextCompat.getColor(requireContext().applicationContext, R.color.white)
        paint.color = color

        //Write number of total detections in top left corner
        paint.style = Paint.Style.FILL
        Log.d("log", "Number of detections:" + Arrays.toString(numberOfDetections))
        canvas.drawText("Total Detections:" + numberOfDetections[0].toString(), 0f, h/15F, paint)

        var x = 0
        scores.forEachIndexed { index, fl ->
            x = index

            //Skip points until the next top left corner
            x *= 4
            if(fl > 0.5){
                //Set the style to stroke first so that we don't fill the whole shape with color
                paint.style = Paint.Style.STROKE
                canvas.drawRect(RectF(locations[x + 1] * w, locations[x] * h, locations[x + 3] * w, locations[x + 2] * h), paint)
                //Set the style back to fill so it doesn't draw the text as just an outline
                paint.style = Paint.Style.FILL
                canvas.drawText(labels[classes[index].toInt()] + " " + fl.toString(), locations[x + 1] * w, locations[x] * h, paint)
            }
//            13:48 https://www.youtube.com/watch?v=RDARCjVpg8Q
        }

        //Set the image view to display the updated image with detections
        view?.findViewById<ImageView>(R.id.image_view)?.setImageBitmap(mutable)

    }

}