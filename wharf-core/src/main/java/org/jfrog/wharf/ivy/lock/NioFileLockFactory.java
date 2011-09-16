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

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NioFileLockFactory extends AbstractLockHolderFactory {

    /**
     * This needs to be static since the nio file lock is JVM based
     */
    private static final ConcurrentMap<String, InternalLockHolder> nioLocks = new ConcurrentHashMap<String, InternalLockHolder>();

    @Override
    protected LockHolder createLockHolder(File protectedFile) {
        return new WaitingLockHolder(new ReentrantLockHolder(createLeafLockHolder(protectedFile)));
    }

    private InternalLockHolder createLeafLockHolder(File protectedFile) {
        String path = protectedFile.getAbsolutePath();
        InternalLockHolder nioLockHolder = nioLocks.get(path);
        if (nioLockHolder == null) {
            nioLockHolder = new NioFileLockHolder(this, protectedFile);
            InternalLockHolder oldNioLockHolder = nioLocks.putIfAbsent(path, nioLockHolder);
            if (oldNioLockHolder != null) {
                nioLockHolder = oldNioLockHolder;
            }
        }
        return nioLockHolder;
    }
}
