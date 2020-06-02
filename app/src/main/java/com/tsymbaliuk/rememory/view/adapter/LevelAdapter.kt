package com.tsymbaliuk.rememory.view.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.tsymbaliuk.rememory.R
import com.tsymbaliuk.rememory.model.Level

class LevelAdapter(val context: Context) : RecyclerView.Adapter<LevelAdapter.ThemeViewHolder>() {

    val firestoreReference = FirebaseStorage.getInstance().reference
    var levelList = arrayListOf<Level>()

    private lateinit var clickListener: (Int) -> Unit

    fun setClickListener(clickListener: (Int) -> Unit) {
        this.clickListener = clickListener
    }

    inner class ThemeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flagIV = itemView.findViewById<ImageView>(R.id.flag)
        val drawablesIv = arrayListOf<ImageView>(
            itemView.findViewById<ImageView>(R.id.item_0),
            itemView.findViewById<ImageView>(R.id.item_30),
            itemView.findViewById<ImageView>(R.id.item_60),
            itemView.findViewById<ImageView>(R.id.item_90),
            itemView.findViewById<ImageView>(R.id.item_120),
            itemView.findViewById<ImageView>(R.id.item_150),
            itemView.findViewById<ImageView>(R.id.item_180),
            itemView.findViewById<ImageView>(R.id.item_210),
            itemView.findViewById<ImageView>(R.id.item_240),
            itemView.findViewById<ImageView>(R.id.item_270),
            itemView.findViewById<ImageView>(R.id.item_300),
            itemView.findViewById<ImageView>(R.id.item_330)
        )
        val starsIV = itemView.findViewById<ImageView>(R.id.stars_for_round_passed)
        val lockedIcon = itemView.findViewById<ImageView>(R.id.locked_icon)
        val lockedBackground = itemView.findViewById<ImageView>(R.id.locked_background)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.level_item, parent, false)
        return ThemeViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return levelList.size
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        loadDrawableTo(levelList[position].themeDrawable, holder.flagIV)

        levelList[position].itemDrawables?.forEachIndexed { index, drawable ->
            if (holder.drawablesIv.size > index) {
                loadDrawableTo(drawable, holder.drawablesIv[index])
                holder.drawablesIv[index].setOnClickListener {
                    if (::clickListener.isInitialized) {
                        clickListener(position)
                    }
                }
            }
        }

        //add stars
        //set locked icon and click listener

    }

    fun loadDrawableTo(drawable: String?, imageView: ImageView) {
        val ref = firestoreReference.child( Uri.parse(drawable).path!!)
        Glide.with(context)
            .load(ref)
            .into(imageView)
    }

    fun updateData(newLevelList: ArrayList<Level>) {
        levelList = newLevelList
        notifyDataSetChanged()
    }
}