package com.dcrandroid.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import com.dcrandroid.R
import com.dcrandroid.data.Constants
import com.dcrandroid.util.DcrConstants
import kotlinx.android.synthetic.main.confirm_seed_page.*
import java.util.*

class ConfirmSeedActivity : AppCompatActivity() {

    private var seed = ""
    private var restore = false
    private lateinit var seedsArray: ArrayList<String>
    private lateinit var arrayAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.confirm_seed_page)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        autoCompleteSeed.setSingleLine()
        autoCompleteSeed.completionHint = getString(R.string.tap_to_select)

        autoCompleteSeed.setOnItemClickListener { parent, _, position, _ ->
            val str = parent.getItemAtPosition(position)
            tvSeedDisplayConfirm.text = String.format("%s %s", tvSeedDisplayConfirm.text.toString().trim(), str)
            autoCompleteSeed.setText("")
        }

        btnConfirmSeed.setOnClickListener { confirmSeeds() }
        btnDeleteSeed.setOnClickListener { removeLastSeed() }
        btnClearSeed.setOnClickListener { deleteAllSeeds() }

        prepareData()
    }

    private fun prepareData() {
        val bundle = intent.extras
        if (!bundle.isEmpty) {
            seed = bundle.getString(Constants.SEED).trim()
            restore = bundle.getBoolean(Constants.RESTORE)
            seedsArray = ArrayList(seed.split(" "))
            arrayAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, seedsArray)
            autoCompleteSeed.setAdapter(arrayAdapter)

            if (restore) {
                Collections.sort(seedsArray, SortIgnoreCase())
            } else {
                seedsArray.shuffle()
            }

        } else {
            Toast.makeText(applicationContext, R.string.error_bundle_null, Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmSeeds() {

        var isTypedSeedsCorrect = false
        var isVerifiedFromDcrConstants = false
        val dcrConstants = DcrConstants.getInstance()

        if (!restore && seed == tvSeedDisplayConfirm.text.toString()) {
            isTypedSeedsCorrect = true
        } else if (restore && dcrConstants.wallet.verifySeed(tvSeedDisplayConfirm.text.toString())) {
            isVerifiedFromDcrConstants = true
        }

        if (isTypedSeedsCorrect || isVerifiedFromDcrConstants) {
            val intent = Intent(this, EncryptWallet::class.java)
                    .putExtra(Constants.SEED, tvSeedDisplayConfirm.text.toString())
            startActivity(intent)
        } else if (tvSeedDisplayConfirm.text.isNotEmpty()) {
            Toast.makeText(applicationContext, R.string.incorrect_seed_input, Toast.LENGTH_SHORT).show()
        } else if (tvSeedDisplayConfirm.text.isNullOrEmpty()) {
            Toast.makeText(applicationContext, getString(R.string.theInputFieldIsEmpty), Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeLastSeed() {
        if (tvSeedDisplayConfirm.text.isNotEmpty()) {
            val existingSeeds = tvSeedDisplayConfirm.text.toString().trim().split(" ")
            tvSeedDisplayConfirm.text = existingSeeds.joinToString(prefix = "", postfix = "", separator = " ", limit = existingSeeds.size - 1, truncated = "")
        }
    }

    private fun deleteAllSeeds() {
        tvSeedDisplayConfirm.text = ""
    }

    inner class SortIgnoreCase : Comparator<String> {
        override fun compare(s1: String?, s2: String?): Int {
            return s1!!.toLowerCase().compareTo(s2!!.toLowerCase())
        }

    }
}


