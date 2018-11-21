package com.dcrandroid.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.dcrandroid.R
import com.dcrandroid.adapter.ConfirmSeedAdapter
import com.dcrandroid.adapter.InputSeed
import com.dcrandroid.data.Constants
import com.dcrandroid.util.DcrConstants
import kotlinx.android.synthetic.main.confirm_seed_page.*

const val CONFIRM_SEED_ACTIVITY = "confirmSeedActivity"

class ConfirmSeedActivity : AppCompatActivity() {

    private var seed = ""
    private var restore: Boolean = false
    private var allSeeds = ArrayList<String>()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private val seedsForInput = ArrayList<InputSeed>()
    private val seedsFromAdapter = ArrayList<InputSeed>()
    private var sortedList = listOf<InputSeed>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.confirm_seed_page)
//        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerViewSeeds.layoutManager = linearLayoutManager

        button_delete_seed.setOnClickListener {
        //TODO: implement removing all entered seeds via "Clear All" button
        }
        button_confirm_seed.setOnClickListener { confirmSeed(sortedList) }
        prepareData()
    }

    private fun prepareData() {
        val bundle = intent.extras
        val temp = arrayOfNulls<String>(33)
        if (!bundle.isEmpty) {
            var inputSeed: InputSeed
            seed = bundle.getString(Constants.SEED)
            restore = bundle.getBoolean(Constants.RESTORE)
            allSeeds = ArrayList(seed.split(" "))
            if (restore) {
                temp.forEachIndexed { number, _ ->
                    inputSeed = InputSeed(number, " ")
                    seedsForInput.add(inputSeed)
                }
            } else {
                allSeeds.forEachIndexed { number, seed ->
                    inputSeed = InputSeed(number, seed)
                    seedsForInput.add(inputSeed)
                }
            }
            Log.d(CONFIRM_SEED_ACTIVITY, "allSeeds: $allSeeds")

            initAdapter()
        }
    }

    private fun initAdapter() {
        recyclerViewSeeds.adapter = ConfirmSeedAdapter(seedsForInput.distinct(), allSeeds, applicationContext,
                { savedSeed: InputSeed ->
                    seedsFromAdapter.add(savedSeed)
                    sortedList = seedsFromAdapter.sortedWith(compareBy { it.number }).distinct()
                    Log.d(CONFIRM_SEED_ACTIVITY, "inside activity: $sortedList")
                },
                { removeSeed: InputSeed ->
                    Log.d(CONFIRM_SEED_ACTIVITY, "sortedList before removing: $sortedList")
                    seedsFromAdapter.clear()
                    seedsFromAdapter.addAll(sortedList)
                    seedsFromAdapter.remove(removeSeed)
                    sortedList = seedsFromAdapter.sortedWith(compareBy { it.number }).distinct()
                    Log.d(CONFIRM_SEED_ACTIVITY, "sortedList after removing: $sortedList")
                })
    }

    private fun confirmSeed(sortedList: List<InputSeed>) {
        val dcrConstants = DcrConstants.getInstance()
        val intent = Intent(this, EncryptWallet::class.java)
        Log.d(CONFIRM_SEED_ACTIVITY, "sortedList.size: ${sortedList.size}")
        val isAllSeeds = sortedList.size == 33

        if (sortedList.isNotEmpty() && isAllSeeds) {
            val finalSeedsString = sortedList.joinToString(" ", "", "", -1, "...") { it.phrase }
            Log.d(CONFIRM_SEED_ACTIVITY, "finalSeedsString: $finalSeedsString")

            val isVerifiedFromDcrConstants = restore && dcrConstants.wallet.verifySeed(finalSeedsString)
            val isTypedSeedsCorrect = !restore && seed == finalSeedsString

            if (isTypedSeedsCorrect || isVerifiedFromDcrConstants) {
                intent.putExtra(Constants.SEED, finalSeedsString)
//                startActivity(intent)
                Toast.makeText(applicationContext, "All seeds are correct! Intent is ready.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, R.string.incorrect_seed_input, Toast.LENGTH_SHORT).show()
            }

        } else if (!isAllSeeds) {
            Toast.makeText(applicationContext, getString(R.string.notAllSeedsEntered), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(applicationContext, getString(R.string.theInputFieldIsEmpty), Toast.LENGTH_SHORT).show()
        }
    }

}
