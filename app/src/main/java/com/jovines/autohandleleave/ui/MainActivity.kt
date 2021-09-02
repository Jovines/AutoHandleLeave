package com.jovines.autohandleleave.ui

import android.app.Dialog
import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.jovines.autohandleleave.BuildConfig
import com.jovines.autohandleleave.R
import com.jovines.autohandleleave.config.SPKeyConfig.PASSWORD
import com.jovines.autohandleleave.config.SPKeyConfig.REAL_NAME
import com.jovines.autohandleleave.config.SPKeyConfig.USERNAME
import com.jovines.autohandleleave.config.userSharedPreferences
import com.jovines.autohandleleave.config.userSharedPreferencesDelegate
import com.jovines.autohandleleave.databinding.ActivityMainBinding
import com.jovines.autohandleleave.databinding.DialogSettingsBinding
import okhttp3.*

class MainActivity : AppCompatActivity() {
    lateinit var contentView: ActivityMainBinding

    private val username: String by userSharedPreferencesDelegate(USERNAME)

    private val realName: String by userSharedPreferencesDelegate(REAL_NAME)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentView = DataBindingUtil.setContentView(this, R.layout.activity_main)

        contentView.leaveButton.setOnClickListener {
            if (username.isEmpty()) {
                Toast.makeText(this, "请设置用户名信息", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (realName.isEmpty()) {
                Toast.makeText(this, "请设置辅导员真名", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, LeaveActivity::class.java))
        }

        contentView.sickButton.setOnClickListener {
            startActivity(Intent(this, SickActivity::class.java))
        }

//        checkoutUpdate { version ->
//            val element = version.elements.firstOrNull() ?: return@checkoutUpdate
//            if (element.versionCode > BuildConfig.VERSION_CODE) {
//                AlertDialog.Builder(this)
//                    .setMessage("检查到有更新是否跳转至浏览器下载？")
//                    .setNeutralButton("取消") { dialogInterface, _ -> dialogInterface.dismiss() }
//                    .setPositiveButton("去下载新版本") { dialogInterface, _ ->
//                        dialogInterface.dismiss()
////                        Single.create<File> {
////                            it.onSuccess(download(getUpdateUri(element), element.outputFile))
////                        }.subscribeOn(Schedulers.io())
////                            .observeOn(AndroidSchedulers.mainThread())
////                            .subscribe({
////                                install(it)
////                            }, {
////                                println()
////                            })
////                        val uri: Uri = Uri.parse(getUpdateUri(element))
////                        val intent = Intent(Intent.ACTION_VIEW, uri)
////                        startActivity(intent)
//                        val request = DownloadManager.Request(Uri.parse(getUpdateUri(element)))
//                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE)
//                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
//                        request.setTitle("下载")
//                        request.setDescription("apk正在下载")
//                        request.setAllowedOverRoaming(false)
//                        request.setDestinationInExternalFilesDir(
//                            this,
//                            Environment.DIRECTORY_DOWNLOADS,
//                            "mydown"
//                        )
//                        val downManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
//                        val id = downManager.enqueue(request)
//                    }.show()
//            }
//        }

        contentView.settingsButton.setOnClickListener {
            Dialog(this).apply {
                setContentView(R.layout.dialog_settings)
                val settingsBinding =
                    DataBindingUtil.bind<DialogSettingsBinding>(findViewById(R.id.container))
                        ?: return@setOnClickListener
                settingsBinding.username.setText(userSharedPreferences().getString(USERNAME, ""))
                settingsBinding.password.setText(userSharedPreferences().getString(PASSWORD, ""))
                settingsBinding.realName.setText(userSharedPreferences().getString(REAL_NAME, ""))
                settingsBinding.save.setOnClickListener {
                    userSharedPreferences {
                        putString(USERNAME, settingsBinding.username.text.toString())
                        putString(PASSWORD, settingsBinding.password.text.toString())
                        putString(REAL_NAME, settingsBinding.realName.text.toString())
                    }

                    Toast.makeText(this@MainActivity, "保存成功", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }.show()
        }
    }

}