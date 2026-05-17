private val VPN_REQUEST_CODE = 1001
private var isVpnMode = false

// اضافه کردن به MainActivity موجود
private fun connectVpn(config: String) {
    val intent = Intent(this, ZyrlnVpnService::class.java)
    intent.action = ZyrlnVpnService.ACTION_CONNECT
    intent.putExtra("config", config)
    ContextCompat.startForegroundService(this, intent)
}

private fun disconnectVpn() {
    val intent = Intent(this, ZyrlnVpnService::class.java)
    intent.action = ZyrlnVpnService.ACTION_DISCONNECT
    startService(intent)
}

// تغییر دکمه Connect در ConfigAdapter
binding.btnConnect.setOnClickListener {
    if (isVpnMode) {
        // درخواست مجوز VPN
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, VPN_REQUEST_CODE)
        } else {
            connectVpn(configJson)
        }
    } else {
        // حالت پروکسی قبلی
        startProxyWithConfig(configJson)
    }
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
        connectVpn(currentConfigJson)
    }
}