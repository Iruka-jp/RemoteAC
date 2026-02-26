package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ConfigViewModel : ViewModel() {
    private val _configs = MutableLiveData<List<Config>>(emptyList())
    val configs: LiveData<List<Config>> = _configs

    fun addConfig(config: Config) {
        val currentList = _configs.value ?: emptyList()
        _configs.value = currentList + config
    }
}