package com.example.dogedex.api.responses

import com.squareup.moshi.Json

class AuthApiResponse(val message:String,
                      @field:Json(name = "is_success") val isSUccess:Boolean,
                      val data :UserResponse)