package com.file.download.demo

import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    companion object {
        val apiCallMethods: APICallMethods get() = APIHandler.getInstance().handler
    }

    var manager: DownloadManager? = null
    var fullUrlPDF: String = "https://www.slidesharedownloader.com/slideshareappii/convert2pdf.php?url="
    var fullUrlPPT: String = "https://www.slidesharedownloader.com/slideshareappii/convert2pptx.php?url="
    var fullUrlToDownload: String = ""
    var fileName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUrl = findViewById<EditText>(R.id.et_url)
        val btnPdf = findViewById<Button>(R.id.btn_pdf)
        val btnPpt = findViewById<Button>(R.id.btn_ppt)


        btnPdf.setOnClickListener {
            val url = etUrl.text.toString()
            if (url.isEmpty()) {
                showToast("Please Enter Url")
                return@setOnClickListener
            }
            fullUrlToDownload = "$fullUrlPDF$url"
            fileName=getPdfFilename(url)
            checkUrl(url)
        }
        btnPpt.setOnClickListener {
            val url = etUrl.text.toString()
            if (url.isEmpty()) {
                showToast("Please Enter Url")
                return@setOnClickListener
            }
            fullUrlToDownload = "$fullUrlPPT$url"
            fileName=getPptFilename(url)
            checkUrl(url)
        }

    }

    fun downloadFile(fullUrl: String) {
        df(fullUrl)
//        manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//        val uri: Uri = Uri.parse(fullUrl)
//        val request = DownloadManager.Request(uri)
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
//        val reference = manager!!.enqueue(request)
    }

    private fun df(fu: String) {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val uri: Uri = Uri.parse(fu)
        val request = DownloadManager.Request(uri)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or
                DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle("Downloading File")
        request.setDescription(fileName)
        Log.e("FileName",fileName)
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
            override fun onResponse(call: Call<CheckUrlResponse?>?, response: Response<CheckUrlResponse?>) {

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
}

