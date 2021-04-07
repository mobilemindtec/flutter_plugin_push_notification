package io.flutter.plugins.push.extra

import org.json.JSONArray
import org.json.JSONObject


class JsonObjectExtended : JSONObject() {
    companion object {
        //Overwrite for the method wrap as it is only available in Api Level 19+ and this ensures
        //it will work on lower level deployments

        /**
         * Wraps the given object if necessary.
         *
         *
         * If the object is null or , returns [.NULL].
         * If the object is a `JSONArray` or `JSONObject`, no wrapping is necessary.
         * If the object is `NULL`, no wrapping is necessary.
         * If the object is an array or `Collection`, returns an equivalent `JSONArray`.
         * If the object is a `Map`, returns an equivalent `JSONObject`.
         * If the object is a primitive wrapper type or `String`, returns the object.
         * Otherwise if the object is from a `java` package, returns the result of `toString`.
         * If wrapping fails, returns null.
         */
        fun wrap(o: Any?): Any? {
            if (o == null) {
                return NULL
            }
            if (o is JSONArray || o is JSONObject) {
                return o
            }
            if (o!! == NULL) {
                return o
            }
            try {
                if (o is Collection<*>) {
                    return JSONArray(o as Collection<*>?)
                } else if (o!!.javaClass.isArray) {
                    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        JSONArray(o)
                    } else {
                        var ret = JSONArray();
                        for(i in o as Array<*>){
                            ret.put(i)
                        }
                        ret
                    }
                }
                if (o is Map<*, *>) {
                    return JSONObject(o as Map<*, *>?)
                }
                if (o is Boolean ||
                        o is Byte ||
                        o is Character ||
                        o is Double ||
                        o is Float ||
                        o is Integer ||
                        o is Long ||
                        o is Short ||
                        o is String) {
                    return o
                }
                if (o!!.javaClass.getPackage()!!.name.startsWith("java.")) {
                    return o!!.toString()
                }
            } catch (ignored: Exception) {
            }

            return null
        }
    }

}
