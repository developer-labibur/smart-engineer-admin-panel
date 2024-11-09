package com.rudhashi.seadminpanel.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.rudhashi.seadminpanel.R
import com.rudhashi.seadminpanel.adaptor.BookAdapter
import com.rudhashi.seadminpanel.databinding.ActivityAllBookBinding
import com.rudhashi.seadminpanel.model.Book
import com.rudhashi.seadminpanel.model.User

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

        binding.rvAllBook.layoutManager = LinearLayoutManager(this)

        bookAdapter = BookAdapter { bookCode ->
            val intent = Intent(this, BookDetailsActivity::class.java)
            intent.putExtra("BOOK_CODE", bookCode)
            startActivity(intent)
        }

        binding.rvAllBook.adapter = bookAdapter

        fetchBooks()

        binding.addNewBookButton.setOnClickListener {
            startActivity(Intent(this, AddNewPostActivity::class.java))
        }
    }

    private fun fetchBooks() {

        booksCollection.get().addOnSuccessListener { querySnapshot ->
            val books = querySnapshot.documents.mapNotNull { it.toObject(Book::class.java) }
            bookAdapter.submitList(books)
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to fetch books: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}