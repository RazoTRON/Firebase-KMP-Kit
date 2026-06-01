package com.firebasekit.messaging

import java.awt.Desktop
import java.net.URI

 fun interface BrowserOpener {
    fun open(uri: URI)
}

internal class SystemBrowserOpener : BrowserOpener {
    override fun open(uri: URI) {
        val desktop = runCatching { Desktop.getDesktop() }
            .getOrElse { error("Open $uri in a browser to finish Firebase Messaging registration.") }

        check(Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE)) {
            "Open $uri in a browser to finish Firebase Messaging registration."
        }

        desktop.browse(uri)
    }
}
