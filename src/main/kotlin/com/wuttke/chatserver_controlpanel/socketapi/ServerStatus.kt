package com.wuttke.chatserver_controlpanel.socketapi


/**
 * Class to communicate with the server on serverstatus topics
 */
enum class ServerStatus {

    CHATUSERS,
    ADDCHATUSER,
    REMOVECHATUSER,
    CONNECTEDUSER,
    ADDCONNECTEDUSER,
    REMOVECONNECTEDUSER

}