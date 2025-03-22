package com.example.sportify.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthManager private constructor() {
    private val auth: FirebaseAuth = Firebase.auth

    companion object {
        val shared = AuthManager()
        private const val TAG = "AuthManager"
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isSignedIn: Boolean
        get() = currentUser != null

    val userId: String
        get() = currentUser?.uid ?: ""

    fun signUp(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        try {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
                        onComplete(true, null)
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        onComplete(false, task.exception?.message)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign up", e)
            onComplete(false, e.message)
        }
    }

    fun signIn(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithEmail:success")
                        onComplete(true, null)
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        onComplete(false, task.exception?.message)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign in", e)
            onComplete(false, e.message)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun sendPasswordResetEmail(email: String, onComplete: (Boolean, String?) -> Unit) {
        try {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Password reset email sent")
                        onComplete(true, null)
                    } else {
                        Log.w(TAG, "Error sending password reset email", task.exception)
                        onComplete(false, task.exception?.message)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending reset email", e)
            onComplete(false, e.message)
        }
    }
}