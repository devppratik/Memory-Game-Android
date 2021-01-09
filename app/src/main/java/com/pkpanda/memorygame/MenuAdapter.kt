package com.pkpanda.memorygame

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.pkpanda.memorygame.models.MenuCard
import com.pkpanda.memorygame.utiils.EXTRA_CUSTOM_GAME_NAME


class MenuAdapter(private val context: Context, private val List: List<MenuCard>) :
    RecyclerView.Adapter<MenuAdapter.ViewHolder>() {
    companion object {
        private const val TAG = "MenuActivity"
    }

    override fun getItemCount(): Int = List.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.menu_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gameName = itemView.findViewById<TextView>(R.id.tvCardTitle)
        private val image = itemView.findViewById<ImageView>(R.id.ivCardImage)
        private val menuCard = itemView.findViewById<CardView>(R.id.cvMenuCard)
        fun bind(position: Int) {
            gameName.text = List[position].gameName
            image.setImageResource(List[position].imageUrl)
            menuCard.setOnClickListener {
                Log.i(TAG, "Clicked on card with name = ${List[position].gameName}")
                if (List[position].gameName == "CLIPART") {
                    context.startActivity(Intent(context, MainActivity::class.java))
                } else {
                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra(EXTRA_CUSTOM_GAME_NAME, List[position].gameName)
                    context.startActivity(intent)
                }
            }
        }
    }
}

