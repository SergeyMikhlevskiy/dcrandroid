package com.dcrandroid.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.WindowManager
import com.dcrandroid.R
import com.dcrandroid.adapter.InputSeed
import com.dcrandroid.adapter.SavedSeedAdapter
import com.dcrandroid.data.Constants
import kotlinx.android.synthetic.main.confirm_seed_page.*
import java.text.FieldPosition
import java.util.*
import kotlin.collections.ArrayList

const val CONFIRM_PHRASE_ACTIVITY: String = "ConfirmPhraseActivity"

class ConfirmPhraseActivity : AppCompatActivity() {
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
        button_confirm_seed.setOnClickListener { confirmSeed() }
        prepareData()
    }

    private fun prepareData() {
        val bundle = intent.extras
        if (!bundle.isEmpty) {
            seed = bundle.getString(Constants.SEED)
            restore = bundle.getBoolean(Constants.RESTORE)
            allSeeds = ArrayList(seed.split(" "))

            generateRandomSeeds()
        }
    }

    private fun IntRange.random() = Random().nextInt((allSeeds.size + 1) - start)

    private fun generateRandomSeeds() {

        val firstRandom = (1..allSeeds.size).random()
        val secondRandom = (1..allSeeds.size).random()
        val thirdRandom = (1..allSeeds.size).random()
        var inputSeed: InputSeed

        if (firstRandom != secondRandom && firstRandom != thirdRandom && secondRandom != thirdRandom) {
            for (item in allSeeds) {
                if (item == allSeeds[firstRandom]) {
                    inputSeed = InputSeed(firstRandom + 1, item)
                    seedsForInput.add(inputSeed)
                }
                if (item == allSeeds[secondRandom]) {
                    inputSeed = InputSeed(secondRandom + 1, item)
                    seedsForInput.add(inputSeed)
                }
                if (item == allSeeds[thirdRandom]) {
                    inputSeed = InputSeed(thirdRandom + 1, item)
                    seedsForInput.add(inputSeed)
                }
            }

            initAdapter()

        } else {
            generateRandomSeeds()
        }
    }

    private fun initAdapter() {
        linearLayoutManager = LinearLayoutManager(this)
        recyclerViewSeeds.layoutManager = linearLayoutManager
        recyclerViewSeeds.adapter = SavedSeedAdapter(seedsForInput.distinct(), allSeeds, applicationContext) { savedSeed: InputSeed ->
            seedsFromAdapter.add(savedSeed)
        }
    }

    private fun confirmSeed() {
        if (seedsFromAdapter.isNotEmpty()) {

            Log.d("saved", "initAdapter after adding: seedsFromAdapter: $seedsFromAdapter")
            val currentSeeds = seedsFromAdapter.distinct()
            Log.d("saved", "initAdapter after distinct: currentSeeds: $currentSeeds")

            val sortedList = seedsFromAdapter.sortedWith(compareBy { it.number })

            if (sortedList.containsAll(seedsForInput)) {

            }
        }
    }

}


