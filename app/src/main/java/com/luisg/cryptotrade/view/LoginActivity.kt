package com.luisg.cryptotrade.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.luisg.cryptotrade.R
import com.luisg.cryptotrade.model.User
import com.luisg.cryptotrade.network.CallBack
import com.luisg.cryptotrade.network.FirestoreService
import com.luisg.cryptotrade.network.USERS_COLECTION_NAME
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.content_main.*
import java.lang.Exception

const val USERNAME_KEY = "username_key"

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var firestoreService: FirestoreService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firestoreService = FirestoreService(FirebaseFirestore.getInstance())
    }

    fun onStartClicked(view: View){
        view.isEnabled=false
        auth.signInAnonymously()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful){
                    val username = etUserLogin.text.toString()
                    firestoreService.findUserById(username, object : CallBack<User>{
                        override fun onSuccess(result: User?) {
                            if (result == null){
                                val user = User()
                                user.username = username
                                saveUserAndStarTraderActivity(user, view)
                            } else{
                                startMainActivity(username)
                            }
                        }

                        override fun onFailed(exception: Exception) {
                            showErrorMessage(view)
                        }
                    })
                }else{
                    showErrorMessage(view)
                    view.isEnabled=true
                }
            }
    }

    private fun saveUserAndStarTraderActivity(user: User, view: View) {
        firestoreService.setDocument(user, USERS_COLECTION_NAME, user.username, object: CallBack<Void>{
            override fun onSuccess(result: Void?) {
                startMainActivity(user.username)
            }

            override fun onFailed(exception: Exception) {
                showErrorMessage(view)
                Log.e(TAG, "error", exception)
                view.isEnabled=true
            }
        })
    }

    private fun showErrorMessage(view: View) {
        Snackbar.make(view, getString(R.string.error_while_connecting_to_the_server), Snackbar.LENGTH_LONG)
            .setAction("Info", null).show()
    }

    private fun startMainActivity(username: String) {
        val intent = Intent(this@LoginActivity, TraderActivity::class.java)
        intent.putExtra(USERNAME_KEY, username)
        startActivity(intent)
        finish()
    }
}
