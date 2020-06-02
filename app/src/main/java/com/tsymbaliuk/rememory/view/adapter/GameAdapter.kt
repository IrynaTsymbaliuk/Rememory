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
import com.tsymbaliuk.rememory.model.Card
import com.tsymbaliuk.rememory.model.CardState

class GameAdapter(val context: Context) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    val firestore = FirebaseStorage.getInstance()

    private lateinit var clickListener: (Int) -> Unit

    fun setClickListener(clickListener: (Int) -> Unit) {
        this.clickListener = clickListener
    }

    var itemList = emptyList<Card>()
    var oldStateList = arrayListOf<CardState>()
    var newStateList = arrayListOf<CardState>()

    inner class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.game_item, parent, false)
        return GameViewHolder(itemView)
    }

    override fun getItemCount() = itemList.size

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.image.setOnClickListener {
            if (::clickListener.isInitialized) {
                clickListener(holder.adapterPosition)
            }
        }

        when (itemList[position].state) {
            CardState.OPEN -> {
                openCard(holder.image, itemList[position].drawableUri)
            }
            CardState.CLOSE -> {
                closeCard(holder.image)
            }
            CardState.DISCLOSE -> {
                discloseCard(holder.image)
            }
        }

    }

    private fun openCard(imageView: ImageView, drawableUri: String?) {
        flipCard(imageView, null, drawableUri)
    }

    private fun closeCard(imageView: ImageView) {
        flipCard(imageView, R.drawable.shape_rectangle_solid_with_stroke, null)
    }

    private fun discloseCard(imageView: ImageView) {
        flipCard(imageView, null, null)
    }

    private fun flipCard(imageView: ImageView, drawableId: Int?, drawableUri: String?) {
        imageView.animate().withLayer()
            .rotationY(90f)
            .setDuration(150)
            .withEndAction {
                run {
                    when {
                        drawableId != null -> imageView.setImageDrawable(
                            context.getDrawable(
                                drawableId
                            )
                        )
                        drawableUri != null -> loadDrawableTo(drawableUri, imageView)
                        else -> imageView.setImageDrawable(null)
                    }
                    imageView.rotationY = -90f
                    imageView.animate().withLayer()
                        .rotationY(0f)
                        .setDuration(150)
                        .start()
                }
            }.start()
    }

    fun loadDrawableTo(drawableUri: String?, imageView: ImageView) {
        val uri = Uri.parse(drawableUri)
        val ref = firestore.reference.child(uri.path!!)
        Glide.with(context)
            .load(ref)
            .into(imageView)
    }

    fun updateData(newList: ArrayList<Card>) {
        itemList = newList

        oldStateList.clear()
        oldStateList.addAll(newStateList)
        newStateList.clear()
        newList.forEach {
            newStateList.add(it.state)
        }

        if (oldStateList.size == newStateList.size) {
            newStateList.forEachIndexed { index, cardState ->
                if (cardState != oldStateList[index]) notifyItemChanged(index)
            }
        } else notifyDataSetChanged()
    }
}