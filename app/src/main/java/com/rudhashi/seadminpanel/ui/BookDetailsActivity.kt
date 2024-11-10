package com.rudhashi.seadminpanel.ui

import android.content.Intent
import android.graphics.fonts.Font
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.rudhashi.seadminpanel.adaptor.UnitAdapter
import com.rudhashi.seadminpanel.databinding.ActivityBookDetailsBinding
import com.rudhashi.seadminpanel.model.Book
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.firestore.QuerySnapshot
import com.rudhashi.seadminpanel.R
import com.rudhashi.seadminpanel.model.BookUnit
import com.rudhashi.seadminpanel.util.SmartData
import com.rudhashi.seadminpanel.view.MainActivity2

class BookDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookDetailsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var unitAdapter: UnitAdapter
    // Update type for the unit list and adapter
    private val unitList = mutableListOf<BookUnit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize RecyclerView
        setupRecyclerView()

        // Get book ID from intent and load units
        val bookCode = intent.getStringExtra("BOOK_CODE")
        if (bookCode != null) {
            loadUnits(bookCode)
            // Fetch the book details including xDep and xSem
            fetchBookDetails(bookCode)
            // Call this function to handle the view count
            updateBookViewCount(bookCode)
        } else {
            Log.e("BookDetailsActivity", "Book Code is null")
        }
    }

    private fun setupRecyclerView() {
        // Set up the RecyclerView with UnitAdapter
        unitAdapter = UnitAdapter(this, unitList)
        binding.unitRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@BookDetailsActivity)
            adapter = unitAdapter
        }
    }

    private fun loadUnits(bookCode: String) {
        db.collection("books").document(bookCode)
            .get()
            .addOnSuccessListener { snapshot ->
                val book = snapshot.toObject(Book::class.java)
                binding.apply {
                    Glide.with(this@BookDetailsActivity)
                        .load(book?.BCover)
                        .into(bookCoverImage)
                    tvBookTitle.text = book?.BName
                    bookCodes.text = book?.BCode
                    bookTotalUnits.text = book?.BTotalU
                }
            }
            .addOnFailureListener { exception ->
                Log.e("BookDetailsActivity", "Error loading units: ", exception)
            }
        db.collection("books").document(bookCode).collection("BUnit")
            .get()
            .addOnSuccessListener { documents ->
                parseUnitData(documents)
            }
            .addOnFailureListener { exception ->
                Log.e("BookDetailsActivity", "Error loading units: ", exception)
            }
    }
    // Parsing Firestore data and adding it to the list
    private fun parseUnitData(documents: QuerySnapshot) {
        unitList.clear() // Clear the list to avoid duplicates
        for (document in documents) {
            val unit = document.toObject(BookUnit::class.java)
            unitList.add(unit)
        }
        unitAdapter.notifyDataSetChanged() // Refresh adapter to display new data
    }

    private fun fetchBookDetails(bookCode: String) {
        db.collection("books").document(bookCode).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val department = document["xDep"] as? List<String> ?: listOf()
                    val semester = document["xSem"] as? List<String> ?: listOf()

                    val book = document.toObject(Book::class.java)
                    if (book != null) {
                        // Now, xDep is an array of Strings
                        val departmentCodes = book.xDep
                        val departmentNames = departmentCodes.map {
                            SmartData.covertDepCodeToName(it)
                        }

                        displayDepartmentTags(departmentNames)
                        displaySemesterTags(semester)


/*                        // Convert department codes to department names
//                        val departmentNames = book.xDep.joinToString(", ") {
//                            SmartData.getDepartmentName(it)
//                        } ?: "N/A"
//                        val semesterNames = book.xSem.joinToString(", ") ?: "N/A"

                        // Display department and semester names in TextViews
                        //binding.textViewXDep.text = "Departments: $departmentNames"
                        //binding.textViewXSem.text = "Semesters: $semesterNames"*/
                    }
                } else {
                    Toast.makeText(this, "Book not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch book details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayDepartmentTags(departmentNames: List<String>) {
        binding.tagContainerXDep.removeAllViews() // Clear any existing views

        // Loop through the department names and create TextViews
        departmentNames.forEach { departmentName ->
            val textView = TextView(this@BookDetailsActivity).apply {
                text = departmentName
                setBackgroundResource(R.drawable.tag_background)  // Set custom background
                setPadding(16, 8, 16, 5)  // Set padding (left, top, right, bottom)
                typeface = ResourcesCompat.getFont(context, R.font.main_font_regular_2)
                setTextColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.white)) // Set text color

                // Add OnClickListener to start new activity
                setOnClickListener {
                    val intent = Intent(this@BookDetailsActivity, MainActivity2::class.java)
                    intent.putExtra("DEPARTMENT_NAME", departmentName)
                    startActivity(intent)
                }
            }

            // Create LayoutParams with margins (left, top, right, bottom)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,  // Width
                LinearLayout.LayoutParams.WRAP_CONTENT   // Height
            ).apply {
                // Add margins to the TextView
                setMargins(
                    SmartData.dpToPx(this@BookDetailsActivity, 3f),
                    SmartData.dpToPx(this@BookDetailsActivity, 5f),
                    SmartData.dpToPx(this@BookDetailsActivity, 3f),
                    SmartData.dpToPx(this@BookDetailsActivity, 5f)
                )  // (left, top, right, bottom) margins in pixels
            }

            // Set LayoutParams to the TextView
            textView.layoutParams = params

            // Add the TextView to your layout (assuming `linearLayout` is your container)
            binding.tagContainerXDep.addView(textView)
        }
//
//        for (deptName in departmentNames) {
//            val tagView = createTagView(deptName)
//            binding.tagContainerXDep.addView(tagView)
//        }
    }
    private fun displaySemesterTags(semesterNames: List<String>) {
        binding.tagContainerXSem.removeAllViews() // Clear any existing views

        // Loop through the department names and create TextViews
        semesterNames.forEach { semesterNames ->
            val textView = TextView(this@BookDetailsActivity).apply {
                text = semesterNames
                setBackgroundResource(R.drawable.tag_background)  // Set custom background
                setPadding(16, 8, 16, 5)  // Set padding (left, top, right, bottom)
                typeface = ResourcesCompat.getFont(context, R.font.main_font_regular_2)
                setTextColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.white)) // Set text color

                // Add OnClickListener to start new activity
                setOnClickListener {
                    val intent = Intent(this@BookDetailsActivity, MainActivity2::class.java)
                    intent.putExtra("SEMESTER_NAME", semesterNames)
                    startActivity(intent)
                }
            }

            // Create LayoutParams with margins (left, top, right, bottom)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,  // Width
                LinearLayout.LayoutParams.WRAP_CONTENT   // Height
            ).apply {
                // Add margins to the TextView
                setMargins(
                    SmartData.dpToPx(this@BookDetailsActivity, 3f),
                    SmartData.dpToPx(this@BookDetailsActivity, 5f),
                    SmartData.dpToPx(this@BookDetailsActivity, 3f),
                    SmartData.dpToPx(this@BookDetailsActivity, 5f)
                )  // (left, top, right, bottom) margins in pixels
            }

            // Set LayoutParams to the TextView
            textView.layoutParams = params

            // Add the TextView to your layout (assuming `linearLayout` is your container)
            binding.tagContainerXSem.addView(textView)
        }

    }

    private fun createTagView(departmentName: String): TextView {
        val textView = TextView(this)
        textView.apply {
            text = departmentName
            setBackgroundResource(R.drawable.tag_background) // Create drawable for tag appearance
            setPadding(16, 8, 16, 8)  // Set padding (left, top, right, bottom)
            setTextColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.white))
            typeface = ResourcesCompat.getFont(context, R.font.main_font_regular_2)
            // Add OnClickListener to start new activity
            setOnClickListener {
                // Start new activity with department name as extra
                val intent = Intent(this@BookDetailsActivity, MainActivity2::class.java)
                intent.putExtra("DEPARTMENT_NAME", departmentName)
                startActivity(intent)
            }
        }
        // Create LayoutParams with margins (left, top, right, bottom)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,  // Width
            LinearLayout.LayoutParams.WRAP_CONTENT   // Height
        ).apply {
            // Add margins to the TextView
            setMargins(16, 8, 16, 8)  // (left, top, right, bottom) margins in pixels
        }

        // Set LayoutParams to the TextView
        textView.layoutParams = params

        // Add the TextView to your layout (assuming `linearLayout` is your container)
        binding.tagContainerXDep.addView(textView)

        return textView
    }

    private fun updateBookViewCount(bookCode: String) {
        // Reference to the specific book document in Firestore
        val bookRef = db.collection("books").document(bookCode)

        // Fetch current BView count
        bookRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Get the current view count, default to 0 if it doesn't exist
                val currentViewCount = document.getLong("BView") ?: 0

                // Increment the view count
                val newViewCount = currentViewCount + 1

                // Update the BView field in Firestore
                bookRef.update("BView", newViewCount)
                    .addOnSuccessListener {
                        // Optionally, display the new view count in the UI
                        binding.viewCountTextView.text = "Views: $newViewCount"
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update view count: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error fetching book data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}