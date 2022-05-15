package ug.zach.ugali.utils.ugali

import android.app.Activity
import android.content.SharedPreferences
import java.io.File


/**
 * Created by Ugali on 2/22/17.
 */

class UgSoft(activity: Activity) {

    init {
        cache = activity.getSharedPreferences("cache", 0)
        editor = cache.edit()
    }
    
    companion object {

        fun getInstance(activity: Activity): UgSoft {
            cach_dir = activity.cacheDir
            return UgSoft(activity)
        }

        operator fun set(name: String, value: String) {
            editor.putString(name, value)
            editor.commit()
        }

        operator fun get(name: String): String? {
            return cache.getString(name, name)
        }

        fun clear() {
            editor.clear()
            editor.commit()
        }

        private lateinit var cache: SharedPreferences
        private lateinit var editor: SharedPreferences.Editor


        var cach_dir: File? = null
    }


}
