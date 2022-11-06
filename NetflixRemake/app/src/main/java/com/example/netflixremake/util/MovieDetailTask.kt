package com.example.netflixremake.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.netflixremake.model.Movie
import com.example.netflixremake.model.MovieDetail
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class MovieDetailTask(private val callback: Callback, private val similars: List<Movie>) {

    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    interface Callback {
        fun onPreExecute()
        fun onResult(movieDetail: MovieDetail)
        fun onFailure(message: String)
    }

    fun execute(url: String) {
        callback.onPreExecute()

        executor.execute {
            var urlConnection: HttpsURLConnection? = null
            var buffer: BufferedInputStream? = null
            var stream: InputStream? = null

            try {
                val requestURl = URL(url) // abrir uma URl
                urlConnection = requestURl.openConnection() as HttpsURLConnection // Abrir uma conexão
                urlConnection.readTimeout = 2000 //tempo de leitura (2s)
                urlConnection.connectTimeout = 2000 //tempo de conexão (2s)

                val statusCode = urlConnection.responseCode
                if(statusCode == 400){
                    stream = urlConnection.errorStream
                    buffer = BufferedInputStream(stream)
                    val jsonAsString = toString(buffer)
                } else if (statusCode > 400){
                    throw IOException("Erro na comunicação com o servidor!")
                }

                stream = urlConnection.inputStream
                //forma 1: simples e rápida
//                val stream = urlConnection.inputStream // sequencia de bytes
//                val jsonAsString = stream.bufferedReader().use { it.readText() }
//                Log.i("Teste", jsonString)

                //forma 2:
                buffer = BufferedInputStream(stream)
                val jsonAsString = toString(buffer)

                //JSON está preparado para ser convertido em um DATA CLASS!!
                val movieDetail = toMovieDetail(jsonAsString)

                handler.post{
                    //aqui roda dentro da UI-Thread
                    callback.onResult(movieDetail)
                }

            }catch (e: IOException){
                val message = e.message ?: "erro desconhecido"
                Log.e("Teste", message, e)
                handler.post {
                    callback.onFailure(message)
                }
            }finally {
                urlConnection?.disconnect()
                stream?.close()
                buffer?.close()
            }
        }
    }

    private fun toMovieDetail(jsonAsString: String) : MovieDetail {
        val jsonMovie = JSONObject(jsonAsString)
        val id = jsonMovie.getString("imdbID")
        val title = jsonMovie.getString("Title")
        val poster = jsonMovie.getString("Poster")
        val plot = jsonMovie.getString("Plot")
        val actors = jsonMovie.getString("Actors")

        return MovieDetail(Movie(id, title, poster, plot, actors), similars)
    }

    private fun toString(stream: InputStream) : String {
        val bytes = ByteArray(1024)
        val baos = ByteArrayOutputStream()
        var read: Int

        while(true){
            read = stream.read(bytes)
            if (read <= 0){
                break
            }
            baos.write(bytes, 0, read)
        }
        return String(baos.toByteArray())
    }
}