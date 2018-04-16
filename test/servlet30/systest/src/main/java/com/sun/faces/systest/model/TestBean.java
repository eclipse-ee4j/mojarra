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

package com.sun.faces.systest.model;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.PropertyResolver;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.faces.event.SystemEventListenerHolder;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * <p>Test JavaBean for managed object creation facility.</p>
 */
public class TestBean implements SystemEventListenerHolder {

    public List<SystemEventListener> getListenersForEventClass(Class<? extends SystemEvent> arg0) {
        return Collections.EMPTY_LIST;
    }
    
public enum Suit { Hearts, Clubs, Diamonds, Spades }
public enum Color { Red, Blue, Green, Orange }

    private Random random;
    private ArrayList newList1= new ArrayList();
    private ArrayList newList2= new ArrayList();
    private ArrayList oneElementList;
    private List newList3 = new ArrayList();

    ServletContext servletContext = null;
    
    public Suit returnSpades() {
        return Suit.Spades;
    }
    
    public Suit returnDiamonds() {
        return Suit.Diamonds;
    }

    public TestBean() {
    random = new Random(4143);
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext extContext = (null != context) ? context.getExternalContext() : null;
        servletContext = (null != extContext) ? (ServletContext) extContext.getContext() : null;
        oneElementList = new ArrayList<String>(1);
        oneElementList.add("hello");
    }


    private boolean booleanProperty = true;


    public boolean getBooleanProperty() {
        return (this.booleanProperty);
    }


    public void setBooleanProperty(boolean booleanProperty) {
        this.booleanProperty = booleanProperty;
    }

    private boolean booleanProperty2 = false;
    public boolean getBooleanProperty2() {
    return booleanProperty2;
    }

    public void setBooleanProperty2(boolean newBooleanProperty2) {
    booleanProperty2 = newBooleanProperty2;
    }

    protected String successOutcome = "/success.xhtml";

    public String getSuccessOutcome() {
        return successOutcome;
    }

    public void setSuccessOutcome(String successOutcome) {
        this.successOutcome = successOutcome;
    }
    protected String failureOutcome = "/failure.xhtml";

    public String getFailureOutcome() {
        return failureOutcome;
    }

    public void setFailureOutcome(String failureOutcome) {
        this.failureOutcome = failureOutcome;
    }



    private byte byteProperty = 12;


    public byte getByteProperty() {
        return (this.byteProperty);
    }


    public void setByteProperty(byte byteProperty) {
        this.byteProperty = byteProperty;
    }


    private double doubleProperty = 123.45;


    public double getDoubleProperty() {
        return (this.doubleProperty);
    }


    public void setDoubleProperty(double doubleProperty) {
        this.doubleProperty = doubleProperty;
    }


    private float floatProperty = (float) 12.34;


    public float getFloatProperty() {
        return (this.floatProperty);
    }


    public void setFloatProperty(float floatProperty) {
        this.floatProperty = floatProperty;
    }


    private int intProperty = 123;


    public int getIntProperty() {
        return (this.intProperty);
    }


    public void setIntProperty(int intProperty) {
        this.intProperty = intProperty;
    }

    private int[] intsProperty = {5, 6, 7};

    public int[] getIntsProperty() {
        return (this.intsProperty);
    }

    public void setIntsProperty(int[] intsProperty) {
        this.intsProperty = intsProperty;
    }

    private long longProperty = 12345;


    public long getLongProperty() {
        return (this.longProperty);
    }


    public void setLongProperty(long longProperty) {
        this.longProperty = longProperty;
    }


    private short shortProperty = 1234;


    public short getShortProperty() {
        return (this.shortProperty);
    }


    public void setShortProperty(short shortProperty) {
        this.shortProperty = shortProperty;
    }


    private String stringProperty = "This is a String property";


    public String getStringProperty() {
        return (this.stringProperty);
    }


    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }


    private UIInput userName = null;


    public UIInput getUserName() {
        return (this.userName);
    }


    public void setUserName(UIInput userName) {
        this.userName = userName;
    }

    private String renderKitInfo = null;
    public String getRenderKitInfo() {
        renderKitInfo = FacesContext.getCurrentInstance().
            getViewRoot().getRenderKitId();
        return renderKitInfo;
    }
    public void setRenderKitInfo(String renderKitInfo) {
        this.renderKitInfo = renderKitInfo;
    }
    private String responseWriterInfo = null;
    public String getResponseWriterInfo() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (null != context) {
            Object responseWriter = context.getResponseWriter();
            if (null != responseWriter) {
                responseWriterInfo = responseWriter.getClass().getName();
            }
        }
        return responseWriterInfo;
    }
    public void setResponseWriterInfo(String responseWriterInfo) {
        this.responseWriterInfo = responseWriterInfo;
    }

    private Object bean = null;


    public Object getBean() {
        return (this.bean);
    }


    public void setBean(Object bean) {
        this.bean = bean;
    }

    public List selectList = null;

    public List getSelectList() {
    if (null == selectList) {
        selectList = new ArrayList();
        selectList.add(new SelectItem("one", "one", "one"));
        selectList.add(new SelectItem("two", "two", "two"));
        selectList.add(new SelectItem("three", "three", "three"));
    }
    return selectList;
    }

    public void setSelectList(List newSelectList) {
    selectList = newSelectList;
    }

    protected String selection = null;

    public String getSelection() {
    return selection;
    }

    public void setSelection(String newSelection) {
    selection = newSelection;
    }

    protected String [] multiSelection;
    public String [] getMultiSelection() {
    return multiSelection;
    }

    public void setMultiSelection(String [] newMultiSelection) {
    multiSelection = newMultiSelection;
    }
    
    public Object getNullProperty() {
        return null;
    }

    public ArrayList getOneElementList() {
        return oneElementList;
    }

    public void setOneElementList(ArrayList oneElementList) {
        this.oneElementList = oneElementList;
    }

    public void valueChanged(ValueChangeEvent event)
        throws AbortProcessingException {
    String [] values = (String []) event.getNewValue();
    if (null == values) {
        valueChangeMessage = "";
    }
    else {
        valueChangeMessage = "value changed, new values: ";
        for (int i = 0; i < values.length; i++) {
        valueChangeMessage = valueChangeMessage + " " + values[i];
        }
    }
    }

    protected String valueChangeMessage;
    public String getValueChangeMessage() {
    return valueChangeMessage;
    }

    public void setValueChangeMessage(String newValueChangeMessage) {
    valueChangeMessage = newValueChangeMessage;
    }

    public List getNondeterministicSelectList() {
    ArrayList list = new ArrayList(3);
    String str = new String((new Float(random.nextFloat())).toString());
    list.add(new SelectItem(str, str, str));
    str = new String((new Float(random.nextFloat())).toString());
    list.add(new SelectItem(str, str, str));
    str = new String((new Float(random.nextFloat())).toString());
    list.add(new SelectItem(str, str, str));
    return list;
    }

    public void setNondeterministicSelectList(List newNondeterministicSelectList) {
    }

    public void addComponentToTree(ActionEvent action) {
    HtmlOutputText output = new HtmlOutputText();
    output.setValue("<p>==new output==</p>");
    output.setEscape(false);

    FacesContext ctx = FacesContext.getCurrentInstance();
    UIComponent group = ctx.getViewRoot().findComponent("form" + UINamingContainer.getSeparatorChar(ctx) +  "addHere");
    group.getChildren().add(output);

    }

    /*
     * replace the propertyResolver with one that does our bidding for
     * this test.
     */

    public void replacePropertyResolver(ActionEvent action) {
    FacesContext context = FacesContext.getCurrentInstance();
    Application app = context.getApplication();

    // see if we need to take action-
    if (null == context.getExternalContext().getSessionMap().get("systest.replacePropertyResolver")) {
        final PropertyResolver oldProp = app.getPropertyResolver();
        PropertyResolver
        newProp = new PropertyResolver() {
            public Object getValue(Object base, Object property)
            throws EvaluationException, PropertyNotFoundException {
            return oldProp.getValue(base, property);
            }

            public Object getValue(Object base, int index)
            throws EvaluationException, PropertyNotFoundException {
            return oldProp.getValue(base, index);
            }

            public void setValue(Object base, Object property, Object value)
            throws EvaluationException, PropertyNotFoundException {
            TestBean.this.setValueChangeMessage("setValue() called");
            oldProp.setValue(base, property, value);
            }

            public void setValue(Object base, int index, Object value)
            throws EvaluationException, PropertyNotFoundException {
            TestBean.this.setValueChangeMessage("setValue() called");
            oldProp.setValue(base, index, value);
            }

            public boolean isReadOnly(Object base, Object property)
            throws EvaluationException, PropertyNotFoundException {
            return oldProp.isReadOnly(base, property);
            }

            public boolean isReadOnly(Object base, int index)
            throws EvaluationException, PropertyNotFoundException {
            return oldProp.isReadOnly(base, index);
            }

            public Class getType(Object base, Object property)
            throws EvaluationException, PropertyNotFoundException {
            return oldProp.getType(base, property);
            }

            public Class getType(Object base, int index)
            throws EvaluationException, PropertyNotFoundException {
            return oldProp.getType(base, index);
            }

        };
        app.setPropertyResolver(newProp);
        context.getExternalContext().getSessionMap().put("systest.replacePropertyResolver", oldProp);
    }
    }



    /*
     * restore the original PropertyResolver.
     */

    public void restorePropertyResolver(ActionEvent action) {
    FacesContext context = FacesContext.getCurrentInstance();
    Application app = context.getApplication();
    PropertyResolver oldProp = null;

    // see if we need to take action-
    if (null != (oldProp = (PropertyResolver) context.getExternalContext().getSessionMap().get("systest.replacePropertyResolver"))) {
        app.setPropertyResolver(oldProp);
        context.getExternalContext().getSessionMap().remove("systest.replacePropertyResolver");
        setValueChangeMessage(null);

    }
    }

    protected HtmlCommandButton boundButton = new HtmlCommandButton();
    public HtmlCommandButton getBoundButton() {
    if (null != boundButton) {
        boundButton.setValue("button label");
    }
    return boundButton;
    }

    public void setBoundButton(HtmlCommandButton newBoundButton) {
    boundButton = newBoundButton;
    }

    public String getFactoryPrintout() {
        String result = "";
        String[] factoryNames = {
              FactoryFinder.APPLICATION_FACTORY,
              FactoryFinder.FACES_CONTEXT_FACTORY,
              FactoryFinder.LIFECYCLE_FACTORY,
              FactoryFinder.RENDER_KIT_FACTORY
        };
        for (int i = 0; i < factoryNames.length; i++) {
            String tmp = FactoryFinder.getFactory(factoryNames[i]).toString();
            if (tmp.startsWith("com.sun.faces")) {
                int idx = tmp.lastIndexOf('.');
                tmp = tmp.substring(idx + 1);
                idx = tmp.indexOf('@');
                tmp = tmp.substring(0, idx);
                result += tmp + ' ';
            } else {
                result += FactoryFinder.getFactory(factoryNames[i]).toString()
                          + ' ';
            }
        }
        return result;
    }

    /**
     * Holds value of property setPropertyTarget.
     */
    private String setPropertyTarget = "default value";

    /**
     * Getter for property setPropertyTarget.
     * @return Value of property setPropertyTarget.
     */
    public String getSetPropertyTarget() {

        return this.setPropertyTarget;
    }

    /**
     * Setter for property setPropertyTarget.
     * @param setPropertyTarget New value of property setPropertyTarget.
     */
    public void setSetPropertyTarget(String setPropertyTarget) {

        this.setPropertyTarget = setPropertyTarget;
    }

    /**
     * Holds value of property counter.
     */
    private String counter = "0";
    private int counterInt = 0;

    /**
     * Getter for property counter.
     * @return Value of property counter.
     */
    public String getCounter() {
        counter = Integer.valueOf(counterInt++).toString();
        return counter;
    }

    /**
     * Holds value of property validatorMessage.
     */
    private String validatorMessage = "Validator Message Expression";

    /**
     * Getter for property validatorMessage.
     * @return Value of property validatorMessage.
     */
    public String getValidatorMessage() {

        return this.validatorMessage;
    }

    /**
     * Holds value of property converterMessage.
     */
    private String converterMessage = "Converter Message Expression";

    /**
     * Getter for property converterMessage.
     * @return Value of property converterMessage.
     */
    public String getConverterMessage() {

        return this.converterMessage;
    }

    public ArrayList getNewList1() {
        return newList1;
    }

    public ArrayList getNewList2() {
        return newList2;
    }

   public void valueChange1(ValueChangeEvent vce) {
        String newValue = vce.getNewValue().toString();
        if (newList1.size() == 3){
            newList1.clear();
        }
        newList1.add(newValue);
   }
   
   public void valueChange0() {
       FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("valueChange0Called", 
               "true");
   }
   
    public void actionListener0() {
       FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("actionListener0Called", 
               "true");
   }


   public void valueChange2(ValueChangeEvent vce) {
        String newValue = vce.getNewValue().toString();
        if (newList2.size() == 3){
            newList2.clear();
        }
        newList2.add(newValue);
   }

    private Integer selectedValue = new Integer(2);

    public Integer getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(Integer selectedValue) {
        this.selectedValue = selectedValue;
    }

    public SelectItem[] getMySelectItems(){
        return new SelectItem[]{
            new SelectItem(new Integer(1),"1"),
            new SelectItem(new Integer(2),"2"),
            new SelectItem(new Integer(3),"3")
        };
    }

    private int intVal = 3;
    public int getInt() {
        return intVal;
    }


    public void setInt(int newIntVal) {
        intVal = newIntVal;
    }


    @PostConstruct
    public void postConstruct() {
        setPostConstructCalled(true);
        FacesContext context = FacesContext.getCurrentInstance();
        context.getApplication().publishEvent(context, TestBeanPostConstructEvent.class, this);
    }

    @PreDestroy
    public void preDestroy() {
        setPreDestroyCalled(true);
        FacesContext context = FacesContext.getCurrentInstance();
        if (null != context) {
            context.getApplication().publishEvent(context, TestBeanPreDestroyEvent.class, this);
        }
    }

    /**
     * Holds value of property postConstructCalled.
     */
    private boolean postConstructCalled  = false;

    /**
     * Getter for property postConstructCalled.
     * @return Value of property postConstructCalled.
     */
    public boolean isPostConstructCalled() {

        return this.postConstructCalled;
    }

    /**
     * Setter for property postConstructCalled.
     * @param postConstructCalled New value of property postConstructCalled.
     */
    public void setPostConstructCalled(boolean postConstructCalled) {

        this.postConstructCalled = postConstructCalled;
        appendStatusMessage("bean: " + getStringProperty() +
                            " postConstructCalled: " + postConstructCalled);

    }

    /**
     * Holds value of property preDestroyCalled.
     */
    private boolean preDestroyCalled  = false;

    /**
     * Getter for property preDestroyCalled.
     * @return Value of property preDestroyCalled.
     */
    public boolean isPreDestroyCalled() {

        return this.preDestroyCalled;
    }

    /**
     * Setter for property preDestroyCalled.
     * @param preDestroyCalled New value of property preDestroyCalled.
     */
    public void setPreDestroyCalled(boolean preDestroyCalled) {
        this.preDestroyCalled = preDestroyCalled;
        appendStatusMessage("bean: " + getStringProperty() +
                            " preDestroyCalled: " + preDestroyCalled);
    }

    public String invalidateSession() {
        ((HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true)).invalidate();
        return null;
    }

    public String removeRequestBean() {
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().remove("requestBean");
        return null;
    }

    public String removeRequestBean2() {
        HttpServletRequest request = (HttpServletRequest)
              FacesContext.getCurrentInstance().getExternalContext().getRequest();
        request.removeAttribute("requestBean");
        return null;
    }

    public String replaceRequestBean() {
        HttpServletRequest request = (HttpServletRequest)
            FacesContext.getCurrentInstance().getExternalContext().getRequest();
        request.setAttribute("requestBean", new TestBean());
        return null;
    }

    public String replaceRequestBean2() {
        HttpServletRequest request = (HttpServletRequest)
            FacesContext.getCurrentInstance().getExternalContext().getRequest();
        Object oldValue = request.getAttribute("requestBean");
        request.setAttribute("requestBean", oldValue);
        return null;
    }

    public String removeSessionBean() {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("sessionBean");
        return null;
    }

    public String removeSessionBean2() {
        HttpSession request = (HttpSession)
              FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        request.removeAttribute("sessionBean");
        return null;
    }

    public String replaceSessionBean() {
        HttpSession session = (HttpSession)
            FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        session.setAttribute("sessionBean", new TestBean());
        return null;
    }

    public String replaceSessionBean2() {
        HttpSession session = (HttpSession)
            FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        Object oldValue = session.getAttribute("sessionBean");
        session.setAttribute("sessionBean", oldValue);
        return null;
    }

    public String removeApplicationBean() {
        FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().remove("applicationBean");
        return null;
    }

    public String removeApplicationBean2() {
        ServletContext request = (ServletContext)
              FacesContext.getCurrentInstance().getExternalContext().getContext();
        request.removeAttribute("applicationBean");
        return null;
    }

    public String replaceApplicationBean() {
        ServletContext application = (ServletContext)
            FacesContext.getCurrentInstance().getExternalContext().getContext();
        application.setAttribute("applicationBean", new TestBean());
        return null;
    }

    public String replaceApplicationBean2() {
        ServletContext application = (ServletContext)
            FacesContext.getCurrentInstance().getExternalContext().getContext();
        Object oldValue = application.getAttribute("applicationBean");
        application.setAttribute("applicationBean", oldValue);
        return null;
    }

    public String clearRequestMap() {
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().clear();
        return null;
    }

    public String clearRequestMapTwice() {
        clearRequestMap();
        clearRequestMap();
        return null;
    }

    public String clearSessionMap() {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().clear();
        return null;
    }

    public String clearSessionMapTwice() {
        clearSessionMap();
        clearSessionMap();
        return null;
    }

    public void appendStatusMessage(String message) {
        if (null == servletContext) {
            return;
        }
        String oldMessage = (String) servletContext.getAttribute("previousRequestStatus");
        oldMessage = (null != oldMessage) ? oldMessage + "\n": "";
        message = (null != message) ? message : "";
        oldMessage = oldMessage + message;
        servletContext.setAttribute("previousRequestStatus", oldMessage);

    }

    public String getAppendRequestMarker() {
        appendStatusMessage("-----------------");
        return "";
    }

    public String clearStatusMessage() {
        if (null != servletContext) {
            servletContext.removeAttribute("previousRequestStatus");
        }
        return null;
    }
    
    public Map getSelectItems() {
        Map<String,SpecialBean> map = new HashMap<String,SpecialBean>();
        map.put("key1", new SpecialBean("value1"));
        map.put("key2", new SpecialBean("value2"));
        map.put("key3", new SpecialBean("value3"));
        return map;        
    }
    
    private SpecialBean special;
    public void setSpecialModel(SpecialBean special) {
        this.special = special;        
    }
    
    public SpecialBean getSpecialModel() {
        return special;
    }

    /**
     * Holds value of property suit.
     */
    private Suit suit;

    /**
     * Getter for property suit.
     * @return Value of property suit.
     */
    public Suit getSuit() {
        return this.suit;
    }

    /**
     * Setter for property suit.
     * @param suit New value of property suit.
     */
    public void setSuit(Suit suit) {
        this.suit = suit;
    }

    /**
     * Holds value of property referencedSuit.
     */
    private Suit referencedSuit;

    /**
     * Getter for property referencedSuit.
     * @return Value of property referencedSuit.
     */
    public Suit getReferencedSuit() {
        return this.referencedSuit;
    }

    // taken from TCK facesResourceBundleResolverGetTypeTest
    public String getResourceBundleType() {
        FacesContext context = FacesContext.getCurrentInstance();
        ELContext elContext = context.getELContext();
        ELResolver elResolver = elContext.getELResolver();
        Class type = elResolver.getType(elContext, null, "resourceBundle03");

        return type.toString();
    }

    // taken from TCK facesResourceBundleResolverFeatureDescriptorTest
    public String getFeatureDescriptorCorrectness() {
        StringBuilder builder = new StringBuilder();

        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        ELResolver resolver = elContext.getELResolver();
        boolean fd_Found = false;

        // Setup golden FeatureDescriptor.
        FeatureDescriptor controlDesc = new FeatureDescriptor();
        controlDesc.setValue("resolvable", Boolean.TRUE);
        controlDesc.setValue("type", ResourceBundle.class);
        controlDesc.setName("resourceBundle03");
        controlDesc.setDisplayName("simple");
        controlDesc.setExpert(false);
        controlDesc.setHidden(false);
        controlDesc.setPreferred(true);
        controlDesc.setShortDescription("");

        builder.append("<h1>getFeatureDescriptors output</h1>\n");
        for (Iterator i = resolver.getFeatureDescriptors(elContext, null);
                i.hasNext();) {
            FeatureDescriptor test = (FeatureDescriptor) i.next();
            builder.append("<p>Name: ").append(test.getName()).
                    append(" displayName: ").append(test.getDisplayName()).
                    append("</p>\n");
        }

        return builder.toString();
    }

    /**
     * Setter for property referencedSuit.
     * @param referencedSuit New value of property referencedSuit.
     */
    public void setReferencedSuit(Suit referencedSuit) {
        this.referencedSuit = referencedSuit;
    }

    /**
     * Holds value of property color.
     */
    private Color color;

    /**
     * Getter for property color.
     * @return Value of property color.
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * Setter for property color.
     * @param color New value of property color.
     */
    public void setColor(Color color) {
        this.color = color;
    }

    public String emptyStringAction() {
        return null;
    }

    public String getIsCompositeComponentValue() {
        String result = null;

        UIComponent currentComposite = UIComponent.getCurrentCompositeComponent(FacesContext.getCurrentInstance());
        result = "" + UIComponent.isCompositeComponent(currentComposite);

        return result;
    }

    public String getCurrentTimeMillis() {
        return "" + System.currentTimeMillis();
    }

    private Integer idcounter = new Integer(0);
 
    public List getNewList3() {
        return newList3;
    }

    public void setNewList3(List newList3) {
        this.newList3 = newList3;
    }

    public String addNewLineBeginning() {
        newList3.add(0, "ID" + idcounter.toString());
        idcounter++;
        return null;
    }

    public String addNewLineEnd() {
        newList3.add("ID" + idcounter.toString());
        idcounter++;
        return null;
    }



}
