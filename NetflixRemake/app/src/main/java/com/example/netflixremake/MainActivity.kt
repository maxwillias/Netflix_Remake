package com.example.netflixremake

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.netflixremake.model.Category
import com.example.netflixremake.model.Movie
import com.example.netflixremake.util.MovieTask

class MainActivity : AppCompatActivity(), MovieTask.Callback {

    private lateinit var progress: ProgressBar
    private lateinit var adapter: CategoryAdapter
    private val categories = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progress = findViewById(R.id.progressMain)

        adapter = CategoryAdapter(categories){ id ->
            val intent = Intent(this@MainActivity, MovieActivity::class.java)
            intent.putExtra("id", id)
            startActivity(intent)
        }

        val rv:RecyclerView = findViewById(R.id.rvMain)
        rv.layoutManager =LinearLayoutManager(this)
        rv.adapter = adapter

        val typeCategories = mutableListOf<String>()
        typeCategories.add("Sherlock")
        typeCategories.add("Superman")
        typeCategories.add("Sonic")
        typeCategories.add("Harry Potter")
        typeCategories.add("João")

        for(i in 0 until typeCategories.size){
            MovieTask(this, typeCategories.get(i)).execute("https://www.omdbapi.com/?s=${typeCategories.get(i)}&page=&apikey=517538ae")
        }
    }

    override fun onPreExecute() {
        progress.visibility = View.VISIBLE
    }

    override fun onResult(movies: List<Movie>, name: String) {
        this.categories.add(Category(name, movies))
        adapter.notifyDataSetChanged()
        progress.visibility = View.GONE
    }

    override fun onFailure(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        progress.visibility = View.GONE
    }
}