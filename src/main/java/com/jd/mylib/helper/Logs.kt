package com.jd.mylib.helper

import android.util.Log

object Logs {
    private const val TAG = "Logs"
    fun e(tag: String = TAG, msg: String, tr: Throwable?) {
        Log.e(tag, "JARVIS: $msg", tr)
    }

    fun e(tag: String = TAG, msg: String) {
        Log.e(tag, "JARVIS: $msg")
    }

    fun d(tag: String = TAG, msg: String) {
        Log.d(tag, "JARVIS: $msg")
    }

    fun i(tag: String = TAG, msg: String) {
        Log.i(tag, "JARVIS: $msg")
    }

    fun w(tag: String = TAG, msg: String) {
        Log.w(tag, "JARVIS: $msg")
    }

    fun w(tag: String = TAG, msg: String, tr: Throwable?) {
        Log.w(tag, "JARVIS: $msg", tr)
    }

    fun p(tag: String = TAG, msg: String) {
        print("$tag JARVIS printing... $msg")
    }
}