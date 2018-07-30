package com.sun.faces.test.servlet30.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ApplicationScoped
public class Issue2566Bean {

    @Inject
    private FacesContext facesContext;

    public String getTestResult() {

        StringBuilder result = new StringBuilder();

        ELContext elContext = facesContext.getELContext();

        if (elContext != null) {
            ELResolver resolver = elContext.getELResolver();
            Iterator<FeatureDescriptor> featureDescriptors = resolver.getFeatureDescriptors(elContext, null);
            while (featureDescriptors.hasNext()) {
                result.append("[" + featureDescriptors + "] is ok");
                featureDescriptors.next();
            }
            result.append("\n\nEL Resolver Passed");
        }

        return result.toString();
    }

}
