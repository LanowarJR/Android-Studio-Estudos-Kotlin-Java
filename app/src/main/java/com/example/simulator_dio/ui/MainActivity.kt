package com.example.simulator_dio.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simulator_dio.R
import com.example.simulator_dio.data.MatchesApi
import com.example.simulator_dio.databinding.ActivityMainBinding
import com.example.simulator_dio.domain.Match
import com.example.simulator_dio.ui.adapter.MatchesAdapter
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.random.Random


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var matchesApi: MatchesApi

    //private lateinit var matchesAdapter: MatchesAdapter
    private var matchesAdapter: MatchesAdapter? = MatchesAdapter(Collections.emptyList())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHttpClient()
        setupMatchesList()
        setupMatchesRefresh()
        setupFloatingActionButton()

    }


    private fun setupHttpClient() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://lanowarjr.github.io/matches-simulator-api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        matchesApi = retrofit.create(MatchesApi::class.java)
    }


    private fun setupMatchesList() {
        binding.rvMatches.setHasFixedSize(true)
        binding.rvMatches.layoutManager = LinearLayoutManager(this)
        binding.rvMatches.adapter = matchesAdapter
        findMatchesFromApi()
    }


    private fun setupMatchesRefresh() {
        binding.srlMatches.setOnRefreshListener(::findMatchesFromApi)
    }


    private fun findMatchesFromApi() {
        binding.srlMatches.isRefreshing = true
        matchesApi.matches.enqueue(object : Callback<List<Match?>?> {
            override fun onResponse(call: Call<List<Match?>?>, response: Response<List<Match?>?>) {
                if (response.isSuccessful) {
                    val matches = response.body()
                    matchesAdapter = MatchesAdapter(matches)
                    binding.rvMatches.adapter = matchesAdapter
                } else {
                    showErrorMessage()
                }
                binding.srlMatches.isRefreshing = false
            }

            override fun onFailure(call: Call<List<Match?>?>, t: Throwable) {
                showErrorMessage()
                binding.srlMatches.isRefreshing = false
            }
        })
    }


    private fun setupFloatingActionButton() {
        binding.fabSimulate.setOnClickListener { view ->
            view.animate().rotationBy(360F).setDuration(500)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        val random = Random
                        for (i in 0 until matchesAdapter!!.itemCount) {
                            val match = matchesAdapter!!.matches[i]
                            match.homeTeam
                                .score = random.nextInt(match.homeTeam.stars + 1)
                            match.awayTeam
                                .score = random.nextInt(match.awayTeam.stars + 1)
                            matchesAdapter!!.notifyItemChanged(i)
                        }
                    }
                })
        }
    }


    private fun showErrorMessage() {
        Snackbar.make(binding.fabSimulate, R.string.error_Api, Snackbar.LENGTH_LONG).show()
    }
}
