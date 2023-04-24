package com.shinbash.tpk.utils

import android.view.KeyEvent

object ScanCodeHelper {
    /**
     * 拼接扫描到的内容
     *
     * @param mStringBufferResult 接收的结果
     * @param event               用于判断是否按住了Shift键
     * @param keyCode             输入的键
     */
    fun appendInput(mStringBufferResult: StringBuffer, event: KeyEvent, keyCode: Int) {
        val mCaps = checkLetterStatus(event)
        if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
            // 字母
            mStringBufferResult.append(((if (mCaps) 'A' else 'a').toInt() + keyCode - KeyEvent.KEYCODE_A).toChar())
        } else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            // 数字
            mStringBufferResult.append(('0'.toInt() + keyCode - KeyEvent.KEYCODE_0).toChar())
        } else {
            //其他符号
            when (keyCode) {
                KeyEvent.KEYCODE_SEMICOLON -> mStringBufferResult.append(if (mCaps) ':' else ';')
                KeyEvent.KEYCODE_SLASH -> mStringBufferResult.append(if (mCaps) '?' else '/')
                KeyEvent.KEYCODE_PERIOD -> mStringBufferResult.append('.')
                KeyEvent.KEYCODE_EQUALS -> mStringBufferResult.append(if (mCaps) '+' else '=')
            }
        }
    }

    /**
     * 检查shift键
     */
    private fun checkLetterStatus(event: KeyEvent): Boolean {
        var mCaps = false
        val keyCode = event.keyCode
        if (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT || keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) {
            //按着shift键，表示大写
            //松开shift键，表示小写
            mCaps = event.action == KeyEvent.ACTION_DOWN
        }

        return mCaps

    }
}