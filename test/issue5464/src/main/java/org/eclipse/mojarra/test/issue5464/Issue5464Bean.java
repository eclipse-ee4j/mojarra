package org.eclipse.mojarra.test.issue5464;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class Issue5464Bean {

    private String input;
    private String output;

    public void submit() {
        
        System.out.println("====> retrieved input " + input);
        output = "Result: " + input;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }
}
