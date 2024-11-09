package com.rudhashi.seadminpanel.util

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.rudhashi.seadminpanel.Users
import com.rudhashi.seadminpanel.view.DashboardActivity
import com.rudhashi.seadminpanel.view.MainActivity

class SmartData {
    companion object {
        // Dark Mode Theme
        const val PREF_KEY = "theme"
        const val SWITCH_BUTTON_KEY = "isNightMode"

        // Splash Screen
        const val SPLASH_SCREEN: Long = 2000

        // Google SignIn
        const val WEB_CLIENT_ID = "389818419543-sdlmnl0vdps19ka0ku0n76f4vub730cr.apps.googleusercontent.com"

        // # milliseconds, desired
        const val TIME_INTERVAL: Int = 2000

        // Function to display Toast
        fun lToast(context: Context, message: String, isShort: Boolean) {
            if (isShort) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }

        fun checkAdministrator(user: Users?) : Boolean{
            val role = user?.role.toString()
            // Check if the user is an admin
            // Here you can add your logic to check if the user is an admin
            return if (role == "Main Admin" || role == "Admin") {
                Log.i("TAG", "You are an admin")
                true
            } else {
                Log.i("TAG", "You are a user")
                false
            }
        }

        fun getDepartmentName(depCode: String): String {
            return when (depCode) {
                "61" -> "Architecture Technology"
                "64" -> "Civil Technology"
                "85" -> "Computer Science and Technology"
                "67" -> "Electrical Technology"
                "68" -> "Electronics Technology"
                "70" -> "Mechanical Technology"
                "88" -> "Construction Technology"
                "71" -> "Power Technology"
                else -> "Unknown Department"
            }
        }
    }
}