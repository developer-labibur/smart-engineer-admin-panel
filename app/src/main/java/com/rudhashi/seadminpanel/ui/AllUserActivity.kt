package com.rudhashi.seadminpanel.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rudhashi.seadminpanel.R
import com.rudhashi.seadminpanel.Users
import com.rudhashi.seadminpanel.adaptor.AllUserAdapter
import com.rudhashi.seadminpanel.databinding.ActivityAllUserBinding
import com.rudhashi.seadminpanel.databinding.DialogEditUserBinding
import com.rudhashi.seadminpanel.databinding.DialogLogoutWarningBinding
import com.rudhashi.seadminpanel.model.User
import com.rudhashi.seadminpanel.view.MainActivity

class AllUserActivity : AppCompatActivity(), AllUserAdapter.OnUserClickListener {

    private lateinit var binding: ActivityAllUserBinding
    private lateinit var allUserAdapter: AllUserAdapter
    private lateinit var userList: ArrayList<User>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllUserBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.rvAllUser.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()

        // Fetch data from Firebase
        FirebaseDatabase.getInstance().getReference("Users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.clear()
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        if (user != null) {
                            userList.add(user)
                        }
                    }
                    allUserAdapter = AllUserAdapter(this@AllUserActivity, userList, this@AllUserActivity)
                    binding.rvAllUser.adapter = allUserAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })

        binding.btnBack.setOnClickListener { finish() }
    }

    // Implement OnUserClickListener methods
    override fun onDeleteClicked(userId: String) {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_logout_warning, null)
        val dialogBinding: DialogLogoutWarningBinding =
            DialogLogoutWarningBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(false)
        builder.show()

        dialogBinding.dialogTitle.text = getString(R.string.are_you_sure_you_want_to_delete_this_user)

        dialogBinding.btnYesLogOut.setOnClickListener {
            FirebaseDatabase.getInstance().getReference("Users").child(userId).removeValue()
            Toast.makeText(this@AllUserActivity, "User deleted successfully", Toast.LENGTH_SHORT).show()
            builder.dismiss()
        }
        dialogBinding.btnNoLogOut.setOnClickListener { builder.dismiss() }
        dialogBinding.btnCloseLogOut.setOnClickListener { builder.dismiss() }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()

    }

    override fun onEditClicked(user: User) {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_user, null)
        val dialogBinding: DialogEditUserBinding =
            DialogEditUserBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(false)
        builder.show()

        dialogBinding.etUserInfoUpdate.setText(user.name)
        dialogBinding.apply {
            cancelButton.setOnClickListener { builder.dismiss() }
            closeButton.setOnClickListener { builder.dismiss() }
            btnMakeAdmin.setOnClickListener {
                val role = "Admin"
                FirebaseDatabase.getInstance().getReference("Users").child(user.userId).child("role")
                    .setValue(role)
                    .addOnCompleteListener {
                        Toast.makeText(this@AllUserActivity, "Role updated to $role", Toast.LENGTH_SHORT).show()
                        tvRole.text = role
                    }
            }

            btnMakeUser.setOnClickListener {
                val role = "User"
                FirebaseDatabase.getInstance().getReference("Users").child(user.userId).child("role")
                    .setValue(role)
                    .addOnCompleteListener {
                        Toast.makeText(this@AllUserActivity, "Role updated to $role", Toast.LENGTH_SHORT).show()
                        tvRole.text = role
                    }
            }

            updateButton.setOnClickListener {
                val updatedName = etUserInfoUpdate.text.toString()
                FirebaseDatabase.getInstance().getReference("Users").child(user.userId).child("name")
                    .setValue(updatedName)
                    .addOnCompleteListener {
                        Toast.makeText(this@AllUserActivity, "User updated successfully", Toast.LENGTH_SHORT).show()
                    }
                builder.dismiss()
            }

        }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }
}