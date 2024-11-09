package com.rudhashi.seadminpanel.view

import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
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
import com.rudhashi.seadminpanel.adaptor.FeaturesAdapter
import com.rudhashi.seadminpanel.databinding.ActivityDashboardBinding
import com.rudhashi.seadminpanel.databinding.DialogLogoutWarningBinding
import com.rudhashi.seadminpanel.model.Features
import com.rudhashi.seadminpanel.ui.AllBookActivity
import com.rudhashi.seadminpanel.ui.AllUserActivity
import com.rudhashi.seadminpanel.util.OnItemClickListener
import de.hdodenhof.circleimageview.CircleImageView

class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnItemClickListener {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private var db: FirebaseFirestore? = null
    private var tag = "DashboardActivity"

    private var tagLog = "ExploreFragment"
    private lateinit var featuresDataList: ArrayList<Features>

    // Variable for String Array ---------------------------------------------------------------------------------
    private lateinit var appFeatureId: Array<String>
    private lateinit var appFeatureTitle: Array<String>
    private lateinit var appFeatureImageUrl: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        Log.d(tag, "Enter Catch Block:")


        drawerLayout = binding.drawerLayout

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUser = Firebase.auth.currentUser!!


        // String Array Hooks ------------------------------------------------------------------------------------
        appFeatureId = resources.getStringArray(R.array.appFeatureId)
        appFeatureTitle = resources.getStringArray(R.array.appFeatureTitle)
        appFeatureImageUrl = resources.getStringArray(R.array.appFeatureImageUrl)

        // Gradient Text color Set -----------------------------------------------------------------------------------------
        binding.tvSmart.paint.shader = LinearGradient(0f,0f, binding.tvSmart.paint.measureText(binding.tvSmart.text.toString()), binding.tvSmart.textSize, intArrayOf(
            resources.getColor(R.color.clr1, null), resources.getColor(R.color.clr2, null)), null, Shader.TileMode.CLAMP)
        binding.tvE.paint.shader = LinearGradient(0f,0f, binding.tvE.paint.measureText(binding.tvE.text.toString()), binding.tvE.textSize, intArrayOf(
            resources.getColor(R.color.clr2_1, null), resources.getColor(R.color.clr1_1, null), resources.getColor(R.color.clr2_1, null)), null, Shader.TileMode.CLAMP)
        binding.tvEngineer.paint.shader = LinearGradient(0f,0f, binding.tvEngineer.paint.measureText(binding.tvEngineer.text.toString()), binding.tvEngineer.textSize, intArrayOf(
            resources.getColor(R.color.clr2_1_1, null), resources.getColor(R.color.clr2_1_2, null), resources.getColor(R.color.clr2_1_3, null) ),null, Shader.TileMode.CLAMP )


        // Adapter RecyclerView Category -----------------------------------------------------------------------------------
        binding.recycleFeatures.layoutManager = GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        binding.recycleFeatures.setHasFixedSize(true)
        binding.recycleFeatures.isNestedScrollingEnabled = false;
        featuresDataList = arrayListOf<Features>()
        getCategoryData()


        // Menu Button Click Event -------------------------------------------------------------------------------

        navigationDrawer()

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
                    Glide.with(this@DashboardActivity)
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


    // =====================================================================================================================
    // =====================================       Get Category Data                  ======================================
    // =====================================================================================================================
    private fun getCategoryData(){
        for (i in appFeatureImageUrl.indices){
            val categoryDataClass = Features(appFeatureId[i], appFeatureTitle[i], appFeatureImageUrl[i])
            featuresDataList.add(categoryDataClass)
        }
        binding.recycleFeatures.adapter = FeaturesAdapter(featuresDataList, this, this)
    }

    // ===============================================================================
    // ==================        Navigation Drawer Function        ===================
    // ===============================================================================
    private fun navigationDrawer() {
        // Navigation Drawer

        binding.navigationView.bringToFront()
        binding.navigationView.setNavigationItemSelectedListener(this@DashboardActivity)

        //binding.navigationView.setCheckedItem(R.id.nav_home);
        binding.icMenuBtn.setOnClickListener{
            if (binding.drawerLayout.isDrawerVisible(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean { return true }

    override fun onItemClick(position: Int) {
        when (position) {
            0 -> startActivity(Intent(this@DashboardActivity, AllUserActivity::class.java))
            1 -> startActivity(Intent(this@DashboardActivity, EditorActivity::class.java))
            2 -> startActivity(Intent(this@DashboardActivity, AllBookActivity::class.java))
            else -> startActivity(Intent(this@DashboardActivity, MainActivity2::class.java))
        }


    }

}