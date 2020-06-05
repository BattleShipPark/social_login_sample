package com.battleshippark.socialloginsample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.battleshippark.socialloginsample.databinding.ActivityGoogleBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task


class GoogleActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityGoogleBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.signInButton.setOnClickListener(this)
        binding.signOutButton.setOnClickListener(this)
        binding.disconnectButton.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        updateUI(account)
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        account ?: return
        Log.i("SocialLogin", account.id)
        Log.i("SocialLogin", account.email)
        Log.i("SocialLogin", account.displayName)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sign_in_button -> signIn()
            R.id.sign_out_button -> signOut()
            R.id.disconnect_button -> disconnect()
        }
    }

    private fun disconnect() {
        googleSignInClient.revokeAccess()
            .addOnCompleteListener(this) {
                Log.i("SocialLogin", "disconnect")
            }
    }

    private fun signOut() {
        googleSignInClient.signOut()
            .addOnCompleteListener(this) {
                Log.i("SocialLogin", "sign out")
            }
    }

    private fun signIn() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            updateUI(account)
        } catch (e: ApiException) {
            Log.w("SocialLogin", "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }
    }

    companion object {
        private val RC_SIGN_IN = 0x1000
    }
}
