package com.shinbash.tpk.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.tech.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

@SuppressLint("UnspecifiedImmutableFlag")
class NfcHelper(private val activity: AppCompatActivity) : LifecycleObserver {
    private val mNfcAdapter by lazy {
        NfcAdapter.getDefaultAdapter(activity)
    }
    private val mIntent by lazy {
        Intent(activity, activity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
    }

    private val mPendingHanIntent by lazy {
        PendingIntent.getActivity(activity, 0, mIntent, 0)
    }

    init {
        activity.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME) fun onResume() {
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(activity, mPendingHanIntent, null, arrayOf(arrayOf(NfcV::class.java.name), arrayOf(NfcA::class.java.name), arrayOf(NfcF::class.java.name), arrayOf(NfcB::class.java.name), arrayOf(Ndef::class.java.name), arrayOf(NdefFormatable::class.java.name), arrayOf(IsoDep::class.java.name), arrayOf(MifareClassic::class.java.name), arrayOf(MifareUltralight::class.java.name)))
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE) fun onPause() {
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(activity)
        }
    }

    fun handleIntent(intent: Intent) {
        val action = intent.action
        if (action == NfcAdapter.ACTION_TECH_DISCOVERED) { // 可用
        }
    }

    companion object {
        private const val TAG = "NfcHelper"
    }
}