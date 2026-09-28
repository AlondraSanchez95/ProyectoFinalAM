package com.example.proyectofinal.data

import com.google.gson.annotations.SerializedName

data class BelvoTokenResponse(
    @SerializedName("access") val access: String? = null,
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("token") val directToken: String? = null
) {
    val token: String get() = accessToken ?: access ?: directToken ?: ""
}

data class BelvoLinkRequest(
    @SerializedName("link") val link: String
)

data class BelvoAccount(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("balance") val balance: Double?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("current_balance") val currentBalance: Double?,
    @SerializedName("used_balance") val usedBalance: Double?
)

data class BelvoTransaction(
    @SerializedName("id") val id: String,
    @SerializedName("account_id") val accountId: String?,
    @SerializedName("amount") val amount: Double,
    @SerializedName("description") val description: String?,
    @SerializedName("value_date") val valueDate: String?,
    @SerializedName("type") val type: String?
)
