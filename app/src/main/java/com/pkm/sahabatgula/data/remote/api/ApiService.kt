package com.pkm.sahabatgula.data.remote.api

import com.pkm.sahabatgula.data.remote.model.ActivityCategories
import com.pkm.sahabatgula.data.remote.model.ActivityCategoryListResponse
import com.pkm.sahabatgula.data.remote.model.ActivityResponse
import com.pkm.sahabatgula.data.remote.model.CategoryListResponse
import com.pkm.sahabatgula.data.remote.model.DetailFoodResponse
import com.pkm.sahabatgula.data.remote.model.FoodCategories
import com.pkm.sahabatgula.data.remote.model.FoodListResponse
import com.pkm.sahabatgula.data.remote.model.ForgotPasswordRequest
import com.pkm.sahabatgula.data.remote.model.ForgotPasswordResponse
import com.pkm.sahabatgula.data.remote.model.GoogleAuthRequest
import com.pkm.sahabatgula.data.remote.model.GoogleAuthResponse
import com.pkm.sahabatgula.data.remote.model.LogActivityRequest
import com.pkm.sahabatgula.data.remote.model.LogActivityResponse
import com.pkm.sahabatgula.data.remote.model.LogFoodRequest
import com.pkm.sahabatgula.data.remote.model.LogFoodResponse
import com.pkm.sahabatgula.data.remote.model.LogWaterRequest
import com.pkm.sahabatgula.data.remote.model.LogWaterResponse
import com.pkm.sahabatgula.data.remote.model.LoginRequest
import com.pkm.sahabatgula.data.remote.model.LoginResponse
import com.pkm.sahabatgula.data.remote.model.MyProfileResponse
import com.pkm.sahabatgula.data.remote.model.PredictionResponse
import com.pkm.sahabatgula.data.remote.model.ProfileData
import com.pkm.sahabatgula.data.remote.model.SetupProfileResponse
import com.pkm.sahabatgula.data.remote.model.RegisterRequest
import com.pkm.sahabatgula.data.remote.model.RegisterResponse
import com.pkm.sahabatgula.data.remote.model.ResendOtpRequest
import com.pkm.sahabatgula.data.remote.model.ResendOtpResponse
import com.pkm.sahabatgula.data.remote.model.ResetPasswordRequest
import com.pkm.sahabatgula.data.remote.model.ResetPasswordResponse
import com.pkm.sahabatgula.data.remote.model.SummaryResponse
import com.pkm.sahabatgula.data.remote.model.VerifyOtpRequest
import com.pkm.sahabatgula.data.remote.model.VerifyOtpResponse
import com.pkm.sahabatgula.data.remote.model.VerifyResetOtpRequest
import com.pkm.sahabatgula.data.remote.model.VerifyResetOtpResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("register")
    suspend fun register(@Body userDataItem: RegisterRequest): Response<RegisterResponse>

    @POST("resend-otp")
    suspend fun resendOtp(@Body body: ResendOtpRequest): Response<ResendOtpResponse>

    @POST("verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): Response<VerifyOtpResponse>

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("google")
    suspend fun googleAuth(@Body body: GoogleAuthRequest): Response<GoogleAuthResponse>

    @POST("forgot-password")
    suspend fun forgotPasswordInputEmail(@Body body: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    @POST("verify-reset-otp")
    suspend fun verifyResetOtp(@Body body: VerifyResetOtpRequest): Response<VerifyResetOtpResponse>

    @POST("reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<ResetPasswordResponse>

    @POST ("setup")
    suspend fun setupProfile(
        @Header("Authorization") token: String,
        @Body profileData: ProfileData
    ): Response<SetupProfileResponse>

    @GET("me")
    suspend fun getMyProfile(@Header("Authorization") token: String): Response<MyProfileResponse>

    @GET("summary")
    suspend fun getSummary(@Header ("Authorization") token: String): Response<SummaryResponse>


    @POST("log-water")
    suspend fun logWater(
        @Header("Authorization") token: String,
        @Body body: LogWaterRequest
    ): Response<LogWaterResponse>

    @Multipart
    @POST("predictions")
    suspend fun predictImage(
        @Part image: MultipartBody.Part
    ): PredictionResponse

    @POST("log-foods")
    suspend fun logFood(
        @Header("Authorization") token: String,
        @Body body: LogFoodRequest
    ): Response<LogFoodResponse>

    @GET ("foods/{id}")
    suspend fun getFoodDetailById(
        @Path("id") id: String
    ): Response<DetailFoodResponse>

    @GET("foods")
    suspend fun  getFoods(
        @Query("page") page: Int,
        @Query("limit") limit : Int = 20,
        @Query("q") query: String? = null,
        @Query("category_id") categoryId: Int? = null
    ): Response<FoodListResponse>

    @GET("food-categories")
    suspend fun getFoodCategories(): Response<CategoryListResponse>

    @POST("log-activities")
    suspend fun logActivity(
        @Header("Authorization") token: String,
        @Body body: LogActivityRequest
    ): Response<LogActivityResponse>

    @GET("activities")
    suspend fun getActivities(
        @Query("page") page: Int,
        @Query("q") query: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("limit") limit: Int = 20
    ): Response<ActivityResponse>

    @GET("activity-categories")
    suspend fun  getActivityCategories(): Response<ActivityCategoryListResponse>
}
