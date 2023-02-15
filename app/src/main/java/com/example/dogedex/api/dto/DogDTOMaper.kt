package com.example.dogedex.api.dto

import com.example.dogedex.model.Dog

class DogDTOMaper {

    fun fromDogDTOToDogDomain(dogDTO: DogDTO) : Dog {
        return Dog(dogDTO.id, dogDTO.index, dogDTO.name,dogDTO.type,
            dogDTO.heightFemale, dogDTO.heightMale, dogDTO.imageUrl, dogDTO.lifeExpectancy,
            dogDTO.temperament, dogDTO.weightMale, dogDTO.weightFemale)
    }

    fun fromDogDTOListToDogDmainList(dogDTOList: List<DogDTO>) : List<Dog> {
        return dogDTOList.map { fromDogDTOToDogDomain(it) }
    }
}