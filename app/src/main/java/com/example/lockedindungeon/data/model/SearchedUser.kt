package com.example.lockedindungeon.data.model

/**
 * UI facing user row for the friend search dialog.
 *
 * [com.example.lockedindungeon.dto.UserDto] is generated from the OpenAPI spec and gets overwritten
 * on every `openApiGenerate`, so the extra UI state ([isFollowed]) lives here instead of being added
 * to the generated class.
 */
data class SearchedUser(
    val id : Long?,
    val username : String,
    val isFollowed : Boolean = false
)
