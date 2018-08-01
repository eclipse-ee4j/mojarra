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

package com.sun.faces.test.servlet30.faceletsViewAction;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.annotation.ManagedProperty;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;


@Named
@RequestScoped
public class NewsReader {

    private FacesContext facesContext;

    @Inject
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

    }

    public String goToPage01IfValidationFailed() {
        if (facesContext.isValidationFailed()) {
            return "/page01";
        }
        return null;
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

    public boolean isMissingStoryId() {
        return null == selectedStoryId;
    }

}
