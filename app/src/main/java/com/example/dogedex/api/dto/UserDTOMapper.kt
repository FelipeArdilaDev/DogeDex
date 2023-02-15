package com.example.dogedex.api.dto

import com.example.dogedex.model.User

class UserDTOMapper {

    fun fromUserDTOToUSerDomain(userDTO: UserDTO): User =
        User(userDTO.id, userDTO.email, userDTO.authenticationToken)

}