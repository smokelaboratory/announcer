package com.smokelaboratory.announcer.demo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.smokelaboratory.announcer.Announcer
import org.koin.android.ext.android.inject

abstract class BaseActivity<binding : ViewDataBinding> : AppCompatActivity() {

    protected val announcer: Announcer by inject()
    protected lateinit var binding: binding

    protected abstract fun init()
    protected abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, layoutId)
        init()
    }
}