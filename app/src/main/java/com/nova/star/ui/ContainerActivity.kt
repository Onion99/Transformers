package com.nova.star.ui

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.nova.star.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class ContainerActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(this).apply {
            setBackgroundResource(R.drawable.bg_obscure_test)
            layoutParams = ViewGroup.LayoutParams(-1,-1)
        })
    }
}