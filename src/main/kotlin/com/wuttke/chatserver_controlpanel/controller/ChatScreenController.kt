package com.wuttke.chatserver_controlpanel.controller

import com.wuttke.chatserver_controlpanel.viewmodel.ChatMessage
import com.wuttke.chatserver_controlpanel.viewmodel.ChatUser
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.wuttke.chatserver_controlpanel.app.Styles
import com.wuttke.chatserver_controlpanel.socketapi.*
import com.wuttke.chatserver_controlpanel.viewmodel.UserInfo
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import tornadofx.*
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatScreenController : Controller() {

    private val gson = Gson()

    val connectionController : ConnectionController by inject()
    private val mainController : MainScreenController by inject()



    val activeChatUsers     = arrayListOf<ChatUser>().asObservable()
    val offlineChatUsers    = arrayListOf<ChatUser>().asObservable()
    val selectedIndex       = SimpleStringProperty("")
    val sendEnabled         = SimpleBooleanProperty(true)

    var activeChatUsersList : ListView<ChatUser> by singleAssign()
    var offlineChatUsersList : ListView<ChatUser> by singleAssign()

    val chatMessages        = arrayListOf<ChatMessage>().asObservable()

    val userInfo            = arrayListOf<UserInfo>().asObservable()

    val userCount           = SimpleIntegerProperty(0)
    var connectionStatus    : TextFlow by singleAssign()
    var onlineStatus        : Text by singleAssign()

    var nachrichtenScreen   : TextArea by singleAssign()


    init {

        connectionController.angemeldet.onChange {
            changeConnectionStatus(it)
        }

        connectionController.isOnline.onChange {
            onlineStatusChanged(it)
        }

        selectedIndex.onChange {
            fetchUserData(it)
        }

    }


    /**
     * Data related to the chatscreen, which are received from the server are handled here
     * @param jsonObject The data from the server sent as JsonObject
     */
    fun socketMessageReceived(jsonObject: JsonObject){
        when(jsonObject[Types.TYPE.name].asString){

            /**
             * When connecting to a server, the server sends all currently connected users who already
             * chatted with you and sends all infos of them to this application
             * All users are added in a list and displayed on the left side of the control panel
             */
             ServerStatus.CHATUSERS.name->{
                val jsonArray = jsonObject.getAsJsonArray(ServerStatus.CHATUSERS.name)
                activeChatUsers.clear()
                offlineChatUsers.clear()
                jsonArray.forEach {
                    activeChatUsers.add(gson.fromJson(it, ChatUser::class.java))
                    }
                    userCount.value = jsonObject.getAsJsonPrimitive(ServerStatus.CONNECTEDUSER.name).asInt
                }

            /**
             * A user who is ready to chat with you connected to the server
             * the user either just sent you a message or has chatted with you in the past
             * The new user is added to the left side of the control panel
             */
             ServerStatus.ADDCHATUSER.name->{
               val chatUser = Gson().fromJson(jsonObject.getAsJsonObject(ServerStatus.ADDCHATUSER.name),ChatUser::class.java)
               offlineChatUsers.removeIf{ it.socketID == chatUser.socketID }
               activeChatUsers.add(chatUser)
               if (chatUser.socketID == selectedIndex.value) sendEnabled.value = true
            }

            /**
             * A user who chatted with you disconnected from the server
             * the user gets removed from the list of the active chatting users
             * and is added to a list of offline users.
             */
            ServerStatus.REMOVECHATUSER.name->{
                val socketId = jsonObject[ClientStatus.SOCKETID.name].asString

                activeChatUsers.find { it.socketID == socketId }?.let {
                    val formattedTime = with(LocalDateTime.now()){
                        format(DateTimeFormatter.ofPattern("dd/MM/uu HH:mm:ss"))

                    }
                    if(it.socketID == selectedIndex.value) sendEnabled.value = false
                    it.logoutTime = formattedTime
                    offlineChatUsers.add(it)
                    activeChatUsers.remove(it)
                }

            }

            /**
             * A new user visited the website
             */
            ServerStatus.ADDCONNECTEDUSER.name->{
                userCount.value++
            }
            /**
             * A user left your website
             */
            ServerStatus.REMOVECONNECTEDUSER.name->{
                userCount.value--
            }
            /**
             * When you select an user the application fetches the chatlog of the communication with
             * the user and displays it in the middle of the application
             */
            Messages.CHATLOG.name->{
                chatMessages.clear()

                jsonObject.getAsJsonArray(Messages.CHATLOG.name).forEach {
                    val chatMessage = gson.fromJson(it, ChatMessage::class.java)
                    chatMessages.add(chatMessage)
                }

            }

            /**
             * The user you currently have select sent you a message
             * the message is added to the screen in the middle
             * When you have the application in the background the application sends a notification to your system
             */
            Messages.APPENDMESSAGE.name->{
                sendNotification()
                val json = jsonObject.getAsJsonPrimitive( Messages.APPENDMESSAGE.name).asString
                val chatMessage = gson.fromJson(json, ChatMessage::class.java)
                chatMessages.add(chatMessage)
            }
            /**
             * A user sent you a message
             * The user who sent you a message gets marked with the amount of unread messages you got from him at the left side
             * If your application is not focused you'll get an alert sent by your system
             */
            Messages.NEWMESSAGE.name->{
               sendNotification()
                val socketID = jsonObject.getAsJsonPrimitive(Messages.NEWMESSAGE.name).asString
                activeChatUsers.find { it.socketID == socketID }?.let { it.neueNachrichten++ }
                activeChatUsers.sortByDescending { it.neueNachrichten }
            }

            /**
             * An Information has been sucessfully added to a user
             */
            UserInfos.ADDUSERINFO.name->{
                newInfoCreated(jsonObject)
            }

            /**
             * When selecting a User the server sends all additional information you added for this user
             * the information are stored as Strings and displayed on the right side of the application
             */
            UserInfos.GETUSERINFO.name->{
                userInfo.clear()
                jsonObject.getAsJsonArray(UserInfos.GETUSERINFO.name).forEach{
                    userInfo.add(gson.fromJson(it.asJsonObject,UserInfo::class.java))

                }
            }
        }
    }

    /**
     * Send a message to the server, that you need the chatlog and the added info related to a socketID
     * @param socketID the ID of the user of whom you want the chatlog
     */
    private fun fetchUserData(socketID : String?){

        val jsonObject = JsonObject().apply {
            addProperty(Types.TYPE.name, ClientStatus.SOCKETID.name)
            addProperty(ClientStatus.SOCKETID.name,socketID)
        }
        connectionController.webSocket?.send(jsonObject.toString())
        activeChatUsers.find { it.socketID == socketID }?.neueNachrichten = 0
    }

    /**
     * Send a message to the currently selected user
     */
    fun sendMessage(){
        val jsonObject = JsonObject().apply {
            addProperty(Types.TYPE.name, Types.MESSAGE.name)
            addProperty(ClientStatus.SOCKETID.name,selectedIndex.value)
            addProperty(Types.MESSAGE.name,nachrichtenScreen.text)
        }
        connectionController.webSocket?.send(jsonObject.toString())
        nachrichtenScreen.text = ""

    }

    /**
     * Notify the system, that a new message has been received
     */
    private fun sendNotification() {
        try {
            if (!primaryStage.isFocused) {
                primaryStage.toFront()
                if (SystemTray.isSupported()) {

                    val image = Toolkit.getDefaultToolkit().createImage("icon.png")
                    val trayIcon = TrayIcon(image, "Chatserver Admintool").apply {
                        isImageAutoSize = true
                        toolTip = "Chatserver Admintool"
                    }
                    SystemTray.getSystemTray().add(trayIcon)
                    trayIcon.displayMessage("Chatserver Admintool", "New message received", TrayIcon.MessageType.INFO)
                }
            }
        }catch (e : Exception){
            println("error displaying Notification: ${e.message}")
        }

    }

    /**
     * Send message to the server with the info, which shall be added to the currently selected user
     */
    fun createNewInfo(){
        mainController.showDialog("Add Info to User",false){
            val json = JsonObject().apply {
                addProperty(Types.TYPE.name,UserInfos.ADDUSERINFO.name)
                addProperty(UserInfos.ADDUSERINFO.name,it)
                addProperty(ClientStatus.SOCKETID.name,selectedIndex.value)
            }
            connectionController.webSocket?.send(json.toString())
        }
    }

    /**
     * New Info for the user has been added to the database
     */
    private fun newInfoCreated(jsonObject: JsonObject){
        userInfo.add(gson.fromJson(jsonObject.getAsJsonObject(UserInfos.ADDUSERINFO.name),UserInfo::class.java))
    }

    /**
     * Remove info from a user
     * @param infoID the ID as which this info is saved in the database
     */
    fun removeInfo(infoID : Int){
        val json = JsonObject().apply {
            addProperty(Types.TYPE.name,UserInfos.REMOVEINFO.name)
            addProperty(UserInfos.REMOVEINFO.name,infoID)
        }
        connectionController.webSocket?.send(json.toString())
    }


    /**
     * Send message to the server with the new name of the currently selected user
     */
    fun renameUser(){
        mainController.showDialog("new name of the user"){
            val json = JsonObject().apply {
                addProperty(Types.TYPE.name,ClientStatus.RENAMEUSER.name)
                addProperty(ClientStatus.RENAMEUSER.name,it)
                addProperty(ClientStatus.SOCKETID.name,selectedIndex.value)
            }
            activeChatUsers.find { chatUser ->  chatUser.socketID == selectedIndex.value }?.apply{ name = it;activeChatUsersList.refresh() }
            offlineChatUsers.find { chatUser -> chatUser.socketID == selectedIndex.value }?.apply { name = it;offlineChatUsersList.refresh() }
            connectionController.webSocket?.send(json.toString())
        }

    }

    /**
     * Update the display of the current connectionstatus, when
     * a connection to a server is established
     * @param status The new status of the connection to a server
     */
    private fun changeConnectionStatus(status: Boolean){
    connectionStatus.apply {
        clear()
        if (!status) {
            text("not connected"){
                addClass(Styles.unconnected)
                fill = Color.RED
                font = Font(20.0)
            }
        }else{
            text("connected to URL\n"){

                addClass(Styles.connected)
            }
            text("${connectionController.url}\n"){
                addClass(Styles.url)
            }
            text("User: ${connectionController.adminuser}"){
                addClass(Styles.adminuser)

            }
        }
    }
}

    /**
     * Update the display of the current onlinestatus
     * @param status the new status of the onlinestatus
     */
    private fun onlineStatusChanged(status: Boolean){

        onlineStatus.apply {
            text = if (status) "ON" else "OFF"
            fill = if (status) Color.GREEN else Color.RED
        }

    }

}