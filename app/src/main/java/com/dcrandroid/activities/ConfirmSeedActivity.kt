package com.dcrandroid.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.WindowManager
import com.dcrandroid.R
import com.dcrandroid.adapter.ConfirmSeedAdapter
import com.dcrandroid.adapter.InputSeed
import com.dcrandroid.data.Constants
import kotlinx.android.synthetic.main.confirm_seed_page.*
import java.util.*

const val CONFIRM_SEED_ACTIVITY = "confirmSeedActivity"

class ConfirmSeedActivity : AppCompatActivity() {

    private var seed = ""
    private var restore: Boolean = false
    private var allSeeds = ArrayList<String>()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private val seedsForInput = ArrayList<InputSeed>()
    private val seedsFromAdapter = ArrayList<InputSeed>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.confirm_seed_page)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        prepareData()
    }

    private fun prepareData() {
        val bundle = intent.extras
        if (!bundle.isEmpty) {
            var inputSeed: InputSeed
            seed = bundle.getString(Constants.SEED)
            restore = bundle.getBoolean(Constants.RESTORE)
            allSeeds = ArrayList(seed.split(" "))
            Log.d(CONFIRM_SEED_ACTIVITY, "allSeeds: $allSeeds")
            allSeeds.forEachIndexed { number, seed ->
                inputSeed = InputSeed(number, seed)
                seedsForInput.add(inputSeed)
            }
            initAdapter()
        }
    }

    private fun initAdapter() {
        var sortedList = listOf<InputSeed>()
        linearLayoutManager = LinearLayoutManager(this)
        recyclerViewSeeds.layoutManager = linearLayoutManager
        recyclerViewSeeds.setItemViewCacheSize(33)
        recyclerViewSeeds.adapter = ConfirmSeedAdapter(seedsForInput.distinct(), allSeeds, applicationContext,
                { savedSeed: InputSeed ->
                    seedsFromAdapter.add(savedSeed)
                    sortedList = seedsFromAdapter.sortedWith(compareBy { it.number }).distinct()
                    Log.d(CONFIRM_SEED_ACTIVITY, "sortedList: $sortedList")
                },
                { removeSeed: InputSeed ->
                    Log.d(CONFIRM_SEED_ACTIVITY, "sortedList before dropping: $sortedList")
                    seedsFromAdapter.clear()
                    seedsFromAdapter.addAll(sortedList)
                    seedsFromAdapter.remove(removeSeed)
                    Log.d(CONFIRM_SEED_ACTIVITY, "sortedList after dropping: $sortedList")
                })
        button_confirm_seed.setOnClickListener { confirmSeed(sortedList) }
    }

    private fun confirmSeed(sortedList: List<InputSeed>) {
        if (sortedList.isNotEmpty() && sortedList.containsAll(seedsForInput)) {
            val finalSeedsString = sortedList.joinToString(" ", "", "", -1, "...") { it.phrase }
            Log.d(CONFIRM_SEED_ACTIVITY, "finalSeedsString: $finalSeedsString")
        }

    }

}
