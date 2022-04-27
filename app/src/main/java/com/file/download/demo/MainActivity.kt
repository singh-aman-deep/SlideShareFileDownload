package com.file.download.demo

import android.Manifest
import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    companion object {
        val apiCallMethods: APICallMethods get() = APIHandler.getInstance().handler
    }

    var fullUrlPDF: String = BuildConfig.PDF_URL
    var fullUrlPPT: String = BuildConfig.PPT_URL
    var fullUrlToDownload: String = ""
    var fileName: String = ""
    var database = FirebaseDatabase.getInstance()
    var isWorking = false
    var myRef = database.getReference("appvone")
    var perms = arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val RC_LOCATION_CONTACTS_PERM = 124
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUrl = findViewById<EditText>(R.id.et_url)
        val btnPdf = findViewById<Button>(R.id.btn_pdf)
        val btnPpt = findViewById<Button>(R.id.btn_ppt)

        myRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = snapshot.value
                isWorking = value as Boolean

            }

            override fun onCancelled(error: DatabaseError) {
                isWorking = false
            }

        })

        btnPdf.setOnClickListener {
            if (isWorking) {
                val url = etUrl.text.toString()
                if (url.isEmpty()) {
                    showToast("Please Enter Url")
                    return@setOnClickListener
                }
                fullUrlToDownload = "$fullUrlPDF$url"
                fileName = getPdfFilename(url)
                checkUrl(url)
            } else {
                showToast("Make Payment first")
            }

        }
        btnPpt.setOnClickListener {
            if (isWorking) {
                val url = etUrl.text.toString()
                if (url.isEmpty()) {
                    showToast("Please Enter Url")
                    return@setOnClickListener
                }
                fullUrlToDownload = "$fullUrlPPT$url"
                fileName = getPptFilename(url)
                checkUrl(url)
            } else {
                showToast("Make Payment first")
            }

        }
    }

    fun downloadFile(fullUrl: String) {
        if (hasPermission()) {
            // Have permission, do the thing!
            df(fullUrl)
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                this,
                "Please grant permission",
                RC_LOCATION_CONTACTS_PERM,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

    }

    private fun df(fu: String) {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val uri: Uri = Uri.parse(fu)
        val request = DownloadManager.Request(uri)
        request.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_WIFI or
                    DownloadManager.Request.NETWORK_MOBILE
        )
        request.setTitle("Downloading File")
        request.setDescription(fileName)
        Log.e("FileName", fileName)
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        request.setMimeType("*/*")
        downloadManager.enqueue(request)
    }

    private fun getPdfFilename(url: String): String {
        return url.getFileName() + ".pdf"
    }

    private fun getPptFilename(url: String): String {
        return url.getFileName() + ".pptx"
    }

    private fun String.getFileName(): String {
        return this.substring(this.lastIndexOf("/") + 1)
    }

    private fun checkUrl(url: String) {
        val call: Call<CheckUrlResponse> = apiCallMethods.checkUrl(url)
        call.enqueue(object : Callback<CheckUrlResponse?> {
            override fun onResponse(
                call: Call<CheckUrlResponse?>?,
                response: Response<CheckUrlResponse?>
            ) {

                if (response.isSuccessful) {
                    if (response.body()!!.urlValid) {
                        showToast("valid Url")
                        downloadFile(fullUrlToDownload)
                    } else {
                        showToast("Invalid Url")
                    }
                } else {
                    showToast("Invalid Url")
                }

            }

            override fun onFailure(call: Call<CheckUrlResponse?>, t: Throwable?) {
                call.cancel()
                showToast("Invalid Url")
            }
        })
    }

    fun showToast(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        df(fullUrlToDownload)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {

    }

    private fun hasPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, *perms)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}

