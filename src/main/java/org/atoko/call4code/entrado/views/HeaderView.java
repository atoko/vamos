package org.atoko.call4code.entrado.views;

import org.springframework.stereotype.Component;

@Component("HeaderView")
public class HeaderView {

    public String render() {
        return "views/clamp.html";
    }
}
