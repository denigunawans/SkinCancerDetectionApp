package com.capstone.skincancerdetection.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import com.capstone.skincancerdetection.R
import com.capstone.skincancerdetection.databinding.ActivityRegisterBinding
import com.capstone.skincancerdetection.helper.Helper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private  var edtname: EditText? = null

    private val TAG = "CreateAccountActivity"

    lateinit var auth : FirebaseAuth
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()


        mDatabase = FirebaseDatabase.getInstance()
//      mDatabaseReference = mDatabase!!.reference!!.child("name")

        auth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener{
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)

        }

        binding.btnRegister.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setLoginButtonEnable()
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        buttonListener()
    }

    private fun buttonListener(){
        binding.apply {
            btnRegister.setOnClickListener {
                val name = binding.edtName?.text.toString()
                val email = binding.edtEmail.text.toString()
                val pass = binding.edtPassword.text.toString()


                if (email.isEmpty()){
                    binding.edtEmail.error = "Email Harus diisi"
                    binding.edtEmail.requestFocus()
                    return@setOnClickListener
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    binding.edtEmail.error = "Email Tidak Valid"
                    binding.edtEmail.requestFocus()
                    return@setOnClickListener
                }

                if (pass.isEmpty()){
                    binding.edtPassword.error = "Pass Harus diisi"
                    binding.edtPassword.requestFocus()
                    return@setOnClickListener
                }

                if (pass.length < 6){
                    binding.edtPassword.error ="Password minimal 6 karakter"
                    binding.edtPassword.requestFocus()
                    return@setOnClickListener
                }

                RegisterFirebase(email,pass,name)

                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }

            txHaveAccount.setOnClickListener {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            }


        }
    }

    private fun RegisterFirebase(email: String, pass: String, name: String) {
        auth.createUserWithEmailAndPassword(email,pass)

            .addOnCompleteListener(this){
                if (it. isSuccessful){
                    Log.d(TAG, "createUserWithEmail:success")

                    val userId = auth!!.currentUser!!.uid

                    verifyEmail();

                    val currentUserDb = mDatabaseReference!!.child(userId)
                    currentUserDb.child("name").setValue(edtname)
                    updateUserInfoAndUI()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", it.exception)
                    Toast.makeText(this@RegisterActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUserInfoAndUI() {
        //start next activity
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun verifyEmail() {
        val mUser = auth!!.currentUser;
        mUser!!.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@RegisterActivity,
                        "Verification email sent to " + mUser.getEmail(),
                        Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "sendEmailVerification", task.exception)
                    Toast.makeText(this@RegisterActivity,
                        "Failed to send verification email.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun setLoginButtonEnable(){
        binding.apply {
            btnRegister.setTextButton(getString(R.string.tx_daftar))
            val isNameValid = edtName.text.toString().isNotEmpty() && Helper.isNameFormatValid(edtName.text.toString())
            btnRegister.isEnabled = isNameValid
        }

        
    }
}