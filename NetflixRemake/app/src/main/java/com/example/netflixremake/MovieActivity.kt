package com.example.netflixremake

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.netflixremake.model.Movie
import com.example.netflixremake.model.MovieDetail
import com.example.netflixremake.util.DownloadImageTask
import com.example.netflixremake.util.MovieDetailTask
import com.example.netflixremake.util.MovieTask

class MovieActivity : AppCompatActivity(), MovieDetailTask.Callback, MovieTask.Callback {

    private lateinit var txtTitle: TextView
    private lateinit var txtPlot: TextView
    private lateinit var txtActors: TextView
    private lateinit var adapter: MovieAdapter
    private lateinit var progress: ProgressBar
    private val movies = mutableListOf<Movie>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie)

        progress = findViewById(R.id.movieProgress)
        txtTitle = findViewById(R.id.movieTxtTitle)
        txtPlot = findViewById(R.id.movieTxtDesc)
        txtActors = findViewById(R.id.movieTxtCast)

        val rv: RecyclerView = findViewById(R.id.movieRvSimilar)

        val id = intent?.getStringExtra("id") ?: throw IllegalStateException("ID não foi encontrado!")

        val url2 = "https://www.omdbapi.com/?s=Sherlock&page=&apikey=517538ae"
        val url = "https://www.omdbapi.com/?i=$id&plot=full&apikey=517538ae"

        MovieTask(this," ").execute(url2)
        MovieDetailTask(this, movies).execute(url)

        adapter = MovieAdapter(movies, R.layout.movie_item_similar)
        rv.layoutManager = GridLayoutManager(this, 3)
        rv.adapter = adapter

        val toolbar:Toolbar = findViewById(R.id.movieToolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPreExecute() {
        progress.visibility = View.VISIBLE
    }

    //onResult do MovieTask
    override fun onResult(movies: List<Movie>, name: String) {
        this.movies.addAll(movies)
        adapter.notifyDataSetChanged()
    }

    //onResult do MovieDetailTask
    override fun onResult(movieDetail: MovieDetail) {
        progress.visibility = View.GONE

        txtTitle.text  = movieDetail.movie.title
        txtPlot.text = movieDetail.movie.plot
        txtActors.text = getString(R.string.actors, movieDetail.movie.actors)

        DownloadImageTask(object : DownloadImageTask.Callback {
            override fun onResult(bitmap: Bitmap) {
                val layerDrawable: LayerDrawable = ContextCompat.getDrawable(this@MovieActivity, R.drawable.shadows) as LayerDrawable
                val movieCover = BitmapDrawable(resources, bitmap)
                layerDrawable.setDrawableByLayerId(R.id.coverDrawable, movieCover)
                val coverImg: ImageView = findViewById(R.id.movieImg)
                coverImg.setImageDrawable(layerDrawable)
            }
        }).execute(movieDetail.movie.poster)
    }

    override fun onFailure(message: String) {
        progress.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}