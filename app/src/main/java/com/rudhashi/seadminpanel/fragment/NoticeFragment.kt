package com.rudhashi.seadminpanel.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rudhashi.seadminpanel.R
import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.rudhashi.seadminpanel.adaptor.NoticeAdapter
import com.rudhashi.seadminpanel.databinding.ActivityNoticeBinding
import com.rudhashi.seadminpanel.databinding.DialogAddNoticeBinding
import com.rudhashi.seadminpanel.databinding.FragmentNoticeBinding
import com.rudhashi.seadminpanel.model.Notice
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class NoticeFragment : Fragment() {

    private lateinit var binding: FragmentNoticeBinding
    private lateinit var dialogView: View
    private var firestoreDB: FirebaseFirestore? = null
    private val noticeList = mutableListOf<Notice>()
    private var isALlFABVisible: Boolean? = null
    private lateinit var auth: FirebaseAuth

    // RecyclerView Adapter
    private lateinit var adapter: NoticeAdapter // Replace with your RecyclerView adapter
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNoticeBinding.inflate(inflater, container,false)

        firestoreDB = FirebaseFirestore.getInstance()
        adapter = NoticeAdapter(noticeList, requireActivity())
        auth = FirebaseAuth.getInstance()

        // Set up RecyclerView (assuming you have a RecyclerView with ID recyclerView)
        binding.apply {

            rvNotice.adapter = adapter
            rvNotice.layoutManager = LinearLayoutManager(requireActivity())

        }

        setFabButton()

        return binding.root
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

        val builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).create()
        dialogView = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_add_notice, null)
        val dialogBinding = DialogAddNoticeBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(false)
        builder.show()

        dialogBinding.apply {
            editTextNotice.requestFocus()
            val catName = if (category == "শোক সংবাদ"){
                getString(R.string.category_dead_name, category)
            } else {
                getString(R.string.category_name, category)
            }
            tvNoticeTitle.text = catName

            btnPost.setOnClickListener {
                val noticeText = editTextNotice.text.toString()
                val currentUserUid = auth.currentUser?.uid

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
                                requireActivity(),
                                "Notice posted successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        ?.addOnFailureListener { e ->
                            Toast.makeText(
                                requireActivity(),
                                "Failed to post notice: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    builder.dismiss()
                    isALlFABVisible = false
                } else {
                    Toast.makeText(
                        requireActivity(),
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
                    Toast.makeText(requireActivity(), "Error fetching data: ${e.message}", Toast.LENGTH_SHORT)
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