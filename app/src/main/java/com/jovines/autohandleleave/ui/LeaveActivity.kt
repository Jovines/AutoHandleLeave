package com.jovines.autohandleleave.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.jovines.autohandleleave.R
import com.jovines.autohandleleave.bean.SchoolLeavingApproval
import com.jovines.autohandleleave.config.SPKeyConfig.REAL_NAME
import com.jovines.autohandleleave.config.SPKeyConfig.USERNAME
import com.jovines.autohandleleave.config.userSharedPreferencesDelegate
import com.jovines.autohandleleave.databinding.ActivityLeaveBinding
import com.jovines.autohandleleave.databinding.DialogProgressLoadingBinding
import com.jovines.autohandleleave.databinding.ItemRecycleSelectBinding
import com.jovines.autohandleleave.utils.approvalOfLeave
import com.jovines.autohandleleave.utils.checkAskForLeave
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

@SuppressLint("SetTextI18n")
class LeaveActivity : AppCompatActivity() {
    private lateinit var contentView: ActivityLeaveBinding
    private lateinit var subscribe: Disposable

    private val username: String by userSharedPreferencesDelegate(USERNAME)

    private val realName: String by userSharedPreferencesDelegate(REAL_NAME)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentView = DataBindingUtil.setContentView(this, R.layout.activity_leave)

        var shadowList = mutableListOf<SchoolLeavingApproval.Result>()

        var logIdList = mutableMapOf<String, Boolean>()

        val adapter = object : RecyclerView.Adapter<SelectDataBindingViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): SelectDataBindingViewHolder {
                return SelectDataBindingViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_recycle_select, parent, false)
                )
            }

            override fun onBindViewHolder(holder: SelectDataBindingViewHolder, position: Int) {
                val result = shadowList[position]
                holder.leaveBinding?.apply {
                    detailData.text = "姓名：${result.name}\n" +
                            "理由：${result.qjsy}\n" +
                            "请假时间:\n" +
                            "开始:${result.wcrq}\n" +
                            "结束:${result.yjfxsj}"
                    val logId = result.log_id
                    isOk.isChecked = if (!logId.isNullOrEmpty())
                        logIdList[logId] ?: false else false

                    isOk.setOnClickListener {
                        if (!logId.isNullOrEmpty())
                            logIdList[logId] = isOk.isChecked
                    }
                }
            }

            override fun getItemCount() = shadowList.size
        }
        contentView.recycleView.adapter = adapter


        contentView.pass.setOnClickListener {
            handleLeave(logIdList, true)
        }

        contentView.refuse.setOnClickListener {
            handleLeave(logIdList, false)
        }

        subscribe = Observable.interval(0, 5L, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                contentView.progressCircular.visibility = View.VISIBLE
            }
            .observeOn(Schedulers.io())
            .map {
                val list = checkAskForLeave(username)
                val filterList = list.filter { !shadowList.contains(it) }
                if (!filterList.isNullOrEmpty()) {
                    shadowList = list
                    logIdList = logIdList.filter { logId -> list.any { it.log_id == logId.key } }
                        .toMutableMap()
                    true
                } else false
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                contentView.tips.visibility =
                    if (shadowList.isNullOrEmpty()) View.VISIBLE else View.GONE
                contentView.progressCircular.visibility = View.GONE
                if (it) {
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun handleLeave(
        logIdList: MutableMap<String, Boolean>,
        agreeOrNot: Boolean
    ) {
        val dialog = Dialog(this).apply {
            setContentView(R.layout.dialog_progress_loading)
            setCancelable(false)
        }
        val loadingBinding = DataBindingUtil
            .bind<DialogProgressLoadingBinding>(dialog.findViewById(R.id.container))
            ?: return
        dialog.show()
        var totalCount = 0
        Observable.create<Boolean> { emitter ->
            val filter = logIdList.filter { it.value }
            totalCount = filter.size
            filter.forEach { (t, _) ->
                emitter.onNext(
                    approvalOfLeave(
                        username,
                        t,
                        agreeOrNot,
                        realName
                    )
                )
            }
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Boolean> {
                var successCount = 0
                var fieldCount = 0

                override fun onSubscribe(d: Disposable?) {}

                override fun onNext(t: Boolean) {
                    if (t) {
                        successCount++
                    } else {
                        fieldCount++
                    }
                    loadingBinding.description.text =
                        "审批中，请稍等...\n" +
                                "${successCount + fieldCount}/$totalCount"
                }

                override fun onError(e: Throwable?) {
                    Toast.makeText(this@LeaveActivity, "网络错误", Toast.LENGTH_SHORT).show()
                }

                override fun onComplete() {
                    dialog.dismiss()
                    if (fieldCount == 0) {
                        Toast.makeText(this@LeaveActivity, "全部审批成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            this@LeaveActivity, "" +
                                    "成功审批$successCount,失败$fieldCount" +
                                    "", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        subscribe.dispose()
    }


    class SelectDataBindingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val leaveBinding = DataBindingUtil.bind<ItemRecycleSelectBinding>(itemView)
    }
}