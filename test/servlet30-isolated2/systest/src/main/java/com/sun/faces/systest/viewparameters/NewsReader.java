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

package com.sun.faces.systest.viewparameters;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.bean.ManagedProperty;
import javax.enterprise.context.RequestScoped;

@RequestScoped
@Named
public class NewsReader {

    private FacesContext facesContext;

    @ManagedProperty("#{newsIndex}")
    private NewsIndex newsIndex;

    private List<NewsStory> stories;

    private NewsStory selectedStory;

    private Long selectedStoryId;

    @PostConstruct
    public void postConstruct() {
        facesContext = FacesContext.getCurrentInstance();
        stories = new ArrayList<NewsStory>(newsIndex.getEntries().values());
    }

    public void loadStory() {
        if (!facesContext.isValidationFailed()) {
            NewsStory story = newsIndex.getStory(selectedStoryId);
            if (story != null) {
                selectedStory = story;
                return;
            }

            facesContext.addMessage(null, new FacesMessage("The headline you requested does not exist."));
        }

        // facesContext.getFlash().setKeepMessages(true); // only needed if navigation case is a redirect
        facesContext.getApplication().getNavigationHandler().handleNavigation(facesContext, null, "/viewParameters/page01");
        // we would like the following instead
        // facesContext.fireNavigation("home");
    }

    public List<NewsStory> getStories() {
        return stories;
    }

    public NewsStory getSelectedStory() {
        return selectedStory;
    }

    public Long getSelectedStoryId() {
        return selectedStoryId;
    }

    public void setSelectedStoryId(Long storyId) {
        this.selectedStoryId = storyId;
    }

    // Injected Properties

    public void setNewsIndex(NewsIndex newsIndex) {
        this.newsIndex = newsIndex;
    }

}
