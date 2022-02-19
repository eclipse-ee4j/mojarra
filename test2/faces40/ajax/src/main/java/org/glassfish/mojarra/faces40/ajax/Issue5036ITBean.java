package org.glassfish.mojarra.faces40.ajax;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class Issue5036ITBean {

    private String hex;
    private String output;
    
    public void submit() {
        char c = (char) Integer.parseInt(hex, 16);
        output = "Output" + c;
    }
    
    public String getHex() {
        return hex;
    }
    
    public void setHex(String hex) {
        this.hex = hex;
    }

    public String getOutput() {
        return output;
    }

}
