package com.example.jjjjjppppp.utils

import android.app.AlertDialog
import com.example.jjjjjppppp.network.dto.VersionInfoDto
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.jjjjjppppp.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

object UpdateChecker {

    private var downloadId: Long = -1

    fun checkForUpdate(context: Context) {
        val currentVersionCode = try {
            context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
        } catch (e: PackageManager.NameNotFoundException) { 1 }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.checkVersion(currentVersionCode)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.hasUpdate && body.latestVersion != null) {
                            showUpdateDialog(context, body.latestVersion)
                        } else {
                            Toast.makeText(context, "已是最新版本", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "检查更新失败", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "网络错误: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showUpdateDialog(context: Context, version: VersionInfoDto) {
        val builder = AlertDialog.Builder(context)
            .setTitle("发现新版本 v${version.versionName}")
            .setMessage("更新内容:\n${version.changelog}")

        if (!version.forceUpdate) {
            builder.setNegativeButton("稍后再说", null)
        }

        builder.setPositiveButton("立即更新") { _, _ ->
            downloadAndInstall(context, version.apkUrl, version.versionName)
        }

        builder.setCancelable(!version.forceUpdate)
        builder.show()
    }

    private fun downloadAndInstall(context: Context, apkUrl: String, versionName: String) {
        val fullUrl = if (apkUrl.startsWith("http")) apkUrl else "${RetrofitClient.BASE_URL.trimEnd('/')}$apkUrl"

        val fileName = "toolbox-v$versionName.apk"
        val request = DownloadManager.Request(Uri.parse(fullUrl))
            .setTitle("下载更新 v$versionName")
            .setDescription("正在下载新版本...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = dm.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    context.unregisterReceiver(this)
                    installApk(context, fileName)
                }
            }
        }

        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED)
        Toast.makeText(context, "开始下载...", Toast.LENGTH_SHORT).show()
    }

    private fun installApk(context: Context, fileName: String) {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        if (!file.exists()) {
            Toast.makeText(context, "APK 文件未找到", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "安装失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
