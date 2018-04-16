/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.faces.generate;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.SourceVersion.RELEASE_8;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.faces.application.ApplicationConfigurationPopulator;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.w3c.dom.Document;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

@SupportedAnnotationTypes("com.sun.faces.generate.PreParseFacesConfig")
@SupportedSourceVersion(RELEASE_8)
public class PreParseFacesConfigProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        
        for (Element element : roundEnv.getElementsAnnotatedWith(PreParseFacesConfig.class)) {

            if (element instanceof TypeElement) {

                TypeElement typeElement = (TypeElement) element;

                PackageElement packageElement = (PackageElement) typeElement.getEnclosingElement();

                try {
                    String className = typeElement.getSimpleName() + "Generated";

                    JavaFileObject fileObject = 
                        processingEnv.getFiler()
                                     .createSourceFile(packageElement.getQualifiedName() + "." + className);
                    
                    MethodSpec.Builder methodBuilder = methodBuilder("populateApplicationConfiguration") 
                        .addAnnotation(Override.class)
                        .returns(void.class)
                        .addModifiers(PUBLIC)
                        .addParameter(Document.class, "toPopulate")
                        .addStatement("String ns = toPopulate.getDocumentElement().getNamespaceURI()")
                        .addStatement("Element faces_configElement = toPopulate.getDocumentElement()");
                    
                    FacesConfigParser parser = new FacesConfigParser();
                    
                    parser.setOnStartElement(e -> {
                        methodBuilder
                            .beginControlFlow("")
                            .addStatement(
                                "$T $L = toPopulate.createElementNS(ns, $S)", 
                                org.w3c.dom.Element.class, 
                                toJavaVar(e.getNodeName()),
                                e.getNodeName());
                    });
                    
                    parser.setOnLeafElementWithText((e, t) -> {
                        methodBuilder.addStatement(
                            "$L.appendChild(toPopulate.createTextNode($S))",
                            toJavaVar(e.getNodeName()),
                            t.getWholeText().trim());
                    });
                    
                    parser.setOnEndElement(e -> {
                        methodBuilder.addStatement(
                            "$L.appendChild($L)",
                            toJavaVar(e.getParentNode().getNodeName()),
                            toJavaVar(e.getNodeName()));
                        
                        methodBuilder.endControlFlow();
                    });
                    
                    parser.parseFromClassPath("com/sun/faces/jsf-ri-runtime.xml");

                    TypeSpec helloWorld = classBuilder(className)
                        .superclass(ApplicationConfigurationPopulator.class)
                        .addModifiers(PUBLIC, FINAL)
                        .addMethod(
                            methodBuilder
                                .build())
                        .build();

                    JavaFile javaFile = JavaFile.builder(packageElement.getQualifiedName().toString(), helloWorld)
                        .build();
                    

                    try (Writer writter = fileObject.openWriter()) {
                        writter.append(javaFile.toString());
                    }

                } catch (final IOException ex) {
                    processingEnv.getMessager().printMessage(Kind.ERROR, ex.getMessage());
                }

            }

        }

        return true;
    }
    
    private static String toJavaVar(String name) {
        return name.replace('-', '_') + "Element";
    }

}
