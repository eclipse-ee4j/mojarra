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

// MultiThreadTestRunner.java

package com.sun.faces.util;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;

import java.io.PrintStream;

/**
 * <B>MultiThreadTestRunner.java</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class MultiThreadTestRunner extends Object {

//
// Protected Constants
//

// Class Variables
//

//
// Instance Variables
//

// Attribute Instance Variables

// Relationship Instance Variables

    private Thread [] threads;
    private Object [] outcomes;

//
// Constructors and Initializers    
//

    public MultiThreadTestRunner(Thread [] yourThreads,
				 Object [] yourOutcomes) {
	threads = yourThreads;
	outcomes = yourOutcomes;

	if (null == threads || null == outcomes) {
	    throw new IllegalArgumentException();
	}
    }

    /**
     * @return true iff one of the threads has failed.
     */

    public boolean runThreadsAndOutputResults(PrintStream out) throws Exception {
	int i;

	if (outcomes.length != threads.length) {
	    throw new IllegalArgumentException();
	}
	
	for (i = 0; i < threads.length; i++) {
	    outcomes[i] = null;
	}
	for (i = 0; i < threads.length; i++) {
	    threads[i].start();
	}
	
	BitSet printed = new BitSet(threads.length);
	boolean foundFailedThread = false;
	// wait for all threads to complete
	while (true) {
	    boolean foundIncompleteThread = false;
	    for (i = 0; i < threads.length; i++) {
		if (null == outcomes[i]) {
		    foundIncompleteThread = true;
		    break;
		}
		else {
		    // print out the outcome for this thread
		    if (!printed.get(i)) {
			printed.set(i);
			out.print(threads[i].getName() + " outcome: ");
			if (outcomes[i] instanceof Exception) {
			    foundFailedThread = true;
			    out.println("Exception: " + 
					       outcomes[i] + " " + 
					       ((Exception)outcomes[i]).getMessage());
			}
			else {
			    out.println(outcomes[i].toString());
			}
			out.flush();
		    }
		}
	    }
	    if (!foundIncompleteThread) {
		break;
	    }
	    Thread.sleep(1000);
	}
	
	return foundFailedThread;
    }

} // end of class MultiThreadTestRunner
