package com.wuttke.chatserver_controlpanel.controller

import com.wuttke.chatserver_controlpanel.socketapi.AdminStatus
import com.wuttke.chatserver_controlpanel.socketapi.Types
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.wuttke.chatserver_controlpanel.app.Styles
import com.wuttke.chatserver_controlpanel.socketapi.AdminScreen
import com.wuttke.chatserver_controlpanel.viewmodel.*
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
import javafx.scene.control.ListView

import okhttp3.*
import tornadofx.*

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.IllegalArgumentException

/**
 * Controller to handle the connection with the server
 */
class ConnectionController : Controller() {

    val gson = Gson()
    private val client = OkHttpClient()
    var webSocket: WebSocket? = null

    private val chatScreenController : ChatScreenController by inject()
    private val mainScreenController : MainScreenController by inject()

    var url : String = ""
    var adminuser : String = ""
    private var adminpass : String = ""
    private var hasFixedLogin : Boolean = false

    private lateinit var serverData : ArrayList<Server>
    private var dirtyServerData = ArrayList<Server>()
    val angemeldet = SimpleBooleanProperty(false)
    val isOnline = SimpleBooleanProperty(false)



    init {
        url         = app.parameters.named.getOrDefault("serverurl","")
        adminuser   = app.parameters.named.getOrDefault("adminuser","")
        adminpass   = app.parameters.named.getOrDefault("adminpass","")

        if (url.isNotEmpty() && adminuser.isNotEmpty() && adminpass.isNotEmpty()) {
           hasFixedLogin = true
        }
        else {
           disableAutoLogin()
        }
    }

    /**
     * disable the autologin and enable the user to enter serverdata and user manually
     * creates a file where the serverdata will be stored when there is no file
     */
    private fun disableAutoLogin(){
        hasFixedLogin = false
        val file = File("serverdata.json")
        if (!file.exists()){
            file.createNewFile()
            file.printWriter().use { it.print("{server:[]}") }
        }
        val reader = FileReader("serverdata.json")
        val mytype = object : TypeToken<List<Server>>(){}.type
        serverData = gson.fromJson(reader,mytype)
    }

    /**
     * Connect with the server
     */
    private fun connect(){
        val logindaten = Credentials.basic(adminuser,adminpass)
        updateServerData()
        try {

            val request = Request.Builder().url(url).addHeader("Authorization",logindaten).build()
            webSocket = client.newWebSocket(request, Listener(this))
        }catch (e : IllegalArgumentException){

            url = ""
            adminpass = ""
            adminuser = ""

            disableAutoLogin()
            e.message?.let { connectionError(it) }
        }
    }

    /**
     * Disconnect from the server
     */
    fun disconnect(){
        webSocket?.close(1000,"Closing on purpose")
        webSocket = null
    }

    /**
     * Set the status as online and enable visitors to chat with you
     */
    fun onlineSchalten(){
        val json = JsonObject()
        json.addProperty(Types.TYPE.name, AdminStatus.ONLINE.name)
        webSocket?.send(json.toString())
    }

    /**
     * Set the status as offline and disable the chat function for your website
     */
    fun offlineSchalten(){
        val json = JsonObject()
        json.addProperty(Types.TYPE.name, AdminStatus.OFFLINE.name)
        webSocket?.send(json.toString())
    }

    /**
     * The Server sent a message. The message will be routed to the controller to which this message is related
     * @param jsonObject The message from the server, which contains a property which determines the controller to which the message is related
     */
    fun socketMessageAuswerten(jsonObject: JsonObject){
        when(jsonObject[Types.VIEW.name].asString){

            AdminScreen.CHATSCREEN.name->{
                chatScreenController.socketMessageReceived(jsonObject)
            }

            AdminScreen.MAINSCREEN.name->{
                when(jsonObject[Types.TYPE.name].asString){
                    AdminStatus.ONLINE.name->{
                        isOnline.value = true
                    }
                    AdminStatus.OFFLINE.name->{
                        isOnline.value = false
                    }
                }
            }
        }
    }

    /**
     * establish a connection to a server
     * when the application is started with Parameters to which the it should connect a connection is established immediately
     * when there were no parameters provided this will trigger dialogs to establish a connection
     */
    fun login(){

        if (hasFixedLogin) connect()
        else {
           copyServerdata()
           selectServer()
        }
    }

    /**
     * save the current status of the serverdata in a copy for temporary use
     */
    private fun copyServerdata(){
        dirtyServerData.apply {
            clear()
            serverData.toCollection(this)
        }
    }

    /**
     * When there were no parameters provided with the start of the application, this will show a dialog
     * with all valid servers in the past and an option to chose from them, or to add a new server
     */
    private fun selectServer(){
        val serverNames = ArrayList<String>().apply {
                serverData.forEach { add(it.serverURL) }
             }
        showLoginDialog("Select a server",serverNames.asObservable(),{
            url = serverData[it].serverURL
            selectUser(it)
        },{
            newServer()
        })
    }

    /**
     * Show a dialog to enter an URL to which a connection should be established
     */
    private fun newServer(){
       copyServerdata()
        mainScreenController.showDialog("Enter ServerURL"){
            url = it
            dirtyServerData.add(Server(it,ArrayList(), ArrayList()))
            newuser()
        }
    }

    /**
     * Show a dialog which displays all the users as whom a connection to the selected was successfull established in the past
     * The dialog has an option to connect as a new user
     * @param serverArrayIndex the index of the selected server in the array of servers
     */
    private fun selectUser(serverArrayIndex: Int){
        val userNames = serverData[serverArrayIndex].user
        showLoginDialog("Select the user as whom to connect",userNames.asObservable(),{
            adminuser = userNames[it]
            adminpass = serverData[serverArrayIndex].pass[it]
            connect()
        },{
            newuser(serverArrayIndex)
        },serverArrayIndex)
    }

    /**
     * Connect as a new user to a server
     * @param serverArrayIndex Index of the entry in of the server in the ServerData. When it is a new Server the Index is -1
     */
    private fun newuser(serverArrayIndex: Int = -1){
        mainScreenController.showDialog("Enter a name"){
            adminuser = it
            if (serverArrayIndex == -1){
                dirtyServerData[dirtyServerData.lastIndex].user.add(it)
            }
            else{
               copyServerdata()
                dirtyServerData[serverArrayIndex].user.add(it)
            }
            enterPassword(serverArrayIndex)

        }
    }

    /**
     * Enter the password for the user, as whom you want to connect to the server
     */
    private fun enterPassword(serverArrayIndex: Int){
        mainScreenController.showDialog("Enter the password"){
            adminpass = it
            if (serverArrayIndex == -1){
                dirtyServerData[dirtyServerData.lastIndex].pass.add(it)
            }
            else{
                dirtyServerData[serverArrayIndex].pass.add(it)
            }
            connect()
        }
    }


    /**
     * Update the array of servers after a connection to a server was made
     */
    private fun updateServerData(){
        if(hasFixedLogin) return
        val fileWriter = FileWriter("serverdata.json",false)
        serverData.apply {
            clear()
            dirtyServerData.toCollection(this)
        }
        fileWriter.use {
            it.write(gson.toJson(serverData).toString())
        }
    }

    /**
     * Shows a Dialog, that a connection to the server wasn't successfull
     * When the connection was done with startup parameters there is an option to enable manual login
     * @param messageTitle The title of the error that was received from the server
     */
    fun connectionError(messageTitle : String){

        mainScreenController.mainView.dialog(messageTitle) {

           prefWidth = 300.0
           hbox {
               if (!hasFixedLogin) button("okay"){
                   prefWidth = 80.0
                   setOnMouseClicked {
                       close()
                   }
                   hboxConstraints {
                       marginLeftRight(110.0)
                   }
               }
               else{
                   button("enable manual login"){
                       prefWidth = 140.0
                       setOnMouseClicked {
                           disableAutoLogin()
                           login()
                           close()
                       }
                   }
                   button("okay"){
                       prefWidth = 80.0
                       setOnMouseClicked {
                           close()
                       }
                       hboxConstraints {
                           marginLeft = 80.0
                       }
                   }
               }
           }
        }
    }

    /**
     * Show a Dialog with items to select from, a cancel button, a button to create a new item and a button to confirm the selection
     * When the list of items is empty, the function which would be triggered when the new button is clicked, will be called immediately
     * @param dialogTitle title to display for this dialog
     * @param values Strings to be displayed
     * @param selectedItem the function which will be called when the current selection is confirmed with the index of the selected item as parameter
     * @param newItem the function which will be called when the button to create a new item is pressed
     */
    private fun showLoginDialog(dialogTitle: String, values : ObservableList<String>, selectedItem:(Int)->Unit, newItem:()->Unit,selectedServer : Int = -1){
        mainScreenController.mainView.dialog(dialogTitle) {

            if (values.isEmpty()){
                newItem()
                close()
            }
            gridpane {
                var listView = ListView<String>()
                row {

                    listView =  listview(values){
                        gridpaneConstraints {

                            prefHeight = (values.size*24+2).toDouble()
                            maxHeight = 98.0
                            addClass(Styles.dialogListView)
                            columnSpan = 2
                            marginTopBottom(16.0)
                        }
                        contextmenu {
                            item("delete").action {
                                if (selectedServer == -1){
                                    dirtyServerData.removeAt(selectionModel.selectedIndex)
                                }
                                else{

                                    dirtyServerData[selectedServer].pass.removeAt(selectionModel.selectedIndex)
                                }
                                values.remove(selectionModel.selectedItem)
                                updateServerData()
                            }
                        }
                    }
                }
                 row {
                     button("close"){
                         gridpaneConstraints {
                             marginLeftRight(35.0)
                             marginTopBottom(8.0)
                             addClass(Styles.dialogButton)
                         }
                         setOnMouseClicked {
                             close()
                         }
                     }
                     button("accept"){
                         gridpaneConstraints {
                             marginLeftRight(35.0)
                             marginTopBottom(8.0)

                             addClass(Styles.dialogButton)
                         }
                         setOnMouseClicked{
                             if (listView.selectionModel.selectedIndex!= -1){
                                 selectedItem(listView.selectionModel.selectedIndex)
                                 close()
                             }

                         }

                     }
                 }

                row {
                    button("new item"){
                        gridpaneConstraints {
                            marginLeftRight(80.0)
                            marginTopBottom(8.0)
                            columnSpan = 2
                            addClass(Styles.dialogNewItem)
                        }
                        setOnMouseClicked{
                            newItem()
                            close()
                        }

                    }
                }
            }
        }?.isResizable = false
    }

    /**
     *  WebSocketListener for the communication with the server
     *  @param connectionController a reference to the ConnectionController
     */
    class Listener(private val connectionController: ConnectionController) : WebSocketListener(){
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            runLater {
                connectionController.angemeldet.value = true
            }
           connectionController.updateServerData()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            println(text)
            val json = connectionController.gson.fromJson(text,JsonObject::class.java)
            runLater {
                connectionController.socketMessageAuswerten(json)
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            println(t.message)

        runLater {
                t.message?.let { connectionController.connectionError(it) }
                connectionController.angemeldet.value = false
                connectionController.isOnline.value = false
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)

            runLater {
                connectionController.angemeldet.value = false
                connectionController.isOnline.value = false
            }
        }
    }
}