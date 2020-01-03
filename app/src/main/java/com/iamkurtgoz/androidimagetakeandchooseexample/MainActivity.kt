package com.iamkurtgoz.androidimagetakeandchooseexample

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 0
    private val REQUEST_GALLERY_IMAGE = 1
    private var filePath: String = ""

    private lateinit var imgView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imgView = findViewById(R.id.activity_main_imgView)
    }

    fun takePhoto(view: View){
        Dexter.withActivity(this).withPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object: MultiplePermissionsListener{
                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                        token?.continuePermissionRequest()
                    }

                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report == null) return
                        if (report.areAllPermissionsGranted()) { //İzinler tamam ise
                            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE) //Resim çekme intent
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, getImagePath()) //Dosyayı uri olarak ekliyorz
                            if (takePictureIntent.resolveActivity(packageManager) != null) { //Eğer resim çekmemiz için bir uygulama yoksa hata vermesin diye kontrol ediyorız
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE) //Başlatıyoruz
                            }
                        }
                    }
                }).check()
    }

    fun chooseGallery(view: View){
        Dexter.withActivity(this).withPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object: MultiplePermissionsListener{
                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                        token?.continuePermissionRequest()
                    }

                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report == null) return
                        if (report.areAllPermissionsGranted()) {//İzinler tamam ise
                            val pickPhoto: Intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI) //Galeriden resim seçme intenti
                            if (pickPhoto.resolveActivity(packageManager) != null) { //Eğer resim çekmemiz için bir uygulama yoksa hata vermesin diye kontrol ediyorız
                                startActivityForResult(pickPhoto, REQUEST_GALLERY_IMAGE) //Başlatıyoruz
                            }
                        }
                    }
                }).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_IMAGE_CAPTURE -> {
                imgView.setImageBitmap(BitmapFactory.decodeFile(filePath))
            }
            REQUEST_GALLERY_IMAGE -> {
                val imageUri: Uri? = data?.data
                if (imageUri == null) return
                imgView.setImageURI(imageUri)
            }
        }
    }

    fun getImagePath(): Uri{
        try {
            createImageFile()  //Yeni dosyayı oluşturduk
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val imageFile: File = File(filePath) //Dcim klasörüne resim dosyasını ekliyoruz

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //Nougat ve sonrası için
            return FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", imageFile)
        } else {
            return Uri.fromFile(imageFile) //Öncesi için
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) //Dcim klasörünün yolunu aldık
        return File.createTempFile(
            System.currentTimeMillis().toString(), /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            filePath = absolutePath
        }
    }
}
