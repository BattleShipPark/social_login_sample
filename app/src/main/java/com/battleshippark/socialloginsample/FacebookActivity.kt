package com.battleshippark.socialloginsample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.battleshippark.socialloginsample.databinding.ActivityFacebookBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult


class FacebookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFacebookBinding
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFacebookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        callbackManager = CallbackManager.Factory.create()

        binding.loginButton.setPermissions("email")
/*
        binding.loginButton.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    Log.i("SocialLogin", "success")
                    updateUI()
                }

                override fun onCancel() {
                    Log.i("SocialLogin", "cancel")
                    updateUI()
                }

                override fun onError(error: FacebookException?) {
                    Log.i("SocialLogin", "error: $error")
                    updateUI()
                }
            })
*/

        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult?> {
                override fun onSuccess(result: LoginResult?) {
                    Log.i("SocialLogin", "success2")
                    updateUI()
                }

                override fun onCancel() {
                    Log.i("SocialLogin", "cancel2")
                    updateUI()
                }

                override fun onError(error: FacebookException?) {
                    Log.i("SocialLogin", "error2: $error")
                    updateUI()
                }
            })

        binding.customLoginButton.setOnClickListener {
            LoginManager.getInstance()
                .logInWithReadPermissions(this@FacebookActivity, listOf("email"))
        }

        binding.customLogoutButton.setOnClickListener {
            LoginManager.getInstance().logOut()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        updateUI()
    }

    private fun updateUI() {
        if (AccessToken.isCurrentAccessTokenActive()) {
            binding.status.text = "Logined"
        } else {
            binding.status.text = "Logouted"
        }
    }
}
