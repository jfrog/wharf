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
package org.jfrog.wharf.util;

import org.apache.ivy.util.FileUtil;

import java.io.File;

public class CacheCleaner {

    /**
     * Delete the directory and all it contains. Previously, we used the ant delete task, but it occasionaly failed
     * (access denied) on my machine for unknown reason.
     */
    public static void deleteDir(File toDelete) {
        FileUtil.forceDelete(toDelete);
    }
}
