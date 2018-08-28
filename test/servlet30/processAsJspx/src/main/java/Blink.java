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

/**
 * I love blinking things.
 *
 * @author Arthur van Hoff
 * @modified 04/24/96 Jim Hagen use getBackground
 * @modified 02/05/98 Mike McCloskey removed use of deprecated methods
 * @modified 04/23/99 Josh Bloch, use timer instead of explicit multithreading.
 * @modified 07/10/00 Daniel Peek brought to code conventions, minor changes
 */

import java.awt.*;
import java.util.*;

public class Blink extends java.applet.Applet {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Timer timer; // Schedules the blinking
    private String labelString; // The label for the window
    private int delay; // the delay time between blinks

    @Override
    public void init() {
        String blinkFrequency = getParameter("speed");
        delay = (blinkFrequency == null) ? 400 : (1000 / Integer.parseInt(blinkFrequency));
        labelString = getParameter("lbl");
        if (labelString == null)
            labelString = "Blink";
        Font font = new java.awt.Font("TimesRoman", Font.PLAIN, 24);
        setFont(font);
    }

    @Override
    public void start() {
        timer = new Timer(); // creates a new timer to schedule the blinking
        timer.schedule(new TimerTask() { // creates a timertask to schedule
            // overrides the run method to provide functionality
            @Override
            public void run() {
                repaint();
            }
        }, delay, delay);
    }

    @Override
    public void paint(Graphics g) {
        int fontSize = g.getFont().getSize();
        int x = 0, y = fontSize, space;
        int red = (int) (50 * Math.random());
        int green = (int) (50 * Math.random());
        int blue = (int) (256 * Math.random());
        Dimension d = getSize();
        g.setColor(Color.black);
        FontMetrics fm = g.getFontMetrics();
        space = fm.stringWidth(" ");
        for (StringTokenizer t = new StringTokenizer(labelString); t.hasMoreTokens();) {
            String word = t.nextToken();
            int w = fm.stringWidth(word) + space;
            if (x + w > d.width) {
                x = 0;
                y += fontSize; // move word to next line if it doesn't fit
            }
            if (Math.random() < 0.5)
                g.setColor(new java.awt.Color((red + y * 30) % 256, (green + x / 3) % 256, blue));
            else
                g.setColor(getBackground());
            g.drawString(word, x, y);
            x += w; // shift to the right to draw the next word
        }
    }

    @Override
    public void stop() {
        timer.cancel(); // stops the timer
    }

    @Override
    public String getAppletInfo() {
        return "Title: Blinker\n" + "Author: Arthur van Hoff\n" + "Displays multicolored blinking text.";
    }

    @Override
    public String[][] getParameterInfo() {
        String pinfo[][] = { { "speed", "string", "The blink frequency" }, { "lbl", "string", "The text to blink." }, };
        return pinfo;
    }
}
