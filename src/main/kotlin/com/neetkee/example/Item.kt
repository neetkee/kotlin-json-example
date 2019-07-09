package com.neetkee.example

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotBlank

data class Item(
        @get:NotBlank
        val name: String,
        val description: String,
        @get:NotBlank
        @JsonProperty("completelyDifferentName")
        val someField: String,
        @get:NotBlank
        val anotherField: String
) {
    @NotBlank
    var anotherOne: String? = null
}