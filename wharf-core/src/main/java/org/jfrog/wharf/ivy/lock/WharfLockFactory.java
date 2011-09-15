/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.jfrog.wharf.ivy.lock;

import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.IvySettingsAware;
import org.apache.ivy.plugins.lock.LockStrategy;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WharfLockFactory implements IvySettingsAware, Closeable {

    private static final boolean USE_NIO = true;

    static final String LOCK_FILE_SUFFIX = ".lck";
    private static final long DEFAULT_SLEEP_TIME = 400;
    private static final long DEFAULT_TIMEOUT = 15 * 1000;

    private static final ConcurrentMap<String, LockHolder> locks = new ConcurrentHashMap<String, LockHolder>();

    private long sleepTimeInMs = DEFAULT_SLEEP_TIME;
    private long timeoutInMs = DEFAULT_TIMEOUT;
    private LockLogger logger = new LockLoggerImpl(null);
    private final ArtifactLockStrategy artifactLockStrategy;


    public WharfLockFactory() {
        artifactLockStrategy = new ArtifactLockStrategy(this, "artifact-lock");
    }

    @Override
    public void setSettings(IvySettings settings) {
        logger = new LockLoggerImpl(settings);
    }

    public LockLogger getLogger() {
        return logger;
    }

    public long getTimeoutInMs() {
        return timeoutInMs;
    }

    public long getSleepTimeInMs() {
        return sleepTimeInMs;
    }

    public LockStrategy getArtifactLockStrategy() {
        return artifactLockStrategy;
    }

    public LockHolder getLockHolder(File protectedFile) {
        String path = protectedFile.getAbsolutePath();
        return locks.get(path);
    }

    public LockHolder getOrCreateLockHolder(File protectedFile) {
        String path = protectedFile.getAbsolutePath();
        LockHolder lockHolder = locks.get(path);
        if (lockHolder == null) {
            lockHolder = new WaitingLockHolder(
                    new ReentrantLockHolder(createLeafLockHolder(protectedFile)));
            LockHolder oldLockHolder = locks.putIfAbsent(path, lockHolder);
            if (oldLockHolder != null) {
                // Ignore the one created always take the old one
                lockHolder = oldLockHolder;
            }
        }
        return lockHolder;
    }

    private InternalLockHolder createLeafLockHolder(File protectedFile) {
        if (USE_NIO) {
            return new NIOFileLockHolder(this, protectedFile);
        } else {
            return new SimpleFileLockHolder(this, protectedFile);
        }
    }

    @Override
    public void close() throws IOException {
        for (LockHolder lockHolder : locks.values()) {
            if (lockHolder instanceof Closeable) {
                ((Closeable) lockHolder).close();
            }
        }
    }
}
