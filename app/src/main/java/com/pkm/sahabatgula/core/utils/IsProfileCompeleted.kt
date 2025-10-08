package com.pkm.sahabatgula.core.utils

import com.pkm.sahabatgula.data.remote.model.MyProfile

fun isProfileCompleted (myProfile: MyProfile): Boolean {
    return myProfile.height != null
}
