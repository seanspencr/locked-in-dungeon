package com.example.lockedindungeon.data.remote

import com.example.lockedindungeon.api.FollowControllerApi
import com.example.lockedindungeon.data.model.ApiResult
import com.example.lockedindungeon.dto.AppUsageBragRequest
import com.example.lockedindungeon.dto.FollowDto
import com.example.lockedindungeon.dto.FollowUserRequest
import com.example.lockedindungeon.dto.FriendListResponse
import com.example.lockedindungeon.dto.MessageResponse
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FriendRepository @Inject constructor(
    client : SingletonApiClientWrapper
) {
    private  val friendApi : FollowControllerApi = client.authenticatedApiClient.createService(
        FollowControllerApi::class.java)

    public fun getFriends(page : Int?, size : Int?): ApiResult<FriendListResponse> {

        val response = friendApi.getFriendList(page, size).execute()
        return ApiResult.mapFromResponse(response)
    }


    public fun getFollowers(page : Int?, size : Int?): ApiResult<FriendListResponse> {
        val response = friendApi.getFollowersOnly(page, size).execute()
        return ApiResult.mapFromResponse(response)
    }


    public fun getFollowing(page : Int?, size : Int?): ApiResult<FriendListResponse> {
        val response = friendApi.getFollowingOnly(page, size).execute()
        return ApiResult.mapFromResponse(response)
    }



    public fun getFriendSuggestions(query : String): ApiResult<FriendListResponse?> {
        val response = friendApi.searchNewFriend(query = query).execute()
        return ApiResult.mapFromResponse(response)
    }

    public fun getIncomingFriendRequest(){
        TODO("backend does not exist yet")

    }


    public fun followUser(req : FollowUserRequest): ApiResult<FollowDto> {
        val response = friendApi.followUser(req).execute()
        return ApiResult.mapFromResponse(response)
    }

    public fun unfollowUser(req  : FollowUserRequest): ApiResult<MessageResponse?> {
        val response = friendApi.unfollowUser(req).execute()
        // unfollow may answer with an empty body, that is still a success
        return ApiResult.mapFromResponseAllowingEmptyBody(response)
    }

    public fun bragStats(req : AppUsageBragRequest): ApiResult<MessageResponse?> {
        val response = friendApi.bragToFriend(req).execute()
        return ApiResult.mapFromResponse(response)
    }
}