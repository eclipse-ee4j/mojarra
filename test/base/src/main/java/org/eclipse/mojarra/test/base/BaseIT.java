/*
 * Copyright (c) Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GPL-2.0 with Classpath-exception-2.0 which
 * is available at https://openjdk.java.net/legal/gplv2+ce.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 or Apache-2.0
 */
package org.eclipse.mojarra.test.base;

import static java.lang.System.getProperty;
import static java.time.Duration.ofSeconds;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;

import java.io.File;
import java.net.URL;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

@ExtendWith(ArquillianExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public abstract class BaseIT {

    private WebDriver browser;

    @ArquillianResource
    private URL baseURL;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        String warFileName = getProperty("war.file.name") + ".war";
        return create(ZipImporter.class, warFileName)
                .importFrom(new File("target/" + warFileName))
                .as(WebArchive.class);
    }

    @BeforeAll
    public void setup() {
        String arquillianBrowser = System.getProperty("arquillian.browser");

        switch (arquillianBrowser) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeDriver chrome = new ChromeDriver(new ChromeOptions().addArguments("--no-sandbox", "--headless"));
                chrome.setLogLevel(Level.INFO);
                browser = chrome;
                break;
            default:
                throw new UnsupportedOperationException("arquillian.browser='" + arquillianBrowser + "' is not yet supported");
        }

        PageFactory.initElements(browser, this);
    }

    @AfterAll
    public void teardown() {
        browser.quit();
    }

    protected void open(String resource) {
        browser.get(baseURL + resource);
    }

    protected String getPageSource() {
        return browser.getPageSource();
    }

    protected void guardAjax(Runnable action) {
        String uuid = UUID.randomUUID().toString();
        executeScript("window.$ajax = true; faces.ajax.addOnEvent(data => { if (data.status == 'complete') window.$ajax = '" + uuid + "'; })");
        action.run();
        waitUntil(() -> executeScript("return window.$ajax == '" + uuid + "' || (!window.$ajax && document.readyState == 'complete');"));
    }

    @SuppressWarnings("unchecked")
    private <T> T executeScript(String script) {
        return (T) ((JavascriptExecutor) browser).executeScript(script);
    }

    private void waitUntil(BooleanSupplier predicate) {
        new WebDriverWait(browser, ofSeconds(5)).until($ -> predicate.getAsBoolean());
    }
}
