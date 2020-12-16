package com.wuttke.chatserver_controlpanel.viewmodel

data class ServerData(
        var server: ArrayList<Server>
)

data class Server(
        var serverURL : String,
        var user : ArrayList<String>,
        var pass : ArrayList<String>
)



