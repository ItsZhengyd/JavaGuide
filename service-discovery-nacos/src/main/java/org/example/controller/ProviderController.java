package org.example.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProviderController {

    @RequestMapping(value = "/providerecho/{str}", method = RequestMethod.GET)
    public String echo(@PathVariable String str) {
        System.out.println("providerecho:" + str);
        return "Hello Nacos Discovery " + str;
    }

}
