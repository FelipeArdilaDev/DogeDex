package com.example.dogedex.dogdetail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.dogedex.R
import com.example.dogedex.api.ApiResponseStatus
import com.example.dogedex.databinding.ActivityDogDetailBinding
import com.example.dogedex.model.Dog

class DogDetailActivity : AppCompatActivity() {

    companion object {
        const val DOG_KEY = "dog"
        const val IS_RECOGNITION_KEY = "is_recognition"
    }

    private val viewModel:DogDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDogDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dog = intent?.extras?.getParcelable<Dog>(DOG_KEY)
        val isRecognition = intent?.extras?.getBoolean(IS_RECOGNITION_KEY, false) ?: false


        if (dog == null) {
            Toast.makeText(
                this,
                getString(R.string.error_showing_dog_not_found),
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        binding.dogIndex.text = getString(R.string.dog_index_format, dog.index)
        binding.lifeExpectancy.text =
            getString(R.string.dog_life_expectancy_format, dog.lifeExpectancy)

        binding.dog = dog
        binding.dogImage.load(dog.imageUrl)

        viewModel.status.observe(this) {
            when (it) {
                is ApiResponseStatus.Error -> {
                    binding.loadingWheel.visibility = View.GONE
                    Toast.makeText(this, it.messageId, Toast.LENGTH_SHORT).show()
                }
                is ApiResponseStatus.Loading -> binding.loadingWheel.visibility = View.VISIBLE
                is ApiResponseStatus.Succcess -> {
                    binding.loadingWheel.visibility = View.GONE
                    finish()
                }
            }
        }

        binding.closeButton.setOnClickListener {
            if (isRecognition) {
                viewModel.addDogToUser(dog.id)
            } else {
                finish()
            }
        }
    }
}