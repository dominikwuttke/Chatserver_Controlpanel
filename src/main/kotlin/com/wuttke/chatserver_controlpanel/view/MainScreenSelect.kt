package com.wuttke.chatserver_controlpanel.view

import com.wuttke.chatserver_controlpanel.app.Styles
import com.wuttke.chatserver_controlpanel.controller.MainScreenController
import tornadofx.*

/**
 * The whole screen of the program
 * Other screens are added to this screen
 */
class MainScreenSelect : View("Chatserver Controlpanel") {



    /**
     * Controller to handle things related to the whole application
     */
    private val controller : MainScreenController by inject()


    /**
     * Get a hold of Views which can be added to the screen
     */
    private val chatScreen : ChatScreen by inject()

    init {
        controller.mainView = this
    }

   override val root = vbox {

       addClass(Styles.mainScreen)

        menubar {

            autosize()

            /**
             * Create a menu for login and logout
             */
            menu("Login") {

                item("Sign in"){

                    disableWhen(controller.connectionController.angemeldet)

                    action {

                        controller.anmelden()

                    }

                }

                item("Sign out"){

                    disableWhen(!controller.connectionController.angemeldet)

                    action {

                        controller.abmelden()

                    }

                }

            }

        }

        add(chatScreen)

    }

}
