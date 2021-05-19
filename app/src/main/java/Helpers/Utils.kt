package Helpers

class Utils {
    companion object {
        fun progressToString(p: Int): String {
            var str = ""
            val min = p / 1000 / 60
            val sec = p / 1000 % 60
            if (min == 0) {
                str += "0:"
            } else if (min < 10) {
                str = min.toString() + ":"
            } else {
                str = min.toString() + ":"
            }
            if (sec < 10) {
                str = str + "0" + sec.toString()
            } else {
                str += sec.toString()
            }
            return str
        }
    }

}