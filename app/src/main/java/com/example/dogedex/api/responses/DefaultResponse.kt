package com.example.dogedex.api.responses

import com.squareup.moshi.Json

class DefaultResponse(val message:String,
                      @field:Json(name = "is_success") val isSUccess:Boolean,)