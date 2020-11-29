package com.smokelaboratory.announcer.demo.activity

import android.view.View
import androidx.lifecycle.lifecycleScope
import com.smokelaboratory.announcer.demo.Constants
import com.smokelaboratory.announcer.demo.R
import com.smokelaboratory.announcer.demo.data.CounterData
import com.smokelaboratory.announcer.demo.databinding.ActivitySecondScreenBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SecondActivity : BaseActivity<ActivitySecondScreenBinding>(), View.OnClickListener {

    override fun init() {
        binding.click = this

        setCounter(0)

        listeners()
    }

    override val layoutId: Int = R.layout.activity_second_screen

    private fun listeners() {
        announcer.listenAnnouncement<CounterData>(
            Constants.COUNT_ANNOUNCEMENT,
            lifecycleScope,
            Dispatchers.Main
        ) {
            setCounter(it.counter)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            binding.btUnregister.id -> announcer.unregisterAnnouncement(Constants.COUNT_ANNOUNCEMENT)
            binding.btAnnounce.id -> {
                lifecycleScope.launch {
                    /**
                     * announces data of type [CounterData] to an announcement with [Constants.COUNT_ANNOUNCEMENT] ID
                     */
                    announcer.announce(
                        Constants.COUNT_ANNOUNCEMENT,
                        CounterData(
                            try {
                                binding.etCounter.text.toString().toInt()
                            } catch (e: Exception) {
                                0
                            }
                        )
                    )
                }
            }
        }
    }

    private fun setCounter(counterValue: Int) {
        binding.tvCounter.text = getString(R.string.counter_x, counterValue)
    }
}