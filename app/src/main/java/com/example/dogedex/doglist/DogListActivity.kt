package com.example.dogedex.doglist

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dogedex.api.ApiResponseStatus
import com.example.dogedex.databinding.ActivityDogListBinding
import com.example.dogedex.dogdetail.DogDetailActivity
import com.example.dogedex.dogdetail.DogDetailActivity.Companion.DOG_KEY



private const val GRID_SPAN_COUNT = 3
class DogListActivity : AppCompatActivity() {

    private val dogListViewModel: DogListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDogListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val loadingWheel = binding.loadingWheel

        val recycler = binding.dogRecycler
        recycler.layoutManager = GridLayoutManager(this, GRID_SPAN_COUNT)

        val adapter = DogAdapter()

        adapter.setOnItemClickListener {
            val intent = Intent(this, DogDetailActivity::class.java)
            intent.putExtra(DOG_KEY, it)
            startActivity(intent)

        }
        recycler.adapter = adapter


        dogListViewModel.dogList.observe(this) {
            adapter.submitList(it)
        }

        dogListViewModel.status.observe(this) {
            when (it) {
                is ApiResponseStatus.Error -> {
                    loadingWheel.visibility = View.GONE
                    Toast.makeText(this, it.messageId, Toast.LENGTH_SHORT).show()
                }
                is ApiResponseStatus.Loading -> loadingWheel.visibility = View.VISIBLE
                is ApiResponseStatus.Succcess -> loadingWheel.visibility = View.GONE
            }
        }

    }
}