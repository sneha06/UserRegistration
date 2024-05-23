package com.example.userregistration

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    lateinit var textInputName: TextInputEditText
    lateinit var textInputEmail: TextInputEditText
    lateinit var textInputPswd: TextInputEditText
    lateinit var textInputCPswd: TextInputEditText
    lateinit var emailinputlayout: TextInputLayout
    lateinit var phoneNumberinputlayout: TextInputEditText
    lateinit var registerButton: Button
    lateinit var getData: Button
    lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        textInputName = findViewById<TextInputEditText>(R.id.name)
        textInputEmail = findViewById<TextInputEditText>(R.id.email)
        textInputPswd = findViewById<TextInputEditText>(R.id.pswd)
        textInputCPswd = findViewById<TextInputEditText>(R.id.cpswd)
        emailinputlayout = findViewById<TextInputLayout>(R.id.emailinputlayout)
        phoneNumberinputlayout = findViewById<TextInputEditText>(R.id.phoneNumber)
        registerButton = findViewById<Button>(R.id.register)
        getData = findViewById<Button>(R.id.getData)
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        val phoneRegext = "[6-9][0-9]{9}"

        registerButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val name = textInputName.text
                val email = textInputEmail.text
                val pswd = textInputPswd.text
                val cPswd = textInputCPswd.text
                val pNum = phoneNumberinputlayout.text

                if(name.toString().isEmpty() && pNum.toString().isEmpty() && email.toString().isEmpty() && pswd.toString().isEmpty() && cPswd.toString().isEmpty()){
                    Toast.makeText(
                        applicationContext,
                        "Please enter all the details",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                if (!(pNum!!.matches(phoneRegext.toRegex()))) {
                    Toast.makeText(
                        applicationContext,
                        "Please enter valid phone number",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }
                if (!(email!!.matches(emailRegex.toRegex()))) {
                    Toast.makeText(
                        applicationContext,
                        "Please enter valid Email Address",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }
                if (!(pswd.toString()!!.equals(cPswd.toString()))) {
                    Toast.makeText(
                        applicationContext, "Confirmed password are not same as password. " +
                                "Please re-enter the password", Toast.LENGTH_LONG
                    ).show()
                    return
                }

                if ((email!!.matches(emailRegex.toRegex())) && (pswd.toString()!!
                        .equals(cPswd.toString()) && (pNum!!.matches(phoneRegext.toRegex())))
                ) {
                    mAuth.createUserWithEmailAndPassword(email.toString(), pswd.toString())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val db = Firebase.firestore

                                val user = hashMapOf(
                                    "uid" to mAuth.currentUser?.uid,
                                    "name" to name.toString(),
                                    "email" to email.toString(),
                                    "phone number" to pNum.toString()
                                )
                                db.collection("users")
                                    .add(user)
                                    .addOnSuccessListener { documentReference ->
                                        Log.d(
                                            "Saved data to firestore",
                                            "DocumentSnapshot added with ID: ${documentReference.id}"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("MainActivity.kt", "Error adding document", e)
                                    }

                                Toast.makeText(
                                    applicationContext,
                                    "User Registered successfully with " + mAuth.currentUser?.email,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }.addOnFailureListener { exception ->
                            Toast.makeText(
                                applicationContext,
                                exception.localizedMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }

        })

        getData.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val db = FirebaseFirestore.getInstance()
                var isUser: Boolean = false
                println(" Current User  ${mAuth.currentUser?.uid}")

                db.collection("users")
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            Log.d(" Get Data", "${document.id} => ${document.data}")

                            println(" Current User 2  ${document.data.get("uid")}")
                            if (mAuth.currentUser?.uid == document.data.get("uid")) {
                                isUser = true
                                val builder = AlertDialog.Builder(
                                    this@MainActivity
                                )
                                builder.setTitle("User Information")
                                builder.setMessage(
                                    "Name : ${document.data.get("name")},  Email:${
                                        document.data.get(
                                            "email"
                                        )
                                    },  " +
                                            "Phone Number:${document.data.get("phone number")}, " +
                                            "Uid:${document.data.get("uid")} "
                                )

                                builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                                }
                                builder.show()
                            }
                        }
                        if (!isUser) {
                            Toast.makeText(
                                this@MainActivity,
                                "Please Register the User",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("Get Data", "Error getting documents.", exception)
                    }

            }

        })


    }

}