package com.rudhashi.seadminpanel.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rudhashi.seadminpanel.Users
import com.rudhashi.seadminpanel.databinding.ActivityMainBinding
import com.rudhashi.seadminpanel.util.AuthRepository
import com.rudhashi.seadminpanel.util.GoogleAuthClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var googleAuthClient: GoogleAuthClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Initialize Firebase Auth and Firebase Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        googleAuthClient = GoogleAuthClient(this)

        binding.apply {

            googleCntBtn.setOnClickListener {
                lifecycleScope.launch {
                    val isSignIn = googleAuthClient.signIn(false)
                    if (isSignIn) {
                        val id = auth.currentUser?.uid
                        val name = auth.currentUser?.displayName
                        val email = auth.currentUser?.email
                        val image = auth.currentUser?.photoUrl.toString()


                        id?.let { it1 ->
                            database.reference.child("Users")
                                .child(it1)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val user: Users? = snapshot.getValue(Users::class.java)
                                        val role = user?.role.toString()
                                        when (role) {
                                            "Main Admin" -> {
                                                val isImageNull = if (user?.picture!!.isNotEmpty()) user.picture else image
                                                val userInfo = Users(id, name, isImageNull, email, role = "Main Admin")
                                                database.reference.child("Users").child(id).setValue(userInfo)

                                                startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
                                                finish()
                                                Toast.makeText(this@MainActivity, "Success, You are an Main Admin", Toast.LENGTH_LONG).show()
                                            }
                                            "Admin" -> {
                                                val isImageNull = if (user?.picture!!.isNotEmpty()) user.picture else image
                                                val userInfo = Users(id, name, isImageNull, email, role = "Admin")
                                                database.reference.child("Users").child(id).setValue(userInfo)

                                                startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
                                                finish()
                                                Toast.makeText(this@MainActivity, "Success, You are a Admin.", Toast.LENGTH_LONG).show()
                                            }
                                            else -> {
                                                try {
                                                    val isImageNull = if (user?.picture!!.isNotEmpty()) user.picture else image
                                                    val userInfo = Users(id, name, isImageNull, email, role = "User")
                                                    database.reference.child("Users").child(id).setValue(userInfo)
                                                    auth.signOut()
                                                    Toast.makeText(this@MainActivity, "Sorry, You can't access the Admin Panel", Toast.LENGTH_LONG).show()
                                                } catch (e: IllegalStateException) {
                                                    Log.d("TAG", "onDataChange: ${e.message}")
                                                    auth.signOut()
                                                    Toast.makeText(this@MainActivity, "Something went wrong...", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@MainActivity, "Something went wrong...", Toast.LENGTH_LONG).show()
                                    }
                                })
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "SignIn Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
    }
}