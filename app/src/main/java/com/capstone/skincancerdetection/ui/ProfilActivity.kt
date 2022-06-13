package com.capstone.skincancerdetection.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.capstone.skincancerdetection.R
import com.capstone.skincancerdetection.databinding.ActivityProfilBinding
import com.capstone.skincancerdetection.helper.Helper
import com.capstone.skincancerdetection.model.Person
import com.capstone.skincancerdetection.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*

class ProfilActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProfilBinding
    private lateinit var textEditProfile: String
    private lateinit var textSaveProfile: String

    lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var user: User

    private val personCollectionRef = Firebase.firestore.collection("persons")

    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    lateinit var auth : FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        user = intent.getParcelableExtra(MainActivity.EXTRA_USER)!!

        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        val user = auth.currentUser

        //kondisi user sedang login atau tidak
        if (user != null){

            binding.edtName.setText(user.displayName)
            binding.edtEmail.setText(user.email)

            if (user.isEmailVerified){
                binding.apply {
                    iconVerify.visibility = View.VISIBLE
                    iconNotVerify.visibility = View.GONE
                }



            } else {
                binding.apply {
                    iconVerify.visibility = View.GONE
                    iconNotVerify.visibility = View.VISIBLE
                }
            }

        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)


        binding.btnLogout.setOnClickListener{

            btnLogout()
        }

        binding.btnVerify.setOnClickListener{
            emailVerification()
        }

        binding.imgProfil.setOnClickListener { launchGallery() }
        binding.btnUploadImage.setOnClickListener { uploadImage() }

        textEditProfile = getString(R.string.tx_edit_profil)
        textSaveProfile = getString(R.string.tx_save_profil)

        binding.btnEditProfil.setOnClickListener{


            editButtonListener()

            //button ganti email
            binding.btnChangeEmail.setOnClickListener {
                changeEmail()
            }

            //button change pass
            binding.btnChangePass.setOnClickListener {
                changePass()
            }

        }
//        scanListener()






    }

    private fun changeEmail() {
        val intent = Intent(this, ChangeEmailActivity::class.java)
        startActivity(intent)
    }

    private fun changePass() {
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        binding.cvCurrentPass.visibility = View.VISIBLE

        binding.btnCancel.setOnClickListener {
            binding.cvCurrentPass.visibility = View.GONE
        }

        binding.btnConfirm.setOnClickListener btnConfirm@{

            val pass = binding.edtCurrentPassword.text.toString()

            if (pass.isEmpty()) {
                binding.edtCurrentPassword.error = "Password Tidak Boleh Kosong"
                binding.edtCurrentPassword.requestFocus()
                return@btnConfirm
            }

            user.let {
                val userCredential = EmailAuthProvider.getCredential(it?.email!!,pass)
                it.reauthenticate(userCredential).addOnCompleteListener { task ->
                    when {
                        task.isSuccessful -> {
                            binding.cvCurrentPass.visibility = View.GONE
                            binding.cvUpdatePass.visibility = View.VISIBLE
                        }
                        task.exception is FirebaseAuthInvalidCredentialsException -> {
                            binding.edtCurrentPassword.error = "Password Salah"
                            binding.edtCurrentPassword.requestFocus()
                        }
                        else -> {
                            Toast.makeText(this, "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            binding.btnNewCancel.setOnClickListener {
                binding.cvCurrentPass.visibility = View.GONE
                binding.cvUpdatePass.visibility = View.GONE
            }

            binding.btnNewChange.setOnClickListener newChangePassword@{

                val newPass = binding.edtNewPass.text.toString()
                val passConfirm = binding.edtConfirmPass.text.toString()

                if (newPass.isEmpty()) {
                    binding.edtCurrentPassword.error = "Password Tidak Boleh Kosong"
                    binding.edtCurrentPassword.requestFocus()
                    return@newChangePassword
                }

                if(passConfirm.isEmpty()){
                    binding.edtCurrentPassword.error = "Ulangi Password Baru"
                    binding.edtCurrentPassword.requestFocus()
                    return@newChangePassword
                }

                if (newPass.length < 6) {
                    binding.edtCurrentPassword.error = "Password Harus Lebih dari 6 Karakter"
                    binding.edtCurrentPassword.requestFocus()
                    return@newChangePassword
                }

                if (passConfirm.length < 6) {
                    binding.edtCurrentPassword.error = "Password Tidak Sama"
                    binding.edtCurrentPassword.requestFocus()
                    return@newChangePassword
                }

                if (newPass != passConfirm) {
                    binding.edtCurrentPassword.error = "Password Tidak Sama"
                    binding.edtCurrentPassword.requestFocus()
                    return@newChangePassword
                }

                user?.let {
                    user.updatePassword(newPass).addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(this, "Password Berhasil diUpdate", Toast.LENGTH_SHORT).show()
                            successLogout()
                        } else {
                            Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }
        }
    }

    private fun successLogout() {
        auth = FirebaseAuth.getInstance()
        auth.signOut()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()

        Toast.makeText(this, "Silahkan Login Kembali", Toast.LENGTH_SHORT).show()
    }

    private fun emailVerification() {
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(this, "Email Verifikasi Telah Dikirim", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun btnLogout() {
        if(auth != null) {
//            auth = FirebaseAuth.getInstance()
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
//        }else{
//            mGoogleSignInClient.signOut().addOnCompleteListener {
//                val intent= Intent(this, LoginActivity::class.java)
//                Toast.makeText(this,"Logging Out",Toast.LENGTH_SHORT).show()
//                startActivity(intent)
//
//            }
//        }
    }

    private fun launchGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data == null || data.data == null){
                return
            }

            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                binding.imgProfil.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage(){




        if(filePath != null){
            val ref =  FirebaseStorage.getInstance().reference.child("img_user/${FirebaseAuth.getInstance().currentUser?.email}")
            val uploadTask = ref?.putFile(filePath!!)

        }else{
            Toast.makeText(this, "Please Upload an Image", Toast.LENGTH_SHORT).show()
        }
    }





    private fun goIntent(cls: Class<*>){
        val intent = Intent(this, cls)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

//    private fun scanListener(){
//        binding.btnScan.setOnClickListener {
//            val intent = Intent(this, ScanActivity::class.java)
//            startActivity(intent)
//        }
//    }

    private fun editButtonListener(){
//        binding.apply {
//            btnEditProfil.setTextButton(textEditProfile)
//            setColumnEditable(false)
//            btnEditProfil.setOnClickListener {
//                if (btnEditProfil.text == textEditProfile){
//                    btnEditProfil.setTextButton(textSaveProfile)
//                    setColumnEditable(true)
//                }else{
//                    btnEditProfil.setTextButton(textEditProfile)
//                    setColumnEditable(false)
//                }
//            }
//        }

        val nameuser = binding.edtName.text.toString()
        val emailuser = binding.edtEmail.text.toString()

        val person = Person(nameuser, emailuser)
        savePerson(person)




    }
    private fun savePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        try {
            personCollectionRef.add(person).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ProfilActivity, "Successfully saved data.", Toast.LENGTH_LONG).show()
            }
        } catch(e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ProfilActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setColumnEditable(isTrue: Boolean){
        binding.apply {
            edtName.isEnabled = isTrue
            edtEmail.isEnabled = isTrue

        }
    }

    companion object{
        const val EXTRA_USER = "user"
    }
}