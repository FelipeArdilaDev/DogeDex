package com.example.dogedex.main

import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogedex.api.ApiResponseStatus
import com.example.dogedex.doglist.DogRepository
import com.example.dogedex.machinelearning.Classifier
import com.example.dogedex.machinelearning.ClassifierRepository
import com.example.dogedex.machinelearning.DogRecognition
import com.example.dogedex.model.Dog
import kotlinx.coroutines.launch
import java.nio.MappedByteBuffer

class MainViewModel: ViewModel() {

    private val _dog = MutableLiveData<Dog>()
    val dog: LiveData<Dog>
        get() = _dog

    private val _status = MutableLiveData<ApiResponseStatus<Dog>>()
    val status: LiveData<ApiResponseStatus<Dog>>
        get() = _status

    private val _dogRecognition = MutableLiveData<DogRecognition>()
    val dogRecognition: LiveData<DogRecognition>
        get() = _dogRecognition

    private val dogRepository = DogRepository()
    private lateinit var classifierRepository: ClassifierRepository

    fun setupClassifier(tfLiteModel: MappedByteBuffer, labels: List<String>){
        val classifier = Classifier(tfLiteModel, labels)
        classifierRepository = ClassifierRepository(classifier)
    }

    fun recognizeImage(imageProxy: ImageProxy){
        viewModelScope.launch {
            _dogRecognition.value = classifierRepository.recognizeImage(imageProxy)
            imageProxy.close()
        }

    }

    fun getDogByMlId(mlDogID:String) {
        viewModelScope.launch {
            handleResponseStatus(dogRepository.getDogByMlId(mlDogID))
        }
    }

    private fun handleResponseStatus(apiResponseStatus: ApiResponseStatus<Dog>) {
        if (apiResponseStatus is ApiResponseStatus.Succcess) {
            _dog.value = apiResponseStatus.data!!
        }

        _status.value = apiResponseStatus

    }
}