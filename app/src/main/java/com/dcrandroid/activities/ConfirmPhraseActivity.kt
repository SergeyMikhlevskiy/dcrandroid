package com.dcrandroid.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.WindowManager
import android.widget.AutoCompleteTextView
import android.widget.EditText
import com.dcrandroid.R
import com.dcrandroid.adapter.SavedSeedAdapter
import com.dcrandroid.data.Constants
import kotlinx.android.synthetic.main.confirm_seed_page.*
import java.util.*
import kotlin.collections.ArrayList

const val CONFIRM_PHRASE_ACTIVITY: String = "ConfirmPhraseActivity"

class ConfirmPhraseActivity : AppCompatActivity() {
    private var seed = ""
    private var restore: Boolean = false
    private var isEmpty = true
    private var seedsArray = ArrayList<String>()
    private lateinit var linearLayoutManager: LinearLayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.confirm_seed_page)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        prepareData()
    }

    private fun prepareData() {
        val bundle = intent.extras
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

        val numbersOfSeeds = ArrayList<Int>()
        val generatedSeedsArray = ArrayList<String>()

        if (firstRandom != secondRandom && firstRandom != thirdRandom && secondRandom != thirdRandom) {
            for (item in seedsArray) {
                if (item == seedsArray[firstRandom]) {
                    numbersOfSeeds.add(firstRandom + 1)
                    generatedSeedsArray.add(item)
                }
                if (item == seedsArray[secondRandom]) {
                    numbersOfSeeds.add(secondRandom + 1)
                    generatedSeedsArray.add(item)
                }
                if (item == seedsArray[thirdRandom]) {
                    numbersOfSeeds.add(thirdRandom + 1)
                    generatedSeedsArray.add(item)
                }
            }

            val distinctNumbers = numbersOfSeeds.distinct()
            val distinctSeeds = generatedSeedsArray.distinct()

            initAdapter(distinctNumbers, distinctSeeds)
        } else {
            generateRandomSeeds()
        }
    }

    private fun initAdapter(sortedNumbers: List<Int>, sortedSeeds: List<String>) {
        linearLayoutManager = LinearLayoutManager(this)
        recyclerViewSeeds.layoutManager = linearLayoutManager
        recyclerViewSeeds.adapter = SavedSeedAdapter(sortedNumbers, sortedSeeds, seedsArray, applicationContext)
        isEmpty = false
    }

}

