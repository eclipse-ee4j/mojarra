/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.javaee6web.facelets;

import java.io.Serializable;
import java.util.*;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;


@ManagedBean
@SessionScoped
public class BasicViewForIssue4142 implements Serializable {

  private static final long serialVersionUID = -1304416932952283709L;

  private final List<Car> cars;
  private transient HtmlDataTable carsDataTable;

  private final static String[] colors;

  private final static String[] brands;

  static {
    colors = new String[10];
    colors[0] = "Black";
    colors[1] = "White";
    colors[2] = "Green";
    colors[3] = "Red";
    colors[4] = "Blue";
    colors[5] = "Orange";
    colors[6] = "Silver";
    colors[7] = "Yellow";
    colors[8] = "Brown";
    colors[9] = "Maroon";

    brands = new String[10];
    brands[0] = "BMW";
    brands[1] = "Mercedes";
    brands[2] = "Volvo";
    brands[3] = "Audi";
    brands[4] = "Renault";
    brands[5] = "Fiat";
    brands[6] = "Volkswagen";
    brands[7] = "Honda";
    brands[8] = "Jaguar";
    brands[9] = "Ford";
  }

  public BasicViewForIssue4142() {
    // System.out.println("new basicview");
    cars = createCars(10);
  }

  private List<Car> createCars(int size) {
    List<Car> list = new ArrayList<Car>();
    for (int i = 0; i < size; i++) {
      list.add(new Car(getRandomId(), getRandomBrand(), getRandomYear(), getRandomColor(), getRandomPrice(),
          getRandomSoldState()));
    }
    return list;
  }

  private String getRandomId() {
    return UUID.randomUUID().toString().substring(0, 8);
  }

  private int getRandomYear() {
    return (int) (Math.random() * 50 + 1960);
  }

  private String getRandomColor() {
    return colors[(int) (Math.random() * 10)];
  }

  private String getRandomBrand() {
    return brands[(int) (Math.random() * 10)];
  }

  public int getRandomPrice() {
    return (int) (Math.random() * 100000);
  }

  public boolean getRandomSoldState() {
    return (Math.random() > 0.5) ? true : false;
  }

  public List<String> getColors() {
    return Arrays.asList(colors);
  }

  public List<String> getBrands() {
    return Arrays.asList(brands);
  }

  public List<Car> getCars() {
    return cars;
  }

  public HtmlDataTable getCarsDataTable() {
    return carsDataTable;
  }

  public void setCarsDataTable(HtmlDataTable carsDataTable) {
    this.carsDataTable = carsDataTable;
  }

}
