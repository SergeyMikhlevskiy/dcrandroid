package com.dcrandroid.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.WindowManager
import android.widget.Toast
import com.dcrandroid.R
import com.dcrandroid.adapter.ConfirmSeedAdapter
import com.dcrandroid.adapter.InputSeed
import com.dcrandroid.data.Constants
import com.dcrandroid.util.DcrConstants
import kotlinx.android.synthetic.main.confirm_seed_page.*

class ConfirmSeedActivity : AppCompatActivity() {

    private var seed = ""
    private var restore: Boolean = false
    private var allSeeds = ArrayList<String>()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private val seedsForInput = ArrayList<InputSeed>()
    private val seedsFromAdapter = ArrayList<InputSeed>()
    private var sortedList = listOf<InputSeed>()
    private lateinit var seedAdapter: ConfirmSeedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.confirm_seed_page)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerViewSeeds.layoutManager = linearLayoutManager

        button_delete_seed.setOnClickListener {
            recyclerViewSeeds.removeAllViewsInLayout()
            recyclerViewSeeds.adapter = null
            sortedList = emptyList()
            seedsFromAdapter.clear()
            initAdapter()
            seedAdapter.notifyDataSetChanged()
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
            initAdapter()
        }
    }

    private fun initAdapter() {
        seedAdapter = ConfirmSeedAdapter(seedsForInput.distinct(), allSeeds, applicationContext,
                { savedSeed: InputSeed ->
                    seedsFromAdapter.add(savedSeed)
                    sortedList = seedsFromAdapter.sortedWith(compareBy { it.number }).distinct()
                },
                { removeSeed: InputSeed ->
                    seedsFromAdapter.clear()
                    seedsFromAdapter.addAll(sortedList)
                    seedsFromAdapter.remove(removeSeed)
                    sortedList = seedsFromAdapter.sortedWith(compareBy { it.number }).distinct()
                })
        recyclerViewSeeds.adapter = seedAdapter
    }

    private fun confirmSeed(sortedList: List<InputSeed>) {
        val dcrConstants = DcrConstants.getInstance()
        val intent = Intent(this, EncryptWallet::class.java)
        val isAllSeeds = (sortedList.size == 33)

        if (sortedList.isNotEmpty() && isAllSeeds) {
            val finalSeedsString = sortedList.joinToString(" ", "", "", -1, "...") { it.phrase }
            val isVerifiedFromDcrConstants = restore && dcrConstants.wallet.verifySeed(finalSeedsString)
            val isTypedSeedsCorrect = !restore && seed == finalSeedsString

            if (isTypedSeedsCorrect || isVerifiedFromDcrConstants) {
                intent.putExtra(Constants.SEED, finalSeedsString)
                startActivity(intent)
            } else {
                Toast.makeText(applicationContext, R.string.incorrect_seed_input, Toast.LENGTH_SHORT).show()
            }

        } else if (sortedList.isNotEmpty() && !isAllSeeds) {
            Toast.makeText(applicationContext, getString(R.string.notAllSeedsEntered), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(applicationContext, getString(R.string.theInputFieldIsEmpty), Toast.LENGTH_SHORT).show()
        }
    }

}
