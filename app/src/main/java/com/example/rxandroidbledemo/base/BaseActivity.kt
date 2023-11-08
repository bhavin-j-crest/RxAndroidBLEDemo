package com.example.rxandroidbledemo.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

val ROOT_FRAGMENT_TAG = "ROOT_FRAG"

abstract class BaseActivity<Bind : ViewDataBinding>(@LayoutRes val layoutId: Int) :
    AppCompatActivity() {
    protected lateinit var binding: Bind

    /**
     * Entry point for each activity
     */
    protected abstract fun init()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutId)
        binding.lifecycleOwner = this
        init()
    }

    companion object {
        private const val TAG = "BaseActivity"
    }

}