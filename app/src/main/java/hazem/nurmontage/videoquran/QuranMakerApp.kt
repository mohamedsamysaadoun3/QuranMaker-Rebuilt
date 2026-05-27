package hazem.nurmontage.videoquran

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner

class QuranMakerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        @Volatile
        private lateinit var instance: QuranMakerApp

        fun getInstance(): QuranMakerApp = instance
    }
}
