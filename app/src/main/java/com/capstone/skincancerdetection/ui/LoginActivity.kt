package com.capstone.skincancerdetection.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.capstone.skincancerdetection.R
import com.capstone.skincancerdetection.databinding.ActivityLoginBinding
import com.capstone.skincancerdetection.helper.Helper
import com.capstone.skincancerdetection.model.SavedPreference
import com.capstone.skincancerdetection.model.User
import com.capstone.skincancerdetection.model.UserPreference
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

private val Context.datastore: DataStore<Preferences> by preferencesDataStore("settings")

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    lateinit var auth : FirebaseAuth
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var userModel: User



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        userModel = User()

        binding.btnLogin.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setLoginButtonEnable()
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        buttonListener()

        FirebaseApp.initializeApp(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)

        binding.btnRegistWithGoogle.setOnClickListener {
            Toast.makeText(this,"Logging In",Toast.LENGTH_SHORT).show()
            signInGoogle()
        }
    }

    private fun buttonListener(){
        binding.apply {
            btnLogin.setOnClickListener {
                val email = etEmail.text.toString()
                val pass = etPass.text.toString()

                if (email.isEmpty()){
                    binding.etEmail.error = "Email Harus diisi"
                    binding.etEmail.requestFocus()
                    return@setOnClickListener
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    binding.etEmail.error = "Email Tidak Valid"
                    binding.etEmail.requestFocus()
                    return@setOnClickListener
                }

                if (pass.isEmpty()){
                    binding.etPass.error = "Pass Harus diisi"
                    binding.etPass.requestFocus()
                    return@setOnClickListener
                }

                if (pass.length < 6){
                    binding.etPass.error ="Password minimal 6 karakter"
                    binding.etPass.requestFocus()
                    return@setOnClickListener
                }

                LoginFirebase(email,pass)
            }
            linkRegis.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                finish()
            }
        }
    }
    private fun saveUser(email: String, name: String, isLogin: Boolean) {
        val userPreference = UserPreference(this)
        userModel.email = email
        userModel.name = name
        userModel.isLogin = isLogin
        userPreference.setUser(userModel)
    }

    private fun LoginFirebase(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email,pass)
            .addOnCompleteListener(this){
                if (it.isSuccessful){
                    Toast.makeText(this, "Selamat Datang $email", Toast.LENGTH_SHORT ).show()
                    val intent = Intent(this,RoutingActivity::class.java)
                    intent.putExtra(RoutingActivity.EXTRA_RESULT, userModel)
                    startActivity(intent)
                }else {
                    Toast.makeText(this,"${it.exception?.message}", Toast.LENGTH_SHORT).show()

                }


            }

    }

    private fun setLoginButtonEnable(){
        binding.apply {
            btnLogin.setTextButton(getString(R.string.tx_login))
            val isPasswordValid = etPass.text.toString().isNotEmpty() && etPass.text.toString().length > 5
            val isEmailValid = etEmail.text.toString().isNotEmpty() && Helper.isEmailAddressValid(etEmail.text.toString())
            btnLogin.isEnabled = isEmailValid && isPasswordValid
        }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }
    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null){
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_USER, userModel)
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }


    private  fun signInGoogle(){

        val signInIntent: Intent =mGoogleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    companion object {
        private const val TAG = "LoginActivity"
    }

}