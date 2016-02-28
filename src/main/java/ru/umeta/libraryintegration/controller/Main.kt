package ru.umeta.libraryintegration.controller

import ru.umeta.libraryintegration.service.MainService

/**
 * The Main Rest Controller
 * Created by Kirill Kosolapov (https://github.com/c-tash) on 06.04.2015.
 */

fun main(args: Array<String>) {
    val command = args[0]
    when (command) {
        "-parse" -> MainService.use {
            it.parseDirectory(args[1])
        }
        "-parseInit" -> MainService.use {
            it.parseDirectoryInit(args[1])
        }
        "-find" -> MainService.use {
            it.find()
        }
        "-collect" -> MainService.use {
            it.collect();
        }
    }

}