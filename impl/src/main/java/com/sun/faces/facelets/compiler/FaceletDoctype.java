package com.sun.faces.facelets.compiler;

import jakarta.faces.component.Doctype;

public class FaceletDoctype implements Doctype {

    private final String rootElement;
    private final String _public;
    private final String system;

    public FaceletDoctype(String rootElement, String _public, String system) {
        this.rootElement = rootElement;
        this._public = _public;
        this.system = system;
    }

    @Override
    public String getRootElement() {
        return rootElement;
    }

    @Override
    public String getPublic() {
        return _public;
    }

    @Override
    public String getSystem() {
        return system;
    }

}
