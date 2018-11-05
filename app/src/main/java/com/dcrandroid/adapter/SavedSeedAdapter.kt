package com.dcrandroid.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dcrandroid.R
import kotlinx.android.synthetic.main.saved_seeds_list_row.view.*
import java.util.*
private const val SAVED_SEED_ADAPTER = "savedSeedAdapter"

class SavedSeedAdapter(private val seedList: SortedMap<Int, String>, val context: Context) : RecyclerView.Adapter<SavedSeedAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(SAVED_SEED_ADAPTER, "onCreateViewHolder")
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.saved_seeds_list_row, parent, false))
    }

    override fun getItemCount(): Int {
        return seedList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var positionInList = ""
        Log.d(SAVED_SEED_ADAPTER, "onBindViewHolder")
        Log.d(SAVED_SEED_ADAPTER, "positionInList before loop: $positionInList")
        for (entry: MutableMap.MutableEntry<Int, String> in seedList.entries) {
            if (entry.value == seedList[position]) {
                positionInList = "Word #${entry.key}"
            }
        }
        Log.d(SAVED_SEED_ADAPTER, "positionInList after loop: $positionInList")

        holder.positionOfSeed.text = positionInList
        holder.savedSeed.setText(seedList[position])

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val positionOfSeed = view.tvPositionOfSeed!!
        val savedSeed = view.tvSavedSeed!!
    }
}