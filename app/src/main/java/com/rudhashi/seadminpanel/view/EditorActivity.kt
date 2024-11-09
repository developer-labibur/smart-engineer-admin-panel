package com.rudhashi.seadminpanel.view

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.rudhashi.seadminpanel.R
import com.rudhashi.seadminpanel.Users
import com.rudhashi.seadminpanel.databinding.ActivityEditorBinding
import com.rudhashi.seadminpanel.databinding.DialogLogoutWarningBinding
import com.rudhashi.seadminpanel.util.GoogleAuthClient
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private var db: FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        drawerLayout = binding.drawerLayout

        db = FirebaseFirestore.getInstance()
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUser = Firebase.auth.currentUser!!

        val polytechnicNotice = db!!.collection("polytechnic_notice")
        // Get current date and time
        val currentDateTime = Date()

        // Define the desired format
        val dateUploaded = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val timeUploaded = SimpleDateFormat("h:mma", Locale.getDefault())

        // Format the date and time and store it in a variable
        val formattedDate = dateUploaded.format(currentDateTime)
        val formattedTime = timeUploaded.format(currentDateTime)

        // Get notice from EditText


        //Departments ---------------------------------------------------------------------------------------
//        db!!.collection("departments") //.orderBy("DCode", Query.Direction.ASCENDING)
//            .orderBy(FieldPath.documentId())
//            .get()
//            .addOnSuccessListener { queryDocumentSnapshots ->
//                val deplist = queryDocumentSnapshots.documents
//                for (depSnapshot in deplist) {
//                    val dep: DepartmentsModel =
//                        depSnapshot.toObject<T>(DepartmentsModel::class.java)
//                    depArrayList.add(dep)
//                    //							}
//                }
//                // ===== Update Adapter =====
//                departmentAdapter.notifyDataSetChanged()
//            }


        // Set up ActionBarDrawerToggle
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolbar,
            R.string.drawer_open, R.string.drawer_close
        )

        // Sync drawer toggle state
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        // Handle navigation item clicks
        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navLibrary -> {
                    // Handle home action
                }

                R.id.navRecent -> {
                    // Handle settings action
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


        // Navigation Bar Profile ----------------------------------------------------------------------------------------------------
        val navHeader = binding.navigationView.getHeaderView(0)
        val headerName = navHeader.findViewById<TextView>(R.id.headerName)
        val headerEmail = navHeader.findViewById<TextView>(R.id.headerEmail)
        val headerDepartment = navHeader.findViewById<TextView>(R.id.headerDepartment)
        val headerRole = navHeader.findViewById<TextView>(R.id.headerRole)
        val headerPic = navHeader.findViewById<CircleImageView>(R.id.headerPic)
        val headerProfile = navHeader.findViewById<ConstraintLayout>(R.id.headerProfile)
        val headerLogout = navHeader.findViewById<ConstraintLayout>(R.id.headerLogout)

        headerName.text = currentUser.displayName
        headerEmail.text = currentUser.email

        database.reference.child("Users")
            .child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user: Users? = snapshot.getValue(Users::class.java)
                    Glide.with(this@EditorActivity)
                        .load(currentUser.photoUrl)
                        .placeholder(R.drawable.avatar)
                        .into(headerPic)
                    headerRole.text = user?.role
                    //headerSemester.setText(user.getSemester())
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        headerProfile.setOnClickListener {
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }

        headerLogout.setOnClickListener {
            val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_logout_warning, null)
            val dialogBinding: DialogLogoutWarningBinding =
                DialogLogoutWarningBinding.bind(dialogView)
            builder.setView(dialogBinding.root)
            builder.setCanceledOnTouchOutside(false)
            builder.show()

            dialogBinding.btnYesLogOut.setOnClickListener {
                auth.signOut()
                startActivity(Intent(this, MainActivity::class.java))
                builder.dismiss()
                finish()
            }
            dialogBinding.btnNoLogOut.setOnClickListener { builder.dismiss() }
            dialogBinding.btnCloseLogOut.setOnClickListener { builder.dismiss() }

            if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
            builder.show()
        }
    }

    // Handle toolbar menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notification -> {
                // Handle notification click
                true
            }

            R.id.action_profile -> {
                // Handle profile click
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}