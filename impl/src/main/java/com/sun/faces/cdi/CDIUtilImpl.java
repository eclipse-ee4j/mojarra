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

package com.sun.faces.cdi;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InjectionTargetFactory;
import javax.enterprise.util.AnnotationLiteral;

public class CDIUtilImpl implements Serializable, CDIUtil {
    
    private static final long serialVersionUID = -8101770583567814803L;
    
    public CDIUtilImpl() {
        
    }
    
    @Override
    public Bean createHelperBean(BeanManager beanManager, Class beanClass) {
       BeanWrapper result = null;
       
       AnnotatedType annotatedType = beanManager.createAnnotatedType(
               beanClass );
       
       InjectionTargetFactory factory = beanManager.getInjectionTargetFactory(annotatedType);
       
       result = new BeanWrapper(beanClass);
       //use this to create the class and inject dependencies
       final InjectionTarget injectionTarget =
               factory.createInjectionTarget(result);
       result.setInjectionTarget(injectionTarget);
       
       return result;
   }
   
   
   private static class BeanWrapper implements Bean {
       private Class beanClass;
       private InjectionTarget injectionTarget = null;
       
       public BeanWrapper( Class beanClass) {
           this.beanClass = beanClass;
           
       }
       private void setInjectionTarget(InjectionTarget injectionTarget) {
           this.injectionTarget = injectionTarget;
       }
       
       @Override
       public Class<?> getBeanClass() {
           return beanClass;
       }
       
       @Override
       public Set<InjectionPoint> getInjectionPoints() {
           return injectionTarget.getInjectionPoints();
       }
       
       @Override
       public String getName() {
           return null;
       }
       
       @Override
       public Set<Annotation> getQualifiers() {
           Set<Annotation> qualifiers = new HashSet<>();
           qualifiers.add( new DefaultAnnotationLiteral());
           qualifiers.add( new AnyAnnotationLiteral());
           return qualifiers;
       }
       
       public static class DefaultAnnotationLiteral extends AnnotationLiteral<Default> {
           private static final long serialVersionUID = -9065007202240742004L;           
           
       }
       
       public static class AnyAnnotationLiteral extends AnnotationLiteral<Any> {
           private static final long serialVersionUID = -4700109250603725375L;
       }
       
       @Override
       public Class<? extends Annotation> getScope() {
           return Dependent.class;
       }
       
       @Override
       public Set<Class<? extends Annotation>> getStereotypes() {
           return Collections.emptySet();
       }
       
       @Override
       public Set<Type> getTypes() {
           Set<Type> types = new HashSet<>();
           types.add( beanClass );
           types.add( Object.class );
           return types;
       }
       
       @Override
       public boolean isAlternative() {
           return false;
       }
       
       @Override
       public boolean isNullable() {
           return false;
       }
       
       @Override
       public Object create( CreationalContext ctx ) {
           Object instance = injectionTarget.produce( ctx );
           injectionTarget.inject( instance, ctx );
           injectionTarget.postConstruct( instance );
           return instance;
       }
       
       @Override
       public void destroy( Object instance, CreationalContext ctx ) {
           injectionTarget.preDestroy( instance );
           injectionTarget.dispose( instance );
           ctx.release();
       }
   }   
    
}
