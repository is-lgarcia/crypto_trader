package com.luisg.cryptotrade.adapter

import com.luisg.cryptotrade.model.Crypto

interface CryptosAdapterListener {
    fun onBuyCryptoCliked(crypto: Crypto)
}