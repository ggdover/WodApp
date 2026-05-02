package com.ggdover.wodapp

import android.app.Application
import com.ggdover.wodapp.data.local.WodDatabase
import com.ggdover.wodapp.data.repository.WodRepository

class WodApplication : Application() {
    val database: WodDatabase by lazy { WodDatabase.build(this) }
    val repository: WodRepository by lazy { WodRepository(database) }
}
