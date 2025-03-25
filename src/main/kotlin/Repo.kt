package org.example

import com.google.gson.annotations.SerializedName

data class Repo(
    val name: String,
    val description: String? = null,
    @SerializedName("html_url") val url: String
)