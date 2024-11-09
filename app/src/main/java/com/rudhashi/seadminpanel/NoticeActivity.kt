package com.rudhashi.seadminpanel

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.rudhashi.seadminpanel.adaptor.NoticeAdapter
import com.rudhashi.seadminpanel.databinding.ActivityNoticeBinding
import com.rudhashi.seadminpanel.databinding.DialogAddNoticeBinding
import com.rudhashi.seadminpanel.model.Notice
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class NoticeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoticeBinding
    private lateinit var dialogView: View
    private var firestoreDB: FirebaseFirestore? = null
    private val noticeList = mutableListOf<Notice>()
    private var isALlFABVisible: Boolean? = null

    // RecyclerView Adapter
    private lateinit var adapter: NoticeAdapter // Replace with your RecyclerView adapter
    private var listenerRegistration: ListenerRegistration? = null


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestoreDB = FirebaseFirestore.getInstance()
        adapter = NoticeAdapter(noticeList, this)

        // Set up RecyclerView (assuming you have a RecyclerView with ID recyclerView)
        binding.apply {

            rvNotice.adapter = adapter
            rvNotice.layoutManager = LinearLayoutManager(this@NoticeActivity)

        }

        setFabButton()
    }

    private fun setFabButton() {
        binding.apply {
            val iconSize = 50

            addNoticeFab.setOnClickListener { showNoticeDialog(addNoticeText.text.toString())}
            addNewsFab.setOnClickListener {
                showNoticeDialog(addNewsText.text.toString())
                isALlFABVisible = false}
            addHollyDayFab.setOnClickListener {
                showNoticeDialog(addHollydayText.text.toString())
                isALlFABVisible = false}
            addDeadFab.setOnClickListener {
                showNoticeDialog(addDeadText.text.toString())
                isALlFABVisible = false
            }

            // FAB
            bgFAB.visibility = View.GONE
            addFab.setIconResource(R.drawable.menu_vertical_24)
            addFab.iconSize = iconSize
            addFab.visibility = View.VISIBLE
            addNoticeFab.visibility = View.GONE
            addNoticeText.visibility = View.GONE
            addNewsFab.visibility = View.GONE
            addNewsText.visibility = View.GONE
            addHollyDayFab.visibility = View.GONE
            addHollydayText.visibility = View.GONE
            addDeadFab.visibility = View.GONE
            addDeadText.visibility = View.GONE

            // Now make the boolean variable as false
            isALlFABVisible = false

            // at first Ex FAB is set ti shrink
            addFab.setOnClickListener {
                if (!isALlFABVisible!!) {
                    addFab.setIconResource(R.drawable.cross)
                    addFab.iconSize = iconSize
                    bgFAB.visibility = View.VISIBLE
                    addNoticeFab.show()
                    addNoticeText.visibility = View.VISIBLE
                    addNewsFab.show()
                    addNewsText.visibility = View.VISIBLE
                    addHollyDayFab.show()
                    addHollydayText.visibility = View.VISIBLE
                    addDeadFab.show()
                    addDeadText.visibility = View.VISIBLE

                    // now extend the Ex FAB
                    addFab.extend()
                    isALlFABVisible = true
                } else {
                    // Hide sub FAB' s
                    addFab.setIconResource(R.drawable.menu_vertical_24)
                    addFab.iconSize = iconSize
                    bgFAB.visibility = View.GONE
                    addNoticeFab.hide()
                    addNoticeText.visibility = View.GONE
                    addNewsFab.hide()
                    addNewsText.visibility = View.GONE
                    addHollyDayFab.hide()
                    addHollydayText.visibility = View.GONE
                    addDeadFab.hide()
                    addDeadText.visibility = View.GONE

                    // now extend the Ex FAB
                    addFab.extend()
                    isALlFABVisible = false
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged", "InflateParams")
    private fun showNoticeDialog(category: String) {
        // Inflate the dialog layout

        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
        dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_notice, null)
        val dialogBinding = DialogAddNoticeBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(false)
        builder.show()

        dialogBinding.apply {
            editTextNotice.requestFocus()
            val catName = getString(R.string.category_name, category)
            tvNoticeTitle.text = catName

            btnPost.setOnClickListener {
                val noticeText = editTextNotice.text.toString()
                val currentUserUid = "3yst2pi4gXhOx8JL8TgCm4i7pU13"

                if (noticeText.isNotEmpty() && currentUserUid != null) {
                    // Generate a unique ID for the notice
                    val noticeId = UUID.randomUUID().toString()

                    // Create the notice data
                    val noticeData = mapOf(
                        "id" to noticeId,
                        "category" to catName,
                        "notice" to noticeText,
                        "timestamp" to Timestamp.now(),
                        "likes" to 0,
                        "userLiked" to false // Initially, no likes
                    )

                    // Save the notice in Firestore under `polytechnic_notice` collection
                    firestoreDB?.collection("polytechnic_notice")
                        ?.document(noticeId)
                        ?.set(noticeData)
                        ?.addOnSuccessListener {
                            Toast.makeText(
                                this@NoticeActivity,
                                "Notice posted successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        ?.addOnFailureListener { e ->
                            Toast.makeText(
                                this@NoticeActivity,
                                "Failed to post notice: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    builder.dismiss()
                    isALlFABVisible = false
                } else {
                    Toast.makeText(
                        this@NoticeActivity,
                        "Notice cannot be empty",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            btnCancel.setOnClickListener { builder.dismiss() }
        }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }

    private fun saveToFirestore(userInput: String) {

        // Define the desired format
        val dateUploaded = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        val timeUploaded = SimpleDateFormat("h:mma", Locale.getDefault()).format(Date())

        // Add notice to FireStore
        firestoreDB?.collection("polytechnic_notice")?.add(
            mapOf(
                "date" to dateUploaded,
                "time" to timeUploaded,
                "notice" to userInput,
                "timestamp" to FieldValue.serverTimestamp() // Use server timestamp
            )
        )
    }

    override fun onStart() {
        super.onStart()
        setupFirestoreListener()
    }

    private fun setupFirestoreListener() {
        listenerRegistration = firestoreDB?.collection("polytechnic_notice")
            ?.orderBy(
                "timestamp",
                com.google.firebase.firestore.Query.Direction.DESCENDING
            ) // Order by timestamp in descending order
            ?.addSnapshotListener { snapshots, e ->
                if (e != null) {
                    // Handle the error
                    Toast.makeText(this, "Error fetching data: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                    return@addSnapshotListener
                }

                // Update the RecyclerView with the latest data
                if (snapshots != null) {
                    val updatedList = snapshots.documents.map { document ->
                        document.toObject(Notice::class.java)!! // Map each document to your data model
                    }
                    adapter.updateNoticeList(updatedList) // Update the adapter with the new list
                }
            }
    }

    override fun onStop() {
        super.onStop()
        listenerRegistration?.remove()
    }
}