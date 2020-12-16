package com.wuttke.chatserver_controlpanel.viewmodel

data class ChatUser(
        var socketID : String = "",
        var loginTime : String? = "00:00",
        var name : String? = null,
        var logoutTime : String? = null,
        var neueNachrichten : Int = 0
)