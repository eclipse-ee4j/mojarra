package com.sun.faces.test.servlet50.selectitemgroups;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

public class Category {

    private String name;
    private List<Product> products = new ArrayList<>();

    public Category() {
        //
    }

    public Category(String name, Product... products) {
        this.name = name;
        this.products = asList(products);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Product> getProducts() {
        return products;
    }

}