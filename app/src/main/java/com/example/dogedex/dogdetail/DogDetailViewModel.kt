package com.example.dogedex.dogdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogedex.api.ApiResponseStatus
import com.example.dogedex.doglist.DogRepository
import com.example.dogedex.model.Dog
import kotlinx.coroutines.launch

class DogDetailViewModel: ViewModel() {

    private val _dogList = MutableLiveData<List<Dog>>()
    val dogList: LiveData<List<Dog>>
        get() = _dogList

    private val _status = MutableLiveData<ApiResponseStatus<Any>>()
    val status: LiveData<ApiResponseStatus<Any>>
        get() = _status

    private val dogRepository = DogRepository()


    fun addDogToUser(dogId:Long){
        viewModelScope.launch {
            _status.value = ApiResponseStatus.Loading()
            handleAddDogToUserRespopnseStatus(dogRepository.addDogToUser(dogId))

        }
    }

    private fun handleAddDogToUserRespopnseStatus(apiResponseStatus:ApiResponseStatus<Any>) {
        _status.value = apiResponseStatus
    }
}