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

    lateinit var adapter: RecyclerView.Adapter<SelectDataBindingViewHolder>

    var shadowList = mutableListOf<SchoolLeavingApproval.Result>()
    var logIdList = mutableMapOf<String, Boolean>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentView = DataBindingUtil.setContentView(this, R.layout.activity_leave)

        contentView.keyWord.visibility = View.GONE

        contentView.back.setOnClickListener { finish() }

        contentView.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            synchronized(this) {
                shadowList.forEach {
                    logIdList[it.log_id.toString()] = isChecked
                }
            }
            adapter.notifyDataSetChanged()
        }

        adapter = object : RecyclerView.Adapter<SelectDataBindingViewHolder>() {
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

                    val recordSelect = {
                        if (!logId.isNullOrEmpty())
                            logIdList[logId] = isOk.isChecked
                    }
                    isOk.setOnClickListener {
                        recordSelect()
                    }

                    root.setOnClickListener {
                        isOk.isChecked = !isOk.isChecked
                        recordSelect()
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
                    synchronized(this){
                        shadowList = list
                    }
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
        val filter = logIdList.filter { it.value }
        if (filter.isEmpty()){
            Toast.makeText(this,"请勾选你需要审批的",Toast.LENGTH_SHORT).show()
            return
        }
        val dialog = Dialog(this).apply {
            setContentView(R.layout.dialog_progress_loading)
            setCancelable(false)
        }
        val loadingBinding = DataBindingUtil
            .bind<DialogProgressLoadingBinding>(dialog.findViewById(R.id.container))
            ?: return
        dialog.show()
        val totalCount= filter.size
        loadingBinding.description.text =
            "审批中，请稍等...\n" +
                    "0/$totalCount"
        Observable.create<Pair<String,Boolean>> { emitter ->
            filter.forEach { (t, _) ->
                emitter.onNext(
                    Pair(t,approvalOfLeave(
                        username,
                        t,
                        agreeOrNot,
                        realName
                    ))
                )
            }
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Pair<String,Boolean>> {
                var successCount = mutableListOf<String>()
                var fieldCount = mutableListOf<String>()

                override fun onSubscribe(d: Disposable?) {}

                override fun onNext(t: Pair<String,Boolean>) {
                    if (t.second) {
                        successCount.add(t.first)
                    } else {
                        fieldCount.add(t.first)
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
                    shadowList = shadowList.filter { !successCount.contains(it.log_id) }.toMutableList()
                    successCount.forEach { logIdList.remove(it) }
                    adapter.notifyDataSetChanged()
                    if (fieldCount.isEmpty()) {
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