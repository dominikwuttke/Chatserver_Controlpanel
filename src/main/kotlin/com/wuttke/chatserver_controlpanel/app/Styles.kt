package com.wuttke.chatserver_controlpanel.app

import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {



        val chatMessageScreen by cssclass()
        val chatMessageText by cssclass()
        val chatMessageTimestamp by cssclass()

        val chatUserScreen by cssclass()
        val chatUserName by cssclass()
        val chatUserLogin by cssclass()
        val chatUserNewMessage by cssclass()

        val infoScreen by cssclass()
        val changeNameButton by cssclass()
        val addInfoButton by cssclass()
        val infoText by cssclass()

        val controlScreen by cssclass()
        val visitors by cssclass()
        val connected by cssclass()
        val unconnected by cssclass()
        val url by cssclass()
        val adminuser by cssclass()
        val adminStatus by cssclass()
        val adminOnline by cssclass()
        val adminOffline by cssclass()
        val offButton by cssclass()
        val onButton by cssclass()

        val sendButton by cssclass()
        val textInputArea by cssclass()

        val mainScreen by cssclass()
        val upperScreen by cssclass()
        val lowerScreen by cssclass()

        val dialogButton by cssclass()
        val dialogTextArea by cssclass()
        val dialogTextField by cssclass()
        val dialogListView by cssclass()
        val dialogNewItem by cssclass()
    }

    init {



        //#region Mainscreens

        mainScreen{
            minHeight = 750.px
            maxHeight = 750.px
            prefHeight = 750.px

            prefWidth = 750.px
        }

        upperScreen{
            minHeight = 400.px
            maxHeight = 400.px
            prefHeight = 400.px

        }

        lowerScreen{
            minHeight = 250.px
            prefHeight = 250.px
        }

        //#endregion

        //#region Chatusers

        chatUserScreen{
            maxWidth = 200.px
            prefWidth = 200.px
            minWidth = 200.px
            prefHeight = 180.px
        }

        chatUserName{
            fontSize = 1.15.em

        }

        chatUserLogin{
            fontSize = 1.0.em
        }

        chatUserNewMessage{
            fontSize = 1.0.em
            fontWeight =  FontWeight.BOLD
        }

        //#endregion

        //# region ChatMessages

        chatMessageScreen{

        }

        chatMessageText{
            fontSize = 1.15.em
            wrapText = true
            padding = box(8.px)
            prefWidth = 200.px

        }

        chatMessageTimestamp{
            fontSize = 1.25.em
            fontWeight = FontWeight.BOLD
            padding = box(8.px,16.px)
        }

        //#endregion

        //#region UserInfos

        infoScreen{
            prefWidth = 200.px
            minWidth = 200.px
            maxWidth = 200.px
        }

        changeNameButton{
            prefWidth = 120.px
            maxWidth = 120.px
            minWidth = 120.px
            fontSize = 1.15.em
        }

        addInfoButton{
            fontSize = 1.15.em
            prefWidth = 120.px
            maxWidth = 120.px
            minWidth = 120.px
        }

        infoText{
            fontSize = 1.15.em
            prefWidth = 100.px
            wrapText = true
            padding = box(Dimension(8.0,Dimension.LinearUnits.px))
        }

        //#endregion

        //#region Controlls

        controlScreen{
            prefWidth = 200.px
            minWidth = 200.px
            maxWidth = 200.px
        }

        visitors{
            wrapText = true
            fontSize = 20.px
        }

        connected{
            fontSize = 20.px
            wrapText = true
            fill = Color.GREEN
        }

        unconnected{
            fontSize = 20.px
            wrapText = true
            fill = Color.RED
        }

        url{
            underline = true
            fontSize = 16.px
        }

        adminuser{
            fontSize = 20.px
        }

        adminStatus{
            fontSize = 20.px
        }

        offButton{
            prefWidth = 80.px
            minWidth = 80.px
            maxWidth = 80.px
            fontSize = 20.px
            textFill = Color.RED
        }

        onButton{
            prefWidth = 80.px
            minWidth = 80.px
            maxWidth = 80.px
            fontSize = 20.px
            textFill = Color.GREEN
        }

        //#endregion

        //#region AdminChat

        textInputArea{
            fontSize = 16.px
            prefHeight = 200.px
            minHeight = 200.px
        }

        sendButton{
            fontSize = 16.px
        }

        //endregion

        //#region Dialog

        dialogTextArea{
            prefWidth = 300.px
            minWidth = 300.px
            prefHeight = 120.px
            minHeight = 120.px
        }

        dialogTextField{
            prefWidth = 300.px
            minWidth = 300.px
        }

        dialogButton{
            prefWidth = 80.px
            minWidth = 80.px
            fontSize = 1.15.em
        }

        dialogListView{
            prefWidth = 300.px
            minWidth = 300.px
            wrapText = true
        }

        dialogNewItem{
            prefWidth = 140.px
            minWidth = 140.px
            fontSize = 1.5.em
        }
        //endregion

    }
}