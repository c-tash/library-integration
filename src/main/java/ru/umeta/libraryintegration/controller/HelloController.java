package ru.umeta.libraryintegration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.umeta.libraryintegration.dao.ProtocolDao;

@Controller
@RequestMapping("/")
public class HelloController {

    @Autowired
    private ProtocolDao protocolDao;

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD})
    public String printWelcome(ModelMap model) {
        model.addAttribute("message", "Hello world!");
        return "hello";
    }




}
