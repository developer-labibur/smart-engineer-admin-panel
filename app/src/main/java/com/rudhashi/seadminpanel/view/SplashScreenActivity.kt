package com.rudhashi.seadminpanel.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rudhashi.seadminpanel.R
import com.rudhashi.seadminpanel.Users
import com.rudhashi.seadminpanel.databinding.ActivitySplashScreenBinding
import com.rudhashi.seadminpanel.util.SmartData

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var sharedPreferences: SharedPreferences
    private var isNightMode: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize the shared preferences
        sharedPreferences = getSharedPreferences(SmartData.PREF_KEY, MODE_PRIVATE)

        // Get the current theme mode
        isNightMode = sharedPreferences.getBoolean(SmartData.SWITCH_BUTTON_KEY, false)
        if (isNightMode as Boolean) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        val upFromBottom = AnimationUtils.loadAnimation(this, R.anim.animation1)

        binding.imgLogo.animation = upFromBottom

        // Delay the splash screen for 3 seconds, then move to AllUserActivity
        Handler(Looper.getMainLooper()).postDelayed({
            // Check if user is already logged in and move to AllUserActivity if true, otherwise move to VerificationActivity
            if (auth.currentUser != null) {
                // Start the Dashboard activity
                database.reference.child("Users")
                    .child(auth.currentUser!!.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val user: Users? = snapshot.getValue(Users::class.java)
                            val role = user?.role.toString()
                            try {
                                when (role) {
                                    "Main Admin" -> {
                                        startActivity(Intent(this@SplashScreenActivity, DashboardActivity::class.java))
                                        finish()
                                    }
                                    "Admin" -> {
                                        startActivity(Intent(this@SplashScreenActivity, DashboardActivity::class.java))
                                        finish()
                                    }
                                    else -> {
                                        startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                                        finish()
                                        auth.signOut()
                                    }
                                }
                            } catch (e: Exception){
                                auth.signOut()
                                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                                finish()
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            auth.signOut()
                            startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                            finish()
                        }
                    })
        } else {
            // New User => Start the Main activity
            startActivity(Intent(this, MainActivity::class.java))
                auth.signOut()
        }
        // Close this activity
        finish()
    }, SmartData.SPLASH_SCREEN) // 2 seconds delay

}
}