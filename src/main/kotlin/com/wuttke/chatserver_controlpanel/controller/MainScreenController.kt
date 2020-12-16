package com.wuttke.chatserver_controlpanel.controller

import com.wuttke.chatserver_controlpanel.app.Styles
import javafx.scene.control.TextInputControl
import tornadofx.*


/**
 * Controller which refers to the screen of the whole application.
 * It transfers the userinput to the connectionController
 * @see ConnectionController
 */
class MainScreenController : Controller(){


    var mainView             : View by singleAssign()
    val connectionController : ConnectionController by inject()

    /**
     * Userinput to connect to the server
     */
    fun anmelden(){

        connectionController.login()

    }

    /**
     * Userinput to disconnecto from the server
     */
    fun abmelden(){

        connectionController.disconnect()

    }

    /**
     * create a new dialog with a single line or a textarea to enter a text, a cancel button to close the dialog and an accept button
     *
     * @param dialogTitle The title which is displayed for this dialog
     * @param isSingleLine Does the dialog have a single line to enter a String or does it have a textarea for bigger texts
     * @param accept What happens with the String that has been entered when you press accept
     */
    fun showDialog(dialogTitle:String,isSingleLine: Boolean = true,accept:(String)-> Unit) {

        mainView.dialog(dialogTitle){

            var textArea : TextInputControl by singleAssign()
            gridpane {
                row {
                    textArea = if (!isSingleLine)textarea {
                        gridpaneConstraints {
                            addClass(Styles.dialogTextArea)
                            columnSpan = 2
                            marginTopBottom(16.0)
                        }
                    }
                    else textfield {
                        gridpaneConstraints {

                            addClass(Styles.dialogTextField)
                            columnSpan = 2
                            marginTopBottom(16.0)
                        }
                    }
                }
                row {
                    button("cancel"){
                        setOnMouseClicked {
                            close()
                        }
                        gridpaneConstraints {
                            addClass(Styles.dialogButton)
                            marginLeftRight(35.0)
                        }
                    }
                    button("accept"){
                        setOnMouseClicked {
                            accept(textArea.text)
                            close()
                        }
                        gridpaneConstraints {
                            addClass(Styles.dialogButton)
                            marginLeftRight(35.0)
                        }
                    }
                }
            }
        }?.isResizable = false
    }

}