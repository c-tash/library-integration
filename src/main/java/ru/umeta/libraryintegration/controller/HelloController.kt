package ru.umeta.libraryintegration.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod


@Controller
@RequestMapping("/")
class HelloController {

    @RequestMapping(method = arrayOf(RequestMethod.GET, RequestMethod.HEAD))
    fun printWelcome(model: ModelMap): String {
        model.addAttribute("message", "Hello world!")
        return "hello"
    }

}
