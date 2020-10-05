// SelShadowDomApplication.java
// Copyright © 2020 Joel Mussman. All rights reserved.
//

package com.wonderfulwidgets.examples.selshadowdom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude={SecurityAutoConfiguration.class})
public class SelShadowDomApplication {

    public static void main(String[] args) {
        SpringApplication.run(SelShadowDomApplication.class, args);
    }
}
