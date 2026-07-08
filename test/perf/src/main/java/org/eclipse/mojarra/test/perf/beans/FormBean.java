/*
 * Copyright (c) Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.test.perf.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Backs the flat multi-section forms ({@code form-inputs}, {@code form-inputs-ajax}, {@code form-invalid}), which
 * share a single field body via {@code /WEB-INF/includes/form-fields.xhtml}. The "hero" fields ({@code name}, {@code quantity},
 * {@code price}, …) are kept as direct typed properties — the {@code form-invalid} run keys its rejected values on
 * their ids. The remaining fields live in nested section view-models ({@link Address}, {@link Company}, …), exercising
 * the two-level {@code BeanELResolver} property path that real DTO-backed forms take, across a realistic
 * multi-section component count.
 */
@Named
@ViewScoped
public class FormBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private AppConfig appConfig;

    @Inject
    private UserSession session;

    private String name = "Acme";
    private String description = "Some description";
    private Integer quantity = 1;
    private BigDecimal price = new BigDecimal("9.99");
    private LocalDate date = LocalDate.of(2026, 1, 1);
    private boolean active = true;
    private String country = "NL";
    private String category = "Books";

    private final Address address = new Address();
    private final Company company = new Company();
    private final Contact contact = new Contact();
    private final Payment payment = new Payment();
    private final Preferences preferences = new Preferences();
    private final Shipping shipping = new Shipping();

    public String submit() {
        session.getHits();
        appConfig.getAppName();
        return null;
    }

    // --- Hero fields (invalid-path keys on name/quantity/price) ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // --- Nested section view-models ---

    public Address getAddress() {
        return address;
    }

    public Company getCompany() {
        return company;
    }

    public Contact getContact() {
        return contact;
    }

    public Payment getPayment() {
        return payment;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public Shipping getShipping() {
        return shipping;
    }

    public static class Address implements Serializable {
        private static final long serialVersionUID = 1L;
        private String street = "Main Street";
        private Integer houseNumber = 42;
        private String addition = "B";
        private String postalCode = "1000 AA";
        private String city = "Amsterdam";
        private String state = "Noord-Holland";
        private String region = "West";

        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        public Integer getHouseNumber() { return houseNumber; }
        public void setHouseNumber(Integer houseNumber) { this.houseNumber = houseNumber; }
        public String getAddition() { return addition; }
        public void setAddition(String addition) { this.addition = addition; }
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
    }

    public static class Company implements Serializable {
        private static final long serialVersionUID = 1L;
        private String companyName = "Acme Corp";
        private String department = "Engineering";
        private String jobTitle = "Developer";
        private String vatNumber = "NL123456789B01";
        private Integer employees = 100;
        private String website = "https://acme.example";
        private String industry = "Books";

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
        public String getVatNumber() { return vatNumber; }
        public void setVatNumber(String vatNumber) { this.vatNumber = vatNumber; }
        public Integer getEmployees() { return employees; }
        public void setEmployees(Integer employees) { this.employees = employees; }
        public String getWebsite() { return website; }
        public void setWebsite(String website) { this.website = website; }
        public String getIndustry() { return industry; }
        public void setIndustry(String industry) { this.industry = industry; }
    }

    public static class Contact implements Serializable {
        private static final long serialVersionUID = 1L;
        private String email = "info@acme.example";
        private String phone = "+31 20 1234567";
        private String mobile = "+31 6 12345678";
        private String fax = "+31 20 7654321";
        private String skype = "acme.support";

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
        public String getFax() { return fax; }
        public void setFax(String fax) { this.fax = fax; }
        public String getSkype() { return skype; }
        public void setSkype(String skype) { this.skype = skype; }
    }

    public static class Payment implements Serializable {
        private static final long serialVersionUID = 1L;
        private String cardName = "A. Acme";
        private String cardNumber = "4111111111111111";
        private Integer expiryMonth = 12;
        private Integer expiryYear = 2030;
        private String cvc = "123";
        private String iban = "NL91ABNA0417164300";
        private String currency = "EUR";

        public String getCardName() { return cardName; }
        public void setCardName(String cardName) { this.cardName = cardName; }
        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
        public Integer getExpiryMonth() { return expiryMonth; }
        public void setExpiryMonth(Integer expiryMonth) { this.expiryMonth = expiryMonth; }
        public Integer getExpiryYear() { return expiryYear; }
        public void setExpiryYear(Integer expiryYear) { this.expiryYear = expiryYear; }
        public String getCvc() { return cvc; }
        public void setCvc(String cvc) { this.cvc = cvc; }
        public String getIban() { return iban; }
        public void setIban(String iban) { this.iban = iban; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }

    public static class Preferences implements Serializable {
        private static final long serialVersionUID = 1L;
        private boolean newsletter = true;
        private String language = "NL";
        private String timezone = "Europe/Amsterdam";
        private String theme = "Light";
        private String referral = "search";
        private String comments = "No comments";
        private boolean acceptTerms = true;

        public boolean isNewsletter() { return newsletter; }
        public void setNewsletter(boolean newsletter) { this.newsletter = newsletter; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        public String getReferral() { return referral; }
        public void setReferral(String referral) { this.referral = referral; }
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
        public boolean isAcceptTerms() { return acceptTerms; }
        public void setAcceptTerms(boolean acceptTerms) { this.acceptTerms = acceptTerms; }
    }

    public static class Shipping implements Serializable {
        private static final long serialVersionUID = 1L;
        private String street = "Depot Road";
        private Integer houseNumber = 7;
        private String postalCode = "2000 BB";
        private String city = "Rotterdam";
        private String country = "NL";
        private String method = "Standard";
        private String instructions = "Leave at reception";
        private boolean giftWrap = false;
        private LocalDate deliveryDate = LocalDate.of(2026, 2, 1);

        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        public Integer getHouseNumber() { return houseNumber; }
        public void setHouseNumber(Integer houseNumber) { this.houseNumber = houseNumber; }
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        public String getInstructions() { return instructions; }
        public void setInstructions(String instructions) { this.instructions = instructions; }
        public boolean isGiftWrap() { return giftWrap; }
        public void setGiftWrap(boolean giftWrap) { this.giftWrap = giftWrap; }
        public LocalDate getDeliveryDate() { return deliveryDate; }
        public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }
    }
}
