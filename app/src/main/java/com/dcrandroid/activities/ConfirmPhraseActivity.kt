package com.dcrandroid.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.WindowManager
import com.dcrandroid.R
import com.dcrandroid.adapter.SavedSeedAdapter
import com.dcrandroid.data.Constants
import kotlinx.android.synthetic.main.confirm_seed_page.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

const val INPUT_SAVE_SEED: String = "InputSaveSeed"

class InputSaveSeedActivity : AppCompatActivity() {
    private var seed = ""
    private var restore: Boolean = false
    private var seedsArray = ArrayList<String>()
    private lateinit var linearLayoutManager: LinearLayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.confirm_seed_page)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        prepareData()
    }

    private fun prepareData() {
        val bundle: Bundle = intent.extras
        if (!bundle.isEmpty) {
            seed = bundle.getString(Constants.SEED)
            restore = bundle.getBoolean(Constants.RESTORE)
            seedsArray = ArrayList(seed.split(" "))

            generateRandomSeeds()
        }
    }

    private fun IntRange.random() = Random().nextInt((seedsArray.size + 1) - start)

    private fun generateRandomSeeds() {

        val firstRandom = (1..seedsArray.size).random()
        val secondRandom = (1..seedsArray.size).random()
        val thirdRandom = (1..seedsArray.size).random()

        if (firstRandom != secondRandom && firstRandom != thirdRandom && secondRandom != thirdRandom) {

            val generatedSeedsArray = HashMap<Int, String>()

            for (item in seedsArray) {
                if (item == seedsArray[firstRandom]) generatedSeedsArray[firstRandom + 1] = item
                if (item == seedsArray[secondRandom]) generatedSeedsArray[secondRandom + 1] = item
                if (item == seedsArray[thirdRandom]) generatedSeedsArray[thirdRandom + 1] = item
            }

            initAdapter(generatedSeedsArray.toSortedMap())

        } else {
            generateRandomSeeds()
        }


    }

    private fun initAdapter(sortedSeeds: SortedMap<Int, String>) {
        linearLayoutManager = LinearLayoutManager(this)
        rvSavedSeed.layoutManager = linearLayoutManager
        rvSavedSeed.adapter = SavedSeedAdapter(sortedSeeds, applicationContext)
    }
}

