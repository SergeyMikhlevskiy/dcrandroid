package com.dcrandroid.adapter

import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import com.dcrandroid.R
import kotlinx.android.synthetic.main.saved_seeds_list_row.view.*

private const val SAVED_SEED_ADAPTER = "savedSeedAdapter"

class SavedSeedAdapter(private val numbersOfSeeds: List<Int>, private val generatedSeedsArray: List<String>, private val allSeeds: ArrayList<String>,
                       val context: Context) : RecyclerView.Adapter<SavedSeedAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.saved_seeds_list_row, parent, false))
    }

    override fun getItemCount(): Int {
        return numbersOfSeeds.size
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val str = "Word #${numbersOfSeeds[position]}"
        val hintAdapter = ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, allSeeds)
        val currentSeed = generatedSeedsArray[position]
        Log.d(SAVED_SEED_ADAPTER, "generatedSeedsArray: $generatedSeedsArray")
        Log.d(SAVED_SEED_ADAPTER, "currentSeed: $currentSeed")

        holder.savedSeed.completionHint = context.getString(R.string.tap_to_select)
        holder.savedSeed.setSingleLine()
        holder.savedSeed.setAdapter(hintAdapter)
        holder.savedSeed.imeOptions = EditorInfo.IME_ACTION_NEXT
        holder.positionOfSeed.text = str

        holder.savedSeed.setOnItemClickListener { parent, view, pos, id ->
            val s = parent.getItemAtPosition(pos) as String
            holder.savedSeed.setText(s)
            holder.savedSeed.setSelection(holder.savedSeed.text.length)
        }
        holder.savedSeed.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s!!.toString().equals(currentSeed)) {
                    Toast.makeText(context, "Correct Seed!", Toast.LENGTH_LONG).show()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val positionOfSeed = view.tvPositionOfSeed!!
        val savedSeed = view.tvSavedSeed!!
    }
}