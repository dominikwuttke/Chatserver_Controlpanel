package com.wuttke.chatserver_controlpanel.viewmodel

data class ChatMessage(

        var gesendet_um : String = "",
        var user_gesendet : Boolean = false,
        var nachricht : String = ""
)

