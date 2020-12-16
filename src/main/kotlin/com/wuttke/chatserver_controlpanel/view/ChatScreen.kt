package com.wuttke.chatserver_controlpanel.view

import com.wuttke.chatserver_controlpanel.app.Styles
import com.wuttke.chatserver_controlpanel.controller.ChatScreenController
import javafx.geometry.*
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import tornadofx.*

/**
 * Screen
 */
class ChatScreen : View() {



    /**
     * Controller related to this View
     */
    private val controller : ChatScreenController by inject()


    override val root = splitpane(Orientation.VERTICAL) {

        addClass(Styles.mainScreen)

            /**
            * Upper side of the screen
            */
            splitpane(Orientation.HORIZONTAL){

               addClass(Styles.upperScreen)


                vbox {
                    maxWidth = 200.0
                    prefWidth = 200.0
                    minWidth = 200.0

                    /**
                     * Title for the connected users
                     */
                    text("Onlineuser"){

                        vboxConstraints {
                            marginTopBottom(8.0)
                            alignment = Pos.CENTER
                            font = Font.font(null,FontWeight.BOLD,14.0)
                        }
                    }
                /**
                 * List of connected users
                 */
                controller.activeChatUsersList = listview(controller.activeChatUsers){

                    addClass(Styles.chatUserScreen)

                    /**
                     * Check for all cells, if they chatuser in it has a name set, if yes set the name as first line, otherwise
                     * set the socketid shortened to 20 chars as first line
                     * 2nd line shows the date, when the user logged in to your chat
                     * 3rd line shows an optional message, that shows how many unread messages you got from this user
                     */

                    cellFormat {

                        padding = Insets(8.0,16.0,8.0,16.0)
                        graphic = vbox {

                                    text {
                                        vboxConstraints {
                                            marginBottom = 8.0
                                        }
                                        addClass(Styles.chatUserName)
                                        text = if (!it.name.isNullOrEmpty()) it.name
                                        else "${it.socketID.subSequence(0,20)}"
                                    }
                                    text("logged in : ${it.loginTime}") {
                                        addClass(Styles.chatUserLogin)

                                    }
                                    if (it.neueNachrichten!=0){
                                        text("${it.neueNachrichten} new messages"){
                                            addClass(Styles.chatUserNewMessage)

                                       }
                                    }
                                    separator(Orientation.HORIZONTAL){
                                        vboxConstraints {
                                            marginTop = 8.0
                                        }
                                    }
                                }
                        /**
                         * When the clicked item differs from the previous selected item ask the server for the chatlog of this user
                         * When there is a message,that there are unread messages remove this message
                         */
                        setOnMouseClicked {
                            controller.sendEnabled.value = true
                            if (selectedItem?.neueNachrichten != 0){
                                selectedItem?.neueNachrichten = 0

                                this@listview.refresh()

                            }
                            if (controller.selectedIndex.value != selectedItem?.socketID) controller.selectedIndex.value = selectedItem?.socketID

                        }
                            }


                }
                    /**
                     * Title for the disconnected users
                     */
                    text("Offlineuser"){

                        vboxConstraints {
                            marginTopBottom(8.0)
                            alignment = Pos.CENTER
                            font = Font.font(null,FontWeight.BOLD,14.0)
                        }
                    }
                    /**
                     * List of disconnected users
                     */
                   controller.offlineChatUsersList =  listview(controller.offlineChatUsers){

                        addClass(Styles.chatUserScreen)

                        /**
                         * Check for all cells, if they chatuser in it has a name set, if yes set the name as first line, otherwise
                         * set the socketid shortened to 20 chars as first line
                         * 2nd line shows the date, when the user disconnected from your website
                         */

                        cellFormat {

                            padding = Insets(8.0,16.0,8.0,16.0)
                            graphic = vbox {

                                text {
                                    vboxConstraints {
                                        marginBottom = 8.0
                                    }
                                    addClass(Styles.chatUserName)
                                    text = if (!it.name.isNullOrEmpty()) it.name
                                    else "${it.socketID.subSequence(0,20)}"
                                }
                                text("logged out : ${it.logoutTime}") {
                                    addClass(Styles.chatUserLogin)

                                }
                                separator(Orientation.HORIZONTAL){
                                    vboxConstraints {
                                        marginTop = 8.0
                                    }
                                }
                            }
                            /**
                             * When the clicked item differs from the previous selected item ask the server for the chatlog of this user
                             */
                            setOnMouseClicked {
                                controller.sendEnabled.value = false
                                if (controller.selectedIndex.value != selectedItem?.socketID) controller.selectedIndex.value = selectedItem?.socketID

                            }
                        }


                    }

                }

                vbox {

                    /**
                     * Title for the chatmessages
                     */
                    text("Chatmessages"){

                        vboxConstraints {
                            marginTopBottom(8.0)
                            alignment = Pos.CENTER
                            font = Font.font(null,FontWeight.BOLD,14.0)
                        }
                    }

                /**
                 * List of chatmessages
                 */
                listview(controller.chatMessages){
                    addClass(Styles.chatMessageScreen)

                    /**
                     * Format each chatmessage
                     */
                    cellFormat {
                        graphic = cache {
                             vbox {

                                textflow {
                                    vboxConstraints {
                                        addClass(Styles.chatMessageText)
                                    }
                                    text(it.nachricht)
                                    }

                                 textflow{

                                    vboxConstraints {
                                        textAlignment = if (it.user_gesendet) TextAlignment.RIGHT
                                        else TextAlignment.LEFT
                                        addClass(Styles.chatMessageTimestamp)
                                        }
                                    text {

                                        text = if (it.user_gesendet){
                                        "empfangen um: "+it.gesendet_um
                                        } else{
                                        "gesendet um: "+it.gesendet_um
                                        }
                                    }


                                }
                                 separator(Orientation.HORIZONTAL)
                            }
                        }
                    }
                }
                }
                /**
                 * Menu for actions related to the selected user
                 */
                gridpane {

                    addClass(Styles.infoScreen)

                    row {
                        /**
                         * Title for the Userinfos
                         */
                        text("Userinfo"){
                            gridpaneConstraints {
                                marginTopBottom(8.0)
                                hAlignment = HPos.CENTER
                                font = Font.font(null,FontWeight.BOLD,14.0)
                            }
                        }
                    }


                    /**
                     * Change the name of the user
                     */
                    row {
                        button ("Change name"){

                            addClass(Styles.changeNameButton)

                            setOnMouseClicked {
                                controller.renameUser()
                            }

                            gridpaneConstraints {

                                marginTop = 10.0
                                hAlignment = HPos.CENTER

                            }

                        }
                    }

                    /**
                     * Add an info for the selected user
                     */
                    row {

                        button("Add info"){

                            addClass(Styles.addInfoButton)

                            setOnMouseClicked {
                                controller.createNewInfo()
                            }

                            gridpaneConstraints {

                                marginTopBottom(10.0)
                                hAlignment = HPos.CENTER

                            }

                        }

                    }

                    /**
                     * Listview to display the added infos of the user
                     */
                    row {

                        listview(controller.userInfo) {

                            gridpaneConstraints {
                                marginRight = 8.0
                            }
                            contextmenu{
                                item("delete").action{
                                    controller.removeInfo(controller.userInfo[selectionModel.selectedIndex].infoID)
                                    controller.userInfo.removeAt(selectionModel.selectedIndex)
                                }
                            }
                            cellFormat {

                                addClass(Styles.infoText)
                                text = it.info

                            }



                        }
                    }

                }
            }

        /**
         * lower side of the screen
         */
        splitpane(Orientation.HORIZONTAL) {

            addClass(Styles.lowerScreen)

            vbox {

                addClass(Styles.controlScreen)


                /**
                 * Display how many users are currently visiting the website
                 */

                text{

                    val textBinding = controller.userCount.stringBinding{ "$it visitors currently"}

                    bind(textBinding)

                    vboxConstraints {
                        addClass(Styles.visitors)
                        margin = Insets(10.0,0.0,0.0,10.0)
                    }

                }

                /**
                 * Display the current status of the connection
                 * Are you connected to a server? Which one? And as who?
                 * Or are you not connected to any server
                 */
                controller.connectionStatus = textflow{

                    text("not connected"){
                       addClass(Styles.unconnected)
                    }
                    vboxConstraints {
                        margin = Insets(10.0,0.0,0.0,10.0)

                    }

                }

                /**
                 * Display if you are currently visible as online for visiting users and is the chat as a result enabled for this website
                 */
                textflow{

                    text("status : "){
                        addClass(Styles.adminStatus)
                    }
                    controller.onlineStatus = text {
                        text = "OFF"
                        addClass(Styles.adminStatus)
                        fill = Color.RED
                    }

                    vboxConstraints {

                        margin = Insets(10.0,0.0,10.0,10.0)

                    }

                }

                /**
                 * Buttons to turn your status to online or offline
                 */
                hbox {

                    /**
                     * Set your visible status for visitors to offline
                     */
                    button("Off") {

                        disableWhen {
                            !controller.connectionController.angemeldet
                        }

                        setOnMouseClicked {
                            controller.connectionController.offlineSchalten()
                        }
                        addClass(Styles.offButton)
                        hboxConstraints {

                            marginLeft = 10.0
                            marginRight = 10.0

                        }

                    }


                    /**
                     * Set your visible status for visitors to online
                     */
                    button("On"){

                        addClass(Styles.onButton)

                        hboxConstraints {

                            marginLeft = 10.0
                            marginRight = 10.0

                        }

                        disableWhen {
                            !controller.connectionController.angemeldet
                        }
                        setOnMouseClicked {
                            controller.connectionController.onlineSchalten()
                        }

                    }
                }
            }

            /**
             * Lower right side of the screen for typing chatmessages and sending them
             */
            gridpane {

                row {

                    textarea {

                        controller.nachrichtenScreen = this
                        addClass(Styles.textInputArea)
                        gridpaneConstraints {

                            hGrow= Priority.ALWAYS
                            margin = Insets(8.0)

                        }
                    }
                }

                /**
                 * Send the typed message to the selected user
                 */
                row {

                    button("send") {
                        setOnMouseClicked {
                            controller.sendMessage()
                        }
                        disableWhen(!controller.sendEnabled.and(controller.connectionController.isOnline))

                        addClass(Styles.sendButton)

                        gridpaneConstraints {

                            marginTop = 10.0
                            marginRight = 20.0
                            hAlignment = HPos.RIGHT

                        }
                    }
                }
            }
        }
    }
}
