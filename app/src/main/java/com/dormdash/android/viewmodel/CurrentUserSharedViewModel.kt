package com.dormdash.android.viewmodel

import androidx.lifecycle.ViewModel
import com.dormdash.android.database.Students

class CurrentUserSharedViewModel : ViewModel() {
    var currentUser: Students? = null
}