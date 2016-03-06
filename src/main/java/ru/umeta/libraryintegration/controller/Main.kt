package ru.umeta.libraryintegration.controller

import ru.umeta.libraryintegration.service.MainService
import java.util.*

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
            val start = System.currentTimeMillis();
            it.find()
            println((System.currentTimeMillis() - start)/1000)
        }
        "-collect" -> MainService.use {
            it.collect()
            println(Date())
        }
    }

}