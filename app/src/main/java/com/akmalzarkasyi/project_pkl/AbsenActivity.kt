package com.akmalzarkasyi.project_pkl

import android.Manifest
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModelProvider
import com.akmalzarkasyi.project_pkl.databinding.ActivityAbsenBinding
import com.akmalzarkasyi.project_pkl.utils.BitmapManager.bitmapToBase64
import com.akmalzarkasyi.project_pkl.viewmodel.AbsenViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AbsenActivity : AppCompatActivity() {
    private var REQ_CAMERA = 101
    private var strCurrentLatitude = 0.0
    private var strCurrentLongitude = 0.0
    private var strFilePath: String = ""
    private var strLatitude = "0"
    private var strLongitude = "0"
    private lateinit var fileDirectoty: File
    private lateinit var imageFilename: File
    private lateinit var exifInterface: ExifInterface
    private var strBase64Photo: String = "" // Inisialisasi awal
    private lateinit var strCurrentLocation: String
    private lateinit var strTitle: String
    private lateinit var strTimeStamp: String
    private lateinit var strImageName: String
    private lateinit var absenViewModel: AbsenViewModel
    private lateinit var progressDialog: ProgressDialog
    private lateinit var binding: ActivityAbsenBinding // Deklarasi ViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi ViewBinding
        binding = ActivityAbsenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setInitLayout()
        setCurrentLocation()
        setUploadData()
    }

    private fun setCurrentLocation() {
        progressDialog.show()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            progressDialog.dismiss()
            if (location != null) {
                strCurrentLatitude = location.latitude
                strCurrentLongitude = location.longitude
                val geocoder = Geocoder(this@AbsenActivity, Locale.getDefault())
                try {
                    val addressList =
                        geocoder.getFromLocation(strCurrentLatitude, strCurrentLongitude, 1)
                    if (!addressList.isNullOrEmpty()) {
                        strCurrentLocation = addressList[0].getAddressLine(0)
                        binding.inputLokasi.setText(strCurrentLocation) // Menggunakan binding
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                progressDialog.dismiss()
                Toast.makeText(
                    this@AbsenActivity,
                    "Ups, gagal mendapatkan lokasi. Silahkan periksa GPS atau koneksi internet Anda!",
                    Toast.LENGTH_SHORT
                ).show()
                strLatitude = "0"
                strLongitude = "0"
            }
        }
    }

    private fun setInitLayout() {
        progressDialog = ProgressDialog(this)
        strTitle = intent.extras?.getString(DATA_TITLE).toString()

        binding.tvTitle.text = strTitle

        setSupportActionBar(binding.toolbar) // Menggunakan binding
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        absenViewModel = ViewModelProvider(
            this,
            (ViewModelProvider.AndroidViewModelFactory.getInstance(this.application) as ViewModelProvider.Factory)
        ).get(AbsenViewModel::class.java)

        binding.inputTanggal.setOnClickListener {
            val tanggalAbsen = Calendar.getInstance()
            val date =
                DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    tanggalAbsen[Calendar.YEAR] = year
                    tanggalAbsen[Calendar.MONTH] = monthOfYear
                    tanggalAbsen[Calendar.DAY_OF_MONTH] = dayOfMonth
                    val strFormatDefault = "dd MMMM yyyy HH:mm"
                    val simpleDateFormat = SimpleDateFormat(strFormatDefault, Locale.getDefault())
                    binding.inputTanggal.setText(simpleDateFormat.format(tanggalAbsen.time)) // Menggunakan binding
                }
            DatePickerDialog(
                this@AbsenActivity,
                date,
                tanggalAbsen[Calendar.YEAR],
                tanggalAbsen[Calendar.MONTH],
                tanggalAbsen[Calendar.DAY_OF_MONTH]
            ).show()
        }

        binding.layoutImage.setOnClickListener {
            // Memanggil fungsi untuk mengambil gambar dari kamera
            captureImage()
        }
    }

    private fun captureImage() {
        try {
            val authority = "com.akmalzarkasyi.project_pkl.provider" // Gantilah dengan authority yang sesuai
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(
                MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                    this@AbsenActivity, authority, createImageFile()
                )
            )
            startActivityForResult(cameraIntent, REQ_CAMERA)
        } catch (ex: IOException) {
            Toast.makeText(
                this@AbsenActivity, "Ups, gagal membuka kamera", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun createImageFile(): File {
        strTimeStamp = SimpleDateFormat("dd MMMM yyyy HH:mm:ss").format(Date())
        strImageName = "IMG_"
        fileDirectoty =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "")
        imageFilename = File.createTempFile(strImageName, ".jpg", fileDirectoty)
        strFilePath = imageFilename.absolutePath // Menggunakan `absolutePath`
        return imageFilename
    }

    @Deprecated("Deprecated in Java")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CAMERA && resultCode == RESULT_OK) {
            // Jika pengambilan gambar dari kamera berhasil, maka panggil fungsi konversi gambar
            convertImage()
        }
    }

    private fun convertImage() {
        val imageFile = File(strFilePath)
        if (imageFile.exists()) {
            val options = BitmapFactory.Options()
            var bitmapImage = BitmapFactory.decodeFile(strFilePath, options)

            try {
                exifInterface = ExifInterface(imageFile.absolutePath)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            val matrix = Matrix()
            if (orientation == 6) {
                matrix.postRotate(90f)
            } else if (orientation == 3) {
                matrix.postRotate(180f)
            } else if (orientation == 8) {
                matrix.postRotate(270f)
            }

            bitmapImage = Bitmap.createBitmap(
                bitmapImage, 0, 0, bitmapImage.width, bitmapImage.height, matrix, true
            )

            val resizeImage = (bitmapImage.height * (512.0 / bitmapImage.width)).toInt()
            val scaledBitmap = Bitmap.createScaledBitmap(bitmapImage, 512, resizeImage, true)
            Glide.with(this).load(scaledBitmap).diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_photo_camera)
                .into(binding.imageSelfie) // Menggunakan binding
            strBase64Photo = bitmapToBase64(scaledBitmap)
        } else {
            Toast.makeText(
                this@AbsenActivity, "Ups, foto kamu belum ada!", Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setUploadData() {
        binding.btnAbsen.setOnClickListener {
            val strNama = binding.inputNama.text.toString() // Menggunakan binding
            val strTanggal = binding.inputTanggal.text.toString() // Menggunakan binding
            val strKeterangan = binding.inputKeterangan.text.toString() // Menggunakan binding
            if (strBase64Photo.isEmpty() || strNama.isEmpty() || strCurrentLocation.isEmpty() || strTanggal.isEmpty() || strKeterangan.isEmpty()) {
                Toast.makeText(
                    this@AbsenActivity, "Data tidak boleh ada yang kosong!", Toast.LENGTH_SHORT
                ).show()
            } else {
                absenViewModel.addDataAbsen(
                    strBase64Photo, strNama, strTanggal, strCurrentLocation, strKeterangan
                )
                Toast.makeText(
                    this@AbsenActivity,
                    "Laporan Anda terkirim, tunggu info selanjutnya ya!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                val intent = intent
                finish()
                startActivity(intent)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val DATA_TITLE = "TITLE"
    }
}