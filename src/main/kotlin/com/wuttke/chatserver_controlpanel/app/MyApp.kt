package com.wuttke.chatserver_controlpanel.app

import com.wuttke.chatserver_controlpanel.view.MainScreenSelect
import tornadofx.*



class MyApp: App(MainScreenSelect::class, Styles::class){



    override fun onBeforeShow(view: UIComponent) {
        view.setWindowMinSize(750,750)
        view.setWindowMaxSize(1000,800)
        super.onBeforeShow(view)
    }

}
