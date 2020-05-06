package com.luisg.cryptotrade.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luisg.cryptotrade.R
import com.luisg.cryptotrade.model.Crypto
import com.squareup.picasso.Picasso

class CryptosAdapter(val cryptosAdapterListener: CryptosAdapterListener) :
    RecyclerView.Adapter<CryptosAdapter.ViewHolder>() {

    var crytoList: List<Crypto> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.crypto_row, parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount() = crytoList.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val crypto = crytoList[position]

        Picasso.get().load(crypto.imageUrl).into(holder.image)
        holder.name.text = crypto.name
        holder.available.text = holder.available.context.getString(R.string.available_message,crypto.available.toString())
        holder.buyButton.setOnClickListener {
            cryptosAdapterListener.onBuyCryptoCliked(crypto)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image =view.findViewById<ImageView>(R.id.imageCoin)
        var name = view.findViewById<TextView>(R.id.nameTextView)
        var available = view.findViewById<TextView>(R.id.availableTextView)
        var buyButton = view.findViewById<Button>(R.id.buyButton)
    }
}