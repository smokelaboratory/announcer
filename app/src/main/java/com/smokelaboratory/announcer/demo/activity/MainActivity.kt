package com.smokelaboratory.announcer.demo.activity

import android.content.Intent
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.smokelaboratory.announcer.demo.Constants
import com.smokelaboratory.announcer.demo.R
import com.smokelaboratory.announcer.demo.data.CounterData
import com.smokelaboratory.announcer.demo.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers

class MainActivity : BaseActivity<ActivityMainBinding>(), View.OnClickListener {

    override fun init() {
        binding.click = this

        /**
         * registers an announcement with [Constants.COUNT_ANNOUNCEMENT] as ID and [CounterData] as data type
         */
        announcer.registerAnnouncement<CounterData>(Constants.COUNT_ANNOUNCEMENT, replayCount = 1)

        listeners()

        setCounter(0)
    }

    override val layoutId: Int = R.layout.activity_main

    private fun setCounter(counterValue: Int) {
        binding.tvCounter.text = getString(R.string.counter_x, counterValue)
    }

    private fun listeners() {
        /**
         * listens to an announcement with [Constants.COUNT_ANNOUNCEMENT] ID on [Dispatchers.Main] thread
         */
        announcer.listenAnnouncement<CounterData>(
            Constants.COUNT_ANNOUNCEMENT,
            lifecycleScope,
            Dispatchers.Main
        ) {
            setCounter(it.counter)
        }
    }

    override fun onClick(v: View) {
        startActivity(Intent(this, SecondActivity::class.java))
    }
}