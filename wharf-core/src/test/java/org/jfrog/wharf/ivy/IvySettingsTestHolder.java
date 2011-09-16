package org.jfrog.wharf.ivy;

import org.apache.ivy.core.event.EventManager;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolveEngine;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.core.sort.SortEngine;
import org.jfrog.wharf.ivy.cache.WharfCacheManager;
import org.jfrog.wharf.ivy.lock.LockHolderFactory;

import java.io.File;

/**
* Date: 9/16/11
* Time: 3:22 PM
*
* @author Fred Simon
*/
public class IvySettingsTestHolder {
    public IvySettings settings;
    public ResolveEngine engine;
    public ResolveData data;
    public WharfCacheManager cacheManager;

    public void init(File baseDir, File cacheFolder) {
        settings = new IvySettings();
        if (AbstractDependencyResolverTest.useNio) {
            settings.setVariable(LockHolderFactory.class.getName(), "nio");
        } else {
            settings.setVariable(LockHolderFactory.class.getName(), "simple");
        }
        settings.setBaseDir(baseDir);
        settings.setDefaultCache(cacheFolder);
        cacheManager = WharfCacheManager.newInstance(settings);
        settings.setDefaultRepositoryCacheManager(cacheManager);
        engine = new ResolveEngine(settings, new EventManager(), new SortEngine(settings));
        data = new ResolveData(engine, new ResolveOptions());
    }

}
