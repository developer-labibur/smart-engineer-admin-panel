package com.rudhashi.seadminpanel.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.rudhashi.seadminpanel.databinding.ActivityAddNewPostBinding
import com.rudhashi.seadminpanel.model.Department
import com.rudhashi.seadminpanel.util.MakeFirebaseEasy
import com.rudhashi.seadminpanel.util.SmartData

class AddNewPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNewPostBinding

    private val selectedDepartments = mutableListOf<String>()
    private val selectedSemesters = mutableListOf<String>()

    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up department and semester selection dialogs
        setupDepartmentSelection(MakeFirebaseEasy.getAllDepartment(ArrayList()))
        setupSemesterSelection(MakeFirebaseEasy.getAllSemester(ArrayList()))

        // Post data to Firestore on button click
        binding.postButton.setOnClickListener {
            uploadBookData()
        }
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    private fun setupDepartmentSelection(department: ArrayList<String>) {
        val departments: List<String> = department

//        val departmentName = department
//        val departmentCode = departmentName.map { code ->
//            SmartData.covertDepNameToCode(code)
//        }

        binding.departmentSelectButton.setOnClickListener {
            MaterialDialog(this).show {
                title(text = "Select Departments")

                listItemsMultiChoice(items = departments) { _, _, items ->

                    val departmentCode = items.map {
                        SmartData.covertDepNameToCode(it.toString())
                    }

                    selectedDepartments.clear()
                    selectedDepartments.addAll(departmentCode)
                    //selectedDepartments.addAll(items.map { it.toString() })
                    binding.selectedDepartmentsText.text = "Selected: ${selectedDepartments.joinToString(", ")}"
                }
                positiveButton(text = "OK")
                negativeButton(text = "Cancel")
            }
        }
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    private fun setupSemesterSelection(sem: ArrayList<String>) {
        val semesters: List<String> = sem

        binding.semesterSelectButton.setOnClickListener {
            MaterialDialog(this).show {
                title(text = "Select Semesters")
                listItemsMultiChoice(items = semesters) { _, _, items ->
                    selectedSemesters.clear()
                    selectedSemesters.addAll(items.map { it.toString() })
                    binding.selectedSemestersText.text = "Selected: ${selectedSemesters.joinToString(", ")}"
                }
                positiveButton(text = "OK")
                negativeButton(text = "Cancel")
            }
        }
    }

    private fun uploadBookData() {
        val bookCode = binding.addBookCode.text.toString()
        val bookCover = binding.addBookCover.text.toString()
        val bookName = binding.addBookName.text.toString()
        val bookTotalU = binding.addBookTotalUnit.text.toString()

        // Validate input fields
        if (bookCode.isBlank() || bookCover.isBlank() || bookName.isBlank() || bookTotalU.isBlank()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare data to be uploaded
        val bookData = hashMapOf(
            "BCode" to bookCode,
            "TimeStamp" to Timestamp.now(),
            "BCover" to bookCover,
            "BName" to bookName,
            "BTotalU" to bookTotalU,
            "xDep" to selectedDepartments,
            "xSem" to selectedSemesters
        )

        // Upload data to Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("books").document(bookCode).set(bookData)
            .addOnSuccessListener {
                Toast.makeText(this, "Book added successfully", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add book: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
