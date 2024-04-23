package com.example.mc1
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrientationViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)

    private val _orientations = MutableStateFlow<List<OrientationEntity>>(emptyList())
    val orientations = _orientations.asStateFlow()

    fun fetchOrientations() {
        viewModelScope.launch(Dispatchers.IO) {
            _orientations.value = db.orientationDao().getAllOrientations()
        }
    }
}
