package com.dcrandroid.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.dcrandroid.R
import com.dcrandroid.adapter.CreateWalletAdapter
import com.dcrandroid.adapter.InputSeed
import com.dcrandroid.adapter.MultiSeed
import com.dcrandroid.adapter.RestoreWalletAdapter
import com.dcrandroid.data.Constants
import com.dcrandroid.util.DcrConstants
import kotlinx.android.synthetic.main.confirm_seed_page.*
import java.util.*
import kotlin.collections.ArrayList

class ConfirmSeedActivity : AppCompatActivity() {

    private var seed = ""
    private var restore: Boolean = false
    private var isConfirmSeed = false
    private var allSeeds = ArrayList<String>()
    private val seedsForInput = ArrayList<InputSeed>()
    private val confirmedSeedsArray = ArrayList<InputSeed>()
    private var sortedList = listOf<InputSeed>()
    private val arrayOfRandomSeeds = ArrayList<InputSeed>()
    private val arrayOfSeedLists = ArrayList<MultiSeed>()
    private val shuffledSeeds = ArrayList<InputSeed>()
    private var finalSeedsString = ""
    private var confirmClicks = 0
    private var lastConfirmClick: Long = 0
    private var clickThread: Thread? = null
    private var currentSeedPosition: Int = 0
    private lateinit var restoreWalletAdapter: RestoreWalletAdapter
    private lateinit var createWalletAdapter: CreateWalletAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val decorView = window.decorView
            decorView.systemUiVisibility = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        setContentView(R.layout.confirm_seed_page)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerViewSeeds.layoutManager = linearLayoutManager

        button_delete_seed.setOnClickListener {
            recyclerViewSeeds.removeAllViewsInLayout()
            recyclerViewSeeds.adapter = null
            sortedList = emptyList()
            confirmedSeedsArray.clear()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                button_confirm_seed.background = ContextCompat.getDrawable(applicationContext, R.drawable.btn_shape2)
            } else {
                button_confirm_seed.setBackgroundDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.btn_shape2))
            }
            if (restore) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(recyclerViewSeeds, InputMethodManager.SHOW_IMPLICIT)
                initOldWalletAdapter()
            } else {
                initNewWalletAdapter()
            }

        }
        button_confirm_seed.setOnClickListener(this)

        prepareData()
    }

    private fun prepareData() {
        val bundle = intent.extras
        val temp = arrayOfNulls<String>(33)
        if (!bundle.isEmpty) {
            seed = bundle.getString(Constants.SEED)
            restore = bundle.getBoolean(Constants.RESTORE)
            allSeeds = ArrayList(seed.split(" "))
            if (restore) {
                temp.forEachIndexed { number, _ ->
                    seedsForInput.add(InputSeed(number, " "))
                    initOldWalletAdapter()
                }
            } else {
                initNewWalletAdapter()
            }
        }
    }

    private fun initOldWalletAdapter() {
        restoreWalletAdapter = RestoreWalletAdapter(seedsForInput.distinct(), allSeeds, applicationContext,
                { savedSeed: InputSeed ->
                    confirmedSeedsArray.add(savedSeed)
                    Log.d("confirmSeed", "savedSeed: $savedSeed")
                    sortedList = confirmedSeedsArray.sortedWith(compareBy { it.number }).distinct()
                    Log.d("confirmSeed", "sortedList size: ${sortedList.size}")
                },
                { removeSeed: InputSeed ->
                    confirmedSeedsArray.clear()
                    confirmedSeedsArray.addAll(sortedList)
                    confirmedSeedsArray.remove(removeSeed)
                    Log.d("confirmSeed", "removeSeed: $removeSeed")
                    sortedList = confirmedSeedsArray.sortedWith(compareBy { it.number }).distinct()
                    Log.d("confirmSeed", "sortedList size: ${sortedList.size}")
                })

        recyclerViewSeeds.adapter = restoreWalletAdapter
    }

    private fun initNewWalletAdapter() {
        createWalletAdapter = CreateWalletAdapter(applicationContext, arrayOfSeedLists, { enteredSeeds: ArrayList<InputSeed> ->
            confirmedSeedsArray.clear()
            confirmedSeedsArray.addAll(enteredSeeds)
            sortedList = confirmedSeedsArray.sortedWith(compareBy { it.number }).distinct()
        }, { isAllEntered: Boolean ->
            if (isAllEntered && sortedList.size == 33) {
                handleSingleTap(sortedList)
            }

        })
        recyclerViewSeeds.adapter = createWalletAdapter
        generateRandomSeeds()
    }


    private fun generateRandomSeeds() {
        val firstRandom = (1..allSeeds.size).random()
        val secondRandom = (1..allSeeds.size).random()
        val currentItemPosition = (currentSeedPosition)

        if (firstRandom != secondRandom && firstRandom != currentItemPosition && secondRandom != currentItemPosition) {
            for (item in allSeeds) {
                when (item) {
                    allSeeds[firstRandom] -> arrayOfRandomSeeds.add(InputSeed(firstRandom, item))
                    allSeeds[secondRandom] -> arrayOfRandomSeeds.add(InputSeed(secondRandom, item))
                    allSeeds[currentItemPosition] -> arrayOfRandomSeeds.add(InputSeed(currentItemPosition, item))
                }
            }
            addSeedsToAdapter()
        } else {
            generateRandomSeeds()
        }
    }

    private fun addSeedsToAdapter() {
        shuffledSeeds.addAll(arrayOfRandomSeeds.shuffled().distinct())
        if(shuffledSeeds.size == 3) {
            when {
                currentSeedPosition < 32 -> {
                    arrayOfSeedLists.add(MultiSeed(shuffledSeeds[0], shuffledSeeds[1], shuffledSeeds[2]))
                    createWalletAdapter.notifyDataSetChanged()
                    currentSeedPosition++
                    arrayOfRandomSeeds.clear()
                    shuffledSeeds.clear()
                    generateRandomSeeds()
                }
                currentSeedPosition == 32 -> {
                    arrayOfSeedLists.add(MultiSeed(shuffledSeeds[0], shuffledSeeds[1], shuffledSeeds[2]))
                    createWalletAdapter.notifyDataSetChanged()
                    arrayOfRandomSeeds.clear()
                    shuffledSeeds.clear()
                }
            }
        } else {
            arrayOfRandomSeeds.clear()
            shuffledSeeds.clear()
            generateRandomSeeds()
        }
    }

    override fun onClick(v: View?) {
        var lastClick: Long = 0
        if (lastConfirmClick != 0L) {
            lastClick = System.currentTimeMillis() - lastConfirmClick
        }

        if (lastClick < CLICK_THRESHOLD && confirmClicks < 10) {
            confirmClicks++
            lastConfirmClick = System.currentTimeMillis()
            if (clickThread != null && clickThread!!.isAlive) {
                clickThread!!.interrupt()
            }

            clickThread = Thread {
                try {
                    Thread.sleep((CLICK_THRESHOLD + 5).toLong())
                    var lastClick: Long = 0
                    if (lastConfirmClick != 0L) {
                        lastClick = System.currentTimeMillis() - lastConfirmClick
                    }
                    if (lastClick > CLICK_THRESHOLD) {
                        runOnUiThread {
                            confirmSeeds(isConfirmSeed)
                        }
                    }

                    confirmClicks = 0
                    lastConfirmClick = 0
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }

            clickThread!!.start()
        } else if (lastClick <= CLICK_THRESHOLD && confirmClicks == 10) {
            confirmClicks = 0
            lastConfirmClick = 0
            val enteredSeed = ""
            val i = Intent(this@ConfirmSeedActivity, EncryptWallet::class.java)
                    .putExtra(Constants.SEED, enteredSeed)
            startActivity(i)
        }

        lastConfirmClick = System.currentTimeMillis()
    }

    private fun confirmSeeds(isCorrectSeeds: Boolean) {
        if (isCorrectSeeds) {
            val intent = Intent(this, EncryptWallet::class.java)
            intent.putExtra(Constants.SEED, finalSeedsString)
            startActivity(intent)
        } else {
            handleSingleTap(sortedList)
        }
    }

    private fun handleSingleTap(sortedList: List<InputSeed>) {
        val dcrConstants = DcrConstants.getInstance()
        val isAllSeeds = (sortedList.size == 33)

        if (sortedList.isNotEmpty() && isAllSeeds) {
            finalSeedsString = sortedList.joinToString(" ", "", "", -1, "...") { it.phrase }
            val isVerifiedFromDcrConstants = restore && dcrConstants.wallet.verifySeed(finalSeedsString)
            val isTypedSeedsCorrect = !restore && seed == finalSeedsString

            if (isTypedSeedsCorrect || isVerifiedFromDcrConstants) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    button_confirm_seed.background = ContextCompat.getDrawable(applicationContext, R.drawable.btn_shape3)
                } else {
                    button_confirm_seed.setBackgroundDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.btn_shape3))
                }
                isConfirmSeed = true
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    button_confirm_seed.background = ContextCompat.getDrawable(applicationContext, R.drawable.btn_shape_red)
                } else {
                    button_confirm_seed.setBackgroundDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.btn_shape_red))
                }
                Toast.makeText(applicationContext, R.string.incorrect_seed_input, Toast.LENGTH_SHORT).show()

                isConfirmSeed = false
            }

        } else if (sortedList.isNotEmpty() && !isAllSeeds) {
            Toast.makeText(applicationContext, getString(R.string.notAllSeedsEntered), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(applicationContext, getString(R.string.theInputFieldIsEmpty), Toast.LENGTH_SHORT).show()
        }
    }

    private fun IntRange.random() = Random().nextInt((allSeeds.size + 1) - start)

}
