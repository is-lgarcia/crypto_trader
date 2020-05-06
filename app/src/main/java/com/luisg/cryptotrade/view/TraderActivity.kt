package com.luisg.cryptotrade.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.TextureView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.luisg.cryptotrade.R
import com.luisg.cryptotrade.adapter.CryptosAdapter
import com.luisg.cryptotrade.adapter.CryptosAdapterListener
import com.luisg.cryptotrade.model.Crypto
import com.luisg.cryptotrade.model.User
import com.luisg.cryptotrade.network.CallBack
import com.luisg.cryptotrade.network.FirestoreService
import com.luisg.cryptotrade.network.RealtimeDataListener
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.lang.Exception

class TraderActivity : AppCompatActivity(), CryptosAdapterListener {

    lateinit var firestoreService: FirestoreService
    private val cryptosAdapter: CryptosAdapter = CryptosAdapter(this)
    private var username: String? = null
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        firestoreService = FirestoreService(FirebaseFirestore.getInstance())
        username = intent.extras!![USERNAME_KEY]!!.toString()
        usernameTextView.text = username

        configureReciclerView()
        loadCryptos()

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            generateCrytoCurrenciesRandom()
        }
    }

    private fun generateCrytoCurrenciesRandom() {
        for (crypto in cryptosAdapter.crytoList){
            val amount = (1..10).random()
            crypto.available += amount
            firestoreService.updateCrypto(crypto)
        }
    }

    private fun loadCryptos() {
        firestoreService.getCryptos(object: CallBack<List<Crypto>>{
            override fun onSuccess(cryptoList: List<Crypto>?) {

                firestoreService.findUserById(username!!, object: CallBack<User>{
                    override fun onSuccess(result: User?) {
                        user = result
                        if (user!!.cryptoList == null){
                            val userCryptoList = mutableListOf<Crypto>()

                            for (crypto in cryptoList!!){
                                val cryptoUser = Crypto()
                                cryptoUser.name = crypto.name
                                cryptoUser.available = crypto.available
                                cryptoUser.imageUrl = crypto.imageUrl
                                userCryptoList.add(cryptoUser)
                            }
                            user!!.cryptoList = userCryptoList
                            firestoreService.updateUser(user!!, null)
                        }
                        loadUserCryptos()
                        addRealtimeDataBaseListeners(user!!, cryptoList!!)
                    }

                    override fun onFailed(exception: Exception) {
                        showGeneralSeverErrorMessage()
                    }

                })

                this@TraderActivity.runOnUiThread {
                    cryptosAdapter.crytoList = cryptoList!!
                    cryptosAdapter.notifyDataSetChanged()
                }


            }

            override fun onFailed(exception: Exception) {
                Log.e("TraderActivity","error loading cryptos", exception)
                showGeneralSeverErrorMessage()
            }

        })
    }

    private fun addRealtimeDataBaseListeners(
        user: User,
        cryptosList: List<Crypto>
    ) {
        firestoreService.listenForUpdate(user, object : RealtimeDataListener<User>{
            override fun onDataChange(updateData: User) {
                this@TraderActivity.user = updateData
                loadCryptos()
            }

            override fun onError(exception: Exception) {
                showGeneralSeverErrorMessage()
            }
        })
        firestoreService.listenForUpdate(cryptosList, object : RealtimeDataListener<Crypto>{
            override fun onDataChange(updateData: Crypto) {
                var pos = 0
                for (crypto in cryptosAdapter.crytoList){
                    if (crypto.name.equals(updateData.name)){
                        crypto.available = updateData.available
                        cryptosAdapter.notifyItemChanged(pos)
                    }
                    pos++
                }
            }

            override fun onError(exception: Exception) {
                showGeneralSeverErrorMessage()
            }

        })
    }

    private fun loadUserCryptos() {
        runOnUiThread {
            if (user != null && user!!.cryptoList != null){
                infoPanel.removeAllViews()
                for (crypto in user!!.cryptoList!!){
                    addUserCryptoInfoRow(crypto)
                }
            }
        }
    }

    private fun addUserCryptoInfoRow(crypto: Crypto) {
        val view = LayoutInflater.from(this).inflate(R.layout.coin_info, infoPanel, false)
        view.findViewById<TextView>(R.id.coinLabel).text =
            getString(R.string.coin_info, crypto.name, crypto.available.toString())
        Picasso.get().load(crypto.imageUrl).into(view.findViewById<ImageView>(R.id.coinIcon))
        infoPanel.addView(view)
    }

    private fun showGeneralSeverErrorMessage() {
        Snackbar.make(fab, getString(R.string.error_while_connecting_to_the_server),Snackbar.LENGTH_SHORT)
            .setAction("Info",null).show()
    }

    private fun configureReciclerView() {
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = cryptosAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBuyCryptoCliked(crypto: Crypto) {
        if (crypto.available > 0){
            for (userCryto in user!!.cryptoList!!){
                if (userCryto.name == crypto.name){
                    userCryto.available += 1
                    break
                }
            }
            crypto.available--
            firestoreService.updateUser(user!!, null)
            firestoreService.updateCrypto(crypto)
        }
    }
}
