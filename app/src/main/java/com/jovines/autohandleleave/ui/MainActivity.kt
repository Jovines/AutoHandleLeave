package com.jovines.autohandleleave.ui

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.jovines.autohandleleave.databinding.ActivityMainBinding
import com.jovines.autohandleleave.databinding.DialogSettingsBinding
import com.jovines.autohandleleave.utils.UpdateHelper.checkoutUpdate
import com.jovines.autohandleleave.utils.UpdateHelper.getUpdateUri
import okhttp3.*

class MainActivity : AppCompatActivity() {
    lateinit var contentView: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentView = DataBindingUtil.setContentView(this, R.layout.activity_main)

        contentView.leaveButton.setOnClickListener {
            startActivity(Intent(this, LeaveActivity::class.java))
        }

        contentView.sickButton.setOnClickListener {
            startActivity(Intent(this, SickActivity::class.java))
        }

        checkoutUpdate { version ->
            val element = version.elements.firstOrNull() ?: return@checkoutUpdate
            if (element.versionCode > BuildConfig.VERSION_CODE) {
                AlertDialog.Builder(this)
                    .setMessage("检查到有更新是否跳转至浏览器下载？")
                    .setNeutralButton("取消") { dialogInterface, _ -> dialogInterface.dismiss() }
                    .setPositiveButton("去下载新版本") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        val uri: Uri = Uri.parse(getUpdateUri(element))
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        startActivity(intent)
                    }.show()
            }
        }

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