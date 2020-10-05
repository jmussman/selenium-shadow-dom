// IndexController.java
// Copyright © 2020 Joel Mussman. All rights reserved.
//

package com.wonderfulwidgets.examples.selshadowdom.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String def() {

        return "forward:/index";
    }

    @GetMapping("/index")
    public String index() {

        return "index";
    }
}
