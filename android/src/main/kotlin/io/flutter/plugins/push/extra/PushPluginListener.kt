package io.flutter.plugins.push.extra

/**
 * Defines methods for Success and Error callbacks
 */
interface PushPluginListener {
    /**
     * Defines a success callback method, which is used to pass success function reference
     * from the nativescript to the Java plugin
     *
     * @param message
     * @param data
     */
    fun success(message: String?, title: String?, data: Any?)

    fun success(message: String?)  // method overload to mimic optional argument


    /**
     * Defines a error callback method, which is used to pass success function reference
     * from the nativescript to the Java plugin
     *
     * @param data
     */
    fun error(data: Any?)
}
