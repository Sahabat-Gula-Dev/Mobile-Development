package com.pkm.sahabatgula.data.repository

import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.remote.model.Event
import com.pkm.sahabatgula.data.remote.model.EventResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject


class EventRepository @Inject constructor(
    private val apiService: ApiService
) {

}