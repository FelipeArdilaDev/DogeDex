package com.example.dogedex.doglist

import com.example.dogedex.R
import com.example.dogedex.api.ApiResponseStatus
import com.example.dogedex.api.DogsApi.retrofitService
import com.example.dogedex.api.dto.AddDogToUserDTO
import com.example.dogedex.api.dto.DogDTOMaper
import com.example.dogedex.api.makeNetworkCall
import com.example.dogedex.model.Dog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class DogRepository {

    suspend fun getDogCollection(): ApiResponseStatus<List<Dog>> {
        return withContext(Dispatchers.IO) {
            val allDogdListDeferred = async { downloadDogs() }
            val userDogsListDeferred = async { getUserDogs() }

            val allDogdListResponse = allDogdListDeferred.await()
            val userDogsListResponse = userDogsListDeferred.await()

            if (allDogdListResponse is ApiResponseStatus.Error) {
                allDogdListResponse
            } else if (userDogsListResponse is ApiResponseStatus.Error) {
                userDogsListResponse
            } else if (allDogdListResponse is ApiResponseStatus.Succcess && userDogsListResponse is ApiResponseStatus.Succcess) {
                ApiResponseStatus.Succcess(
                    getCollectionList(
                        allDogdListResponse.data,
                        userDogsListResponse.data
                    )
                )
            } else {
                ApiResponseStatus.Error(R.string.unknown_error)
            }
        }

    }

    private fun getCollectionList(allDogList: List<Dog>, userDogList: List<Dog>) =
        allDogList.map {
            if (userDogList.contains(it)) {
                it
            } else {
                Dog(
                    0, it.index, "", "", "", "", "",
                    "", "", "", "", false
                )
            }
        }.sorted()


    private suspend fun downloadDogs(): ApiResponseStatus<List<Dog>> = makeNetworkCall {
        val dogListApiResponse = retrofitService.getAllDogs()
        val dogDTOList = dogListApiResponse.data.dogs
        val dogDTOMapper = DogDTOMaper()
        dogDTOMapper.fromDogDTOListToDogDmainList(dogDTOList)
    }

    suspend fun addDogToUser(dogId: Long): ApiResponseStatus<Any> = makeNetworkCall {
        val addDogToUserDTO = AddDogToUserDTO(dogId)
        val defaultResponse = retrofitService.addDogToUSer(addDogToUserDTO)

        if (!defaultResponse.isSUccess) {
            throw Exception(defaultResponse.message)
        }
    }

    private suspend fun getUserDogs(): ApiResponseStatus<List<Dog>> = makeNetworkCall {
        val dogListApiResponse = retrofitService.getUserDogs()
        val dogDTOList = dogListApiResponse.data.dogs
        val dogDTOMapper = DogDTOMaper()
        dogDTOMapper.fromDogDTOListToDogDmainList(dogDTOList)
    }

    suspend fun getDogByMlId(mlDogID:String) : ApiResponseStatus<Dog> = makeNetworkCall {
        val response = retrofitService.getDogByMlId(mlDogID)
        if (!response.isSUccess) {
            throw Exception(response.message)
        }

        val dogDTOMapper = DogDTOMaper()
        dogDTOMapper.fromDogDTOToDogDomain(response.data.dog)
    }

}