package Helpers

import android.graphics.Bitmap
import android.graphics.Color
import androidx.palette.graphics.Palette

class Utils {
    companion object {
        fun progressToString(p: Int): String {
            var str = ""
            val min = p / 1000 / 60
            val sec = p / 1000 % 60
            if (min == 0)
                str += "0:"
            else if (min < 10)
                str = "$min:"
            else
                str = "$min:"
            if (sec < 10)
                str = str + "0" + sec.toString()
            else
                str += sec.toString()
            return str
        }

        fun extractMutedColor(art: Bitmap): Int {
            val myPalette = Palette.from(art).generate()
            val muted = myPalette.mutedSwatch ?: myPalette.darkVibrantSwatch
            return muted?.rgb ?: Color.DKGRAY
        }
    }
}