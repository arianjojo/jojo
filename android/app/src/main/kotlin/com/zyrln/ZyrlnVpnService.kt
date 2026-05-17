package com.zyrln

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import com.zyrln.relay.MainActivity
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress

class ZyrlnVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private lateinit var proxyAddress: InetSocketAddress
    private val CHANNEL_ID = "zyrln_vpn_channel"
    private val NOTIF_ID = 123

    companion object {
        const val ACTION_CONNECT = "com.zyrln.CONNECT"
        const val ACTION_DISCONNECT = "com.zyrln.DISCONNECT"
        var configJson: String = ""
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Zyrln VPN",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class).createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                configJson = intent.getStringExtra("config") ?: return START_NOT_STICKY
                startVpn()
            }
            ACTION_DISCONNECT -> stopVpn()
        }
        return START_STICKY
    }

    private fun startVpn() {
        if (isRunning) return

        val builder = Builder()
        builder.setSession("Zyrln VPN")
        builder.addAddress("10.0.0.2", 24)
        builder.addRoute("0.0.0.0", 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }
        builder.addDnsServer("8.8.8.8")
        builder.addDnsServer("1.1.1.1")

        vpnInterface = builder.establish() ?: return

        proxyAddress = InetSocketAddress("127.0.0.1", 8085)
        startForeground(NOTIF_ID, createNotification())
        isRunning = true

        // شروع کردن core Zyrln (که روی 8085 گوش می‌ده)
        startZyrlnCore()
    }

    private external fun startZyrlnCore(): Boolean

    private fun stopZyrlnCore() {
        // بعداً با stop از Go
    }

    private fun stopVpn() {
        isRunning = false
        stopZyrlnCore()
        vpnInterface?.close()
        vpnInterface = null
        stopForeground(true)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Zyrln VPN")
                .setContentText("Active - routing all traffic")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setContentIntent(pendingIntent)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("Zyrln VPN")
                .setContentText("Active")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setContentIntent(pendingIntent)
                .build()
        }
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}