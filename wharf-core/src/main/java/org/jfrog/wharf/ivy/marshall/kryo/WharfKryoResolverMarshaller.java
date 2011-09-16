/*
 *
 *  Copyright (C) 2010 JFrog Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package org.jfrog.wharf.ivy.marshall.kryo;


import com.esotericsoftware.kryo.ObjectBuffer;
import org.jfrog.wharf.ivy.lock.LockHolder;
import org.jfrog.wharf.ivy.lock.LockHolderFactory;
import org.jfrog.wharf.ivy.marshall.api.WharfResolverMarshaller;
import org.jfrog.wharf.ivy.model.WharfResolverMetadata;
import org.jfrog.wharf.ivy.util.WharfUtils;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Tomer Cohen
 */
public class WharfKryoResolverMarshaller implements WharfResolverMarshaller {
    private static final String RESOLVERS_FILE_PATH = ".wharf/resolvers.kryo";

    private final LockHolderFactory lockFactory;

    public WharfKryoResolverMarshaller(LockHolderFactory lockFactory) {
        this.lockFactory = lockFactory;
    }

    public void save(File baseDir, Set<WharfResolverMetadata> wharfResolverMetadatas) {
        LockHolder lockHolder = getLockHolder(baseDir);
        OutputStream stream = null;
        boolean locked = false;
        try {
            locked = lockHolder.acquireLock();
            if (!locked) {
                throw new RuntimeException("Could not acquire lock due to: " + lockHolder.stateMessage());
            }
            stream = new FileOutputStream(lockHolder.getProtectedFile());
            ObjectBuffer buffer = KryoFactory.createWharfResolverObjectBuffer();
            buffer.writeObject(stream, wharfResolverMetadatas);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            WharfUtils.closeQuietly(stream);
            if (locked) {
                lockHolder.releaseLock();
            }
        }
    }

    public Set<WharfResolverMetadata> getWharfMetadatas(File baseDir) {
        LockHolder lockHolder = getLockHolder(baseDir);
        boolean locked = false;
        try {
            locked = lockHolder.acquireLock();
            if (!locked) {
                throw new RuntimeException("Could not acquire lock due to: " + lockHolder.stateMessage());
            }
            File resolversFile = lockHolder.getProtectedFile();
            if (resolversFile.exists()) {
                InputStream stream = null;
                try {
                    stream = new FileInputStream(resolversFile);
                    ObjectBuffer buffer = KryoFactory.createWharfResolverObjectBuffer();
                    //noinspection unchecked
                    return buffer.readObject(stream, HashSet.class);
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                } finally {
                    WharfUtils.closeQuietly(stream);
                }
            }
        } finally {
            if (locked) {
                lockHolder.releaseLock();
            }
        }
        return new HashSet<WharfResolverMetadata>();
    }

    private LockHolder getLockHolder(File baseDir) {
        return lockFactory.getOrCreateLockHolder(new File(baseDir, RESOLVERS_FILE_PATH));
    }
}
