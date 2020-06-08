package com.battleshippark.socialloginsample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.battleshippark.socialloginsample.databinding.ActivityFirebaseAuthBinding
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


class FirebaseAuthActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityFirebaseAuthBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var auth: FirebaseAuth

    private var isFacebookSignIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirebaseAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.googleLogin.setOnClickListener(this)
        binding.facebookLogin.setOnClickListener(this)
        binding.googleLink.setOnClickListener(this)
        binding.googleUnlink.setOnClickListener(this)
        binding.facebookLink.setOnClickListener(this)
        binding.facebookUnlink.setOnClickListener(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult?> {
                override fun onSuccess(result: LoginResult?) {
                    Log.i(TAG, "success")
                    handleFacebookAccessToken(result?.accessToken)
                }

                override fun onCancel() {
                    Log.i(TAG, "cancel")
                }

                override fun onError(error: FacebookException?) {
                    Log.i(TAG, "error: $error")
                }
            })

        auth = FirebaseAuth.getInstance()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.google_login -> googleLogin()
            R.id.facebook_login -> facebookLogin()
            R.id.google_link -> googleLink()
            R.id.google_unlink -> googleUnlink()
            R.id.facebook_link -> facebookLink()
            R.id.facebook_unlink -> facebookUnlink()
        }
    }

    private fun facebookUnlink() {
        auth.currentUser?.unlink(FacebookAuthProvider.PROVIDER_ID)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser: FirebaseUser? = auth.currentUser
                    updateUI(currentUser)
                }
            }
    }

    private fun facebookLink() {
        isFacebookSignIn = false
        LoginManager.getInstance()
            .logInWithReadPermissions(this@FirebaseAuthActivity, listOf("email"))
    }

    private fun googleUnlink() {
        auth.currentUser?.unlink(GoogleAuthProvider.PROVIDER_ID)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser: FirebaseUser? = auth.currentUser
                    updateUI(currentUser)
                }
            }
    }

    private fun googleLink() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_LINK)
    }

    private fun facebookLogin() {
        isFacebookSignIn = true
        LoginManager.getInstance()
            .logInWithReadPermissions(this@FirebaseAuthActivity, listOf("email"))
    }

    private fun googleLogin() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            requestCode == RC_SIGN_IN -> {
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogleSignIn(account)
            }
            requestCode == RC_LINK -> {
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogleLink(account)
            }
            FacebookSdk.isFacebookRequestCode(requestCode) -> {
                callbackManager.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun firebaseAuthWithGoogleLink(acct: GoogleSignInAccount?) {
        acct ?: return

        Log.i(TAG, "firebaseAuthWithGoogleLink:" + acct.id)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.currentUser?.linkWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "linkWithCredential:success")
                    val user: FirebaseUser? = task.result?.user
                    updateUI(user)
                } else {
                    Log.w(
                        TAG,
                        "linkWithCredential:failure",
                        task.exception
                    )
                    updateUI(null)
                }
            }
    }

    private fun firebaseAuthWithGoogleSignIn(acct: GoogleSignInAccount?) {
        acct ?: return

        Log.i(TAG, "firebaseAuthWithGoogleLink:" + acct.id)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "signInWithCredential:success")
                    val user: FirebaseUser? = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun handleFacebookAccessToken(token: AccessToken?) {
        Log.d(TAG, "handleFacebookAccessToken:$token")
        token ?: return

        val credential = FacebookAuthProvider.getCredential(token.token)
        if (isFacebookSignIn) {
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithCredential:success")
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        updateUI(null)
                    }
                }
        } else {
            auth.currentUser?.linkWithCredential(credential)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "linkWithCredential:success")
                        val user: FirebaseUser? = task.result?.user
                        updateUI(user)
                    } else {
                        Log.w(
                            TAG,
                            "linkWithCredential:failure",
                            task.exception
                        )
                        updateUI(null)
                    }
                }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        user ?: return
        Log.i(TAG, "user")
        Log.i(TAG, user.uid)
        Log.i(TAG, user.providerId)
        Log.i(TAG, user.email ?: "")
        Log.i(TAG, user.displayName)

        Log.i(TAG, "providerData")
        user.providerData.forEach {
            Log.i(TAG, it.uid)
            Log.i(TAG, it.providerId)
        }
    }

    override fun onStart() {
        super.onStart()

        val currentUser: FirebaseUser? = auth.currentUser
        updateUI(currentUser)
    }

    companion object {
        private const val RC_SIGN_IN = 0x1000
        private const val RC_LINK = 0x1001
        private const val TAG = "SocialLogin"
    }
}
