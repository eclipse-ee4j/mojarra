/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.facelets.impl;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import com.sun.faces.util.ConcurrentCache;
import com.sun.faces.util.ExpiringConcurrentCache;
import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.view.facelets.FaceletCache;

/**
 * Default FaceletCache implementation.
 */
final class DefaultFaceletCache extends FaceletCache<DefaultFacelet> {

    /**
     * Constructor
     *
     * @param refreshPeriod cache refresh period (in seconds). 0 means 'always refresh', negative value means 'never
     * refresh'
     */
    DefaultFaceletCache(final long refreshPeriod) {

        // We will be delegating object storage to the ExpiringCocurrentCache
        // Create Factory objects here for the cache. The objects will be delegating to our
        // own instance factories

        final boolean checkExpiry = refreshPeriod > 0;

        ConcurrentCache.Factory<URL, Record> faceletFactory = key -> {
            // Make sure that the expensive timestamp retrieval is not done
            // if no expiry check is going to be performed
            long lastModified = checkExpiry ? Util.getLastModified(key) : 0;
            return new Record(System.currentTimeMillis(), lastModified, getMemberFactory().newInstance(key), refreshPeriod);
        };

        ConcurrentCache.Factory<URL, Record> metadataFaceletFactory = key -> {
            // Make sure that the expensive timestamp retrieval is not done
            // if no expiry check is going to be performed
            long lastModified = checkExpiry ? Util.getLastModified(key) : 0;
            return new Record(System.currentTimeMillis(), lastModified, getMetadataMemberFactory().newInstance(key), refreshPeriod);
        };

        // No caching if refreshPeriod is 0
        if (refreshPeriod == 0) {
            _faceletCache = new NoCache(faceletFactory);
            _metadataFaceletCache = new NoCache(metadataFaceletFactory);
        } else {
            ExpiringConcurrentCache.ExpiryChecker<URL, Record> checker = refreshPeriod > 0 ? new ExpiryChecker() : new NeverExpired();
            _faceletCache = new ExpiringConcurrentCache<>(faceletFactory, checker);
            _metadataFaceletCache = new ExpiringConcurrentCache<>(metadataFaceletFactory, checker);
        }
    }

    @Override
    public DefaultFacelet getFacelet(URL url) throws IOException {
        com.sun.faces.util.Util.notNull("url", url);
        DefaultFacelet f = null;

        try {
            f = _faceletCache.get(url).getFacelet();
        } catch (ExecutionException e) {
            _unwrapIOException(e);
        }
        return f;
    }

    @Override
    public boolean isFaceletCached(URL url) {
        com.sun.faces.util.Util.notNull("url", url);

        return _faceletCache.containsKey(url);
    }

    @Override
    public DefaultFacelet getViewMetadataFacelet(URL url) throws IOException {
        com.sun.faces.util.Util.notNull("url", url);

        DefaultFacelet f = null;

        try {
            f = _metadataFaceletCache.get(url).getFacelet();
        } catch (ExecutionException e) {
            _unwrapIOException(e);
        }
        return f;
    }

    @Override
    public boolean isViewMetadataFaceletCached(URL url) {
        com.sun.faces.util.Util.notNull("url", url);

        return _metadataFaceletCache.containsKey(url);
    }

    private void _unwrapIOException(ExecutionException e) throws IOException {
        Throwable t = e.getCause();
        if (t instanceof IOException) {
            throw (IOException) t;
        }
        if (t.getCause() instanceof IOException) {
            throw (IOException) t.getCause();
        }
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }
        throw new FacesException(t);
    }

    private final ConcurrentCache<URL, Record> _faceletCache;
    private final ConcurrentCache<URL, Record> _metadataFaceletCache;

    /**
     * This class holds the Facelet instance and its original URL's last modified time. It also produces the time when the
     * next expiry check should be performed
     */
    private static class Record {
        Record(long creationTime, long lastModified, DefaultFacelet facelet, long refreshInterval) {
            _facelet = facelet;
            _creationTime = creationTime;
            _lastModified = lastModified;
            _refreshInterval = refreshInterval;

            // There is no point in calculating the next refresh time if we are refreshing always/never
            _nextRefreshTime = _refreshInterval > 0 ? new AtomicLong(creationTime + refreshInterval) : null;
        }

        DefaultFacelet getFacelet() {
            return _facelet;
        }

        long getLastModified() {
            return _lastModified;
        }

        long getNextRefreshTime() {
            // There is no point in calculating the next refresh time if we are refreshing always/never
            return _refreshInterval > 0 ? _nextRefreshTime.get() : 0;
        }

        long getAndUpdateNextRefreshTime() {
            // There is no point in calculating the next refresh time if we are refreshing always/never
            return _refreshInterval > 0 ? _nextRefreshTime.getAndSet(System.currentTimeMillis() + _refreshInterval) : 0;
        }

        private final long _lastModified;
        private final long _refreshInterval;
        private final long _creationTime;
        private final AtomicLong _nextRefreshTime;
        private final DefaultFacelet _facelet;
    }

    private static class ExpiryChecker implements ExpiringConcurrentCache.ExpiryChecker<URL, Record> {

        @Override
        public boolean isExpired(URL url, Record record) {
            if (System.currentTimeMillis() > record.getNextRefreshTime()) {
                record.getAndUpdateNextRefreshTime();
                long lastModified = Util.getLastModified(url);
                // The record is considered expired if its original last modified time
                // is older than the URL's current last modified time
                return lastModified > record.getLastModified();
            }
            return false;
        }
    }

    private static class NeverExpired implements ExpiringConcurrentCache.ExpiryChecker<URL, Record> {
        @Override
        public boolean isExpired(URL key, Record value) {
            return false;
        }
    }

    /**
     * ConcurrentCache implementation that does no caching (always creates new instances)
     */
    private static class NoCache extends ConcurrentCache<URL, Record> {
        public NoCache(ConcurrentCache.Factory<URL, Record> f) {
            super(f);
        }

        @Override
        public Record get(final URL key) throws ExecutionException {
            try {
                return getFactory().newInstance(key);
            } catch (Exception e) {
                throw new ExecutionException(e);
            }
        }

        @Override
        public boolean containsKey(final URL key) {
            return false;
        }
    }
}
