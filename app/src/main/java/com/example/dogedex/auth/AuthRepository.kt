package com.example.dogedex.auth

import com.example.dogedex.api.ApiResponseStatus
import com.example.dogedex.api.DogsApi
import com.example.dogedex.api.dto.LoginDTO
import com.example.dogedex.api.dto.SignUpDTO
import com.example.dogedex.api.dto.UserDTOMapper
import com.example.dogedex.api.makeNetworkCall
import com.example.dogedex.model.User

class AuthRepository {

    suspend fun login(
        email: String,
        password: String,
    ): ApiResponseStatus<User> = makeNetworkCall {
        val loginDTO = LoginDTO(email, password)
        val loginResponse = DogsApi.retrofitService.login(loginDTO)
        if (!loginResponse.isSUccess) {
            throw Exception(loginResponse.message)
        }
        val userDTO = loginResponse.data.user
        val userDTOMapper = UserDTOMapper()
        userDTOMapper.fromUserDTOToUSerDomain(userDTO)
    }

    suspend fun signUp(
        email: String,
        password: String,
        passwordConfirmation: String
    ): ApiResponseStatus<User> = makeNetworkCall {
        val signUpDTo = SignUpDTO(email, password, passwordConfirmation)
        val signUpResponse = DogsApi.retrofitService.signUp(signUpDTo)
        if (!signUpResponse.isSUccess) {
            throw Exception(signUpResponse.message)
        }
        val userDTO = signUpResponse.data.user
        val userDTOMapper = UserDTOMapper()
        userDTOMapper.fromUserDTOToUSerDomain(userDTO)
    }

}