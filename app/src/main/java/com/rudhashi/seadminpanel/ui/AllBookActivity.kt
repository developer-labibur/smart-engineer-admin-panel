package com.rudhashi.seadminpanel.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rudhashi.seadminpanel.adaptor.BookAdapter
import com.rudhashi.seadminpanel.databinding.ActivityAllBookBinding
import com.rudhashi.seadminpanel.model.Book
import com.rudhashi.seadminpanel.view.DashboardActivity

class AllBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllBookBinding
    private lateinit var bookAdapter: BookAdapter
    private val db = FirebaseFirestore.getInstance()
    private val booksCollection = db.collection("books")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllBookBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)


        bookAdapter = BookAdapter { bookCode ->
            val intent = Intent(this, BookDetailsActivity::class.java)
            intent.putExtra("BOOK_CODE", bookCode)
            startActivity(intent)
        }

        fetchBooks()

        binding.apply {
            rvAllBook.layoutManager = LinearLayoutManager(this@AllBookActivity)
            rvAllBook.adapter = bookAdapter
            addNewBookButton.setOnClickListener {
                startActivity(Intent(this@AllBookActivity, AddNewPostActivity::class.java))
            }
            btnBack.setOnClickListener {
                startActivity(Intent(this@AllBookActivity, DashboardActivity::class.java))
                finish()
            }
            btnRefresh.setOnClickListener {
                isShimmerOn(true)
                fetchBooks()
            }
        }
    }

    private fun fetchBooks() {
        booksCollection.orderBy(
            "TimeStamp",
            Query.Direction.DESCENDING
        ) // Or DESCENDING for reverse order
            .get().addOnSuccessListener { querySnapshot ->
                isShimmerOn(false)
                val books = querySnapshot.documents.mapNotNull { it.toObject(Book::class.java) }
                bookAdapter.submitList(books)
            }.addOnFailureListener { e ->
                isShimmerOn(false)
                Toast.makeText(this, "Failed to fetch books: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    override fun onResume() {
        super.onResume()
        // If data is available then Shimmer Effect will be gone
        isShimmerOn(true)
        fetchBooks()
    }

    override fun onPause() {
        // Shimmer effect will be stopped when the activity is paused to save battery life.
        // If data is available then Shimmer Effect will be gone
        isShimmerOn(false)
        super.onPause()
    }

    fun isShimmerOn(flag: Boolean) {
        if (flag) {
            // If data is available then Shimmer Effect will be gone
            binding.shimmerBookList.startShimmer()
            binding.shimmerBookList.visibility = View.VISIBLE
            binding.rvAllBook.visibility = View.GONE
        } else {
            binding.shimmerBookList.stopShimmer()
            binding.shimmerBookList.visibility = View.GONE
            binding.rvAllBook.visibility = View.VISIBLE
        }
    }
}