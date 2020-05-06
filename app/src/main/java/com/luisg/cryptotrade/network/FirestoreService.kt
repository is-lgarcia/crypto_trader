package com.luisg.cryptotrade.network

import com.google.firebase.firestore.FirebaseFirestore
import com.luisg.cryptotrade.model.Crypto
import com.luisg.cryptotrade.model.User

const val CRYPTO_COLECTION_NAME = "cryptos"
const val USERS_COLECTION_NAME = "users"

class FirestoreService (val firebaseFirestore: FirebaseFirestore) {

    fun setDocument(data: Any, colectionName: String, id: String, callBack: CallBack<Void>){
        firebaseFirestore.collection(colectionName).document(id).set(data)
            .addOnSuccessListener { callBack.onSuccess(null) }
            .addOnFailureListener{exception -> callBack.onFailed(exception)}
    }

    fun updateUser(user: User, callBack: CallBack<User>?){
        firebaseFirestore.collection(USERS_COLECTION_NAME).document(user.username)
            .update("cryptoList",user.cryptoList)
            .addOnSuccessListener { result ->
                if (callBack != null)
                    callBack.onSuccess(user)
            }
            .addOnFailureListener { exception ->
                if (callBack != null) {
                    callBack.onFailed(exception)
                }
            }
    }

    fun updateCrypto(crypto: Crypto){
        firebaseFirestore.collection(CRYPTO_COLECTION_NAME).document(crypto.getDocumentId())
            .update("available",crypto.available)
    }

    fun getCryptos(callBack: CallBack<List<Crypto>>?){
        firebaseFirestore.collection(CRYPTO_COLECTION_NAME)
            .get()
            .addOnSuccessListener { result ->
                for(document in result){
                    val cryptoList = result.toObjects(Crypto::class.java)
                    callBack!!.onSuccess(cryptoList)
                    break
                }
            }
            .addOnFailureListener { exception -> callBack!!.onFailed(exception)  }
    }

    fun findUserById(id: String, callBack: CallBack<User>){
        firebaseFirestore.collection(USERS_COLECTION_NAME).document(id)
            .get()
            .addOnSuccessListener { result ->
                if (result.data != null){
                    callBack.onSuccess(result.toObject(User::class.java))
                } else{
                    callBack.onSuccess(null)
                }
            }
            .addOnFailureListener { exception -> callBack.onFailed(exception)  }
    }

    fun listenForUpdate(crypto: List<Crypto>, listener: RealtimeDataListener<Crypto>){
        val cryptoRefence = firebaseFirestore.collection(CRYPTO_COLECTION_NAME)
        for (cryto in crypto){
            cryptoRefence.document(cryto.getDocumentId()).addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null)
                    listener.onError(firebaseFirestoreException)
                if (documentSnapshot != null && documentSnapshot.exists()){
                    listener.onDataChange(documentSnapshot.toObject(Crypto::class.java)!!)
                }

            }
        }
    }

    fun listenForUpdate(user: User, listener: RealtimeDataListener<User>){
        val usersReference = firebaseFirestore.collection(USERS_COLECTION_NAME)
        usersReference.document(user.username).addSnapshotListener { snapshot, exception ->
            if (exception != null)
                listener.onError(exception)
            if (snapshot != null && snapshot.exists()){
                listener.onDataChange(snapshot.toObject(User::class.java)!!)
            }
        }
    }


}