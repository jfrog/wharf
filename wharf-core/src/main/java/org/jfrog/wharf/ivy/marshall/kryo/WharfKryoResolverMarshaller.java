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
import org.apache.ivy.util.Message;
import org.jfrog.wharf.ivy.marshall.api.WharfResolverMarshaller;
import org.jfrog.wharf.ivy.model.WharfResolverMetadata;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Tomer Cohen
 */
public class WharfKryoResolverMarshaller implements WharfResolverMarshaller {
    private static final String RESOLVERS_FILE_PATH = ".wharf/resolvers.kryo";
    private static final String RESOLVERS_LOCK_FILE_PATH = ".wharf/resolvers.kryo.lock";
    private static final long WAIT_FOR_LOCK_MS = 2000L;

    public String getResolversFilePath() {
        return RESOLVERS_FILE_PATH;
    }

    public void save(File baseDir, Set<WharfResolverMetadata> wharfResolverMetadatas) {
        FileHolder fileHolder = new FileHolder(baseDir);
        OutputStream stream = null;
        try {
            fileHolder.acquireLockFile();
            stream = new FileOutputStream(fileHolder.getResolversFile());
            ObjectBuffer buffer = KryoFactory.createWharfResolverObjectBuffer();
            buffer.writeObject(stream, wharfResolverMetadatas);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if ((stream != null)) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            fileHolder.releaseLockFile();
        }
    }

    public Set<WharfResolverMetadata> getWharfMetadatas(File baseDir) {
        FileHolder fileHolder = new FileHolder(baseDir);
        try {
            fileHolder.acquireLockFile();
            File resolversFile = fileHolder.getResolversFile();
            if (resolversFile.exists()) {
                InputStream stream = null;
                try {
                    stream = new FileInputStream(resolversFile);
                    ObjectBuffer buffer = KryoFactory.createWharfResolverObjectBuffer();
                    return buffer.readObject(stream, HashSet.class);
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                } finally {
                    if ((stream != null)) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                }
            }
        } finally {
            fileHolder.releaseLockFile();
        }
        return new HashSet<WharfResolverMetadata>();
    }

    private static class FileHolder {
        private final File baseDir;
        private final File resolversFile;
        private final File lockFile;
        private boolean lockAcquired = false;

        private FileHolder(File baseDir) {
            this.baseDir = baseDir;
            this.resolversFile = new File(baseDir, RESOLVERS_FILE_PATH);
            this.lockFile = new File(baseDir, RESOLVERS_LOCK_FILE_PATH);
            File dir = resolversFile.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }

        public File getResolversFile() {
            return resolversFile;
        }

        private void releaseLockFile() {
            if (lockAcquired) {
                if (!lockFile.exists()) {
                    Message.error("Acquired lock file " + lockFile.getAbsolutePath() + " but not present on release!");
                }
                if (!lockFile.delete()) {
                    Message.error("Could not release lock file " + lockFile.getAbsolutePath());
                }
            }
            lockAcquired = false;
        }

        private void acquireLockFile() {
            long w = 0;
            try {
                w = WAIT_FOR_LOCK_MS;
                while (!lockFile.createNewFile() && w > 0) {
                    long millis = WAIT_FOR_LOCK_MS / 10;
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Interrupted while acquire lock file " + lockFile.getAbsolutePath(), e);
                    }
                    w -= millis;
                }
            } catch (IOException e) {
                throw new RuntimeException("IOException while acquire lock file " + lockFile.getAbsolutePath(), e);
            }
            if (w <= 0) {
                throw new RuntimeException("Could not acquire lock file " + lockFile.getAbsolutePath() + " in " + WAIT_FOR_LOCK_MS + "ms");
            }
            lockAcquired = true;
        }
    }


}
