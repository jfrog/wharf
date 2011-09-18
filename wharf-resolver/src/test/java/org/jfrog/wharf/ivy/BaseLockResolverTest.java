package org.jfrog.wharf.ivy;

import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.repository.RepositoryCopyProgressListener;
import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.apache.ivy.util.CopyProgressEvent;
import org.apache.ivy.util.Message;
import org.jfrog.wharf.ivy.repository.WharfURLRepository;
import org.jfrog.wharf.ivy.resolver.FileSystemWharfResolver;

import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Date: 9/15/11
 * Time: 6:09 PM
 *
 * @author Fred Simon
 */
public class BaseLockResolverTest extends AbstractDependencyResolverTest {
    protected void runResolvers() throws InterruptedException {
        // we use different settings because Ivy do not support multi thread resolve with the same
        // settings yet and this is not what this test is about: the focus of this test is running
        // concurrent resolves in separate vms but using the same cache. We don't span the test on
        // multiple vms, but using separate settings we should only run into shared cache related
        // issues, and not multi thread related issues.
        IvySettingsTestHolder settings1 = createNewSettings();
        IvySettingsTestHolder settings2 = createNewSettings();
        IvySettingsTestHolder settings3 = createNewSettings();
        IvySettingsTestHolder settings4 = createNewSettings();

        // run 3 concurrent resolves, one taking 100ms to download files, one 20ms and one 5ms
        // the first one do 10 resolves, the second one 20 and the third 50
        // note that the download time is useful only at the very beginning, then the cached file is used
        ResolveThread t1 = asyncResolve(
                settings1, createSlowResolver(settings1.settings, 100), "org6#mod6.4;3", 10);
        ResolveThread t2 = asyncResolve(
                settings2, createSlowResolver(settings2.settings, 20), "org6#mod6.4;3", 20);
        ResolveThread t3 = asyncResolve(
                settings3, createSlowResolver(settings3.settings, 5), "org6#mod6.4;3", 50);
        ResolveThread t4 = asyncResolve(
                settings3, createSlowResolver(settings4.settings, 5), "org6#mod6.2;2.0", 50);
        t1.join(100000);
        t2.join(20000);
        t3.join(20000);
        t4.join(20000);
        assertEquals(10, t1.getCount());
        assertFound("org6#mod6.4;3", t1.getFinalResult());
        assertEquals(20, t2.getCount());
        assertFound("org6#mod6.4;3", t2.getFinalResult());
        assertEquals(50, t3.getCount());
        assertFound("org6#mod6.4;3", t3.getFinalResult());
        assertEquals(50, t4.getCount());
        assertFound("org6#mod6.2;2.0", t4.getFinalResult());
    }

    private FileSystemResolver createSlowResolver(IvySettings settings, final int sleep) {
        FileSystemWharfResolver resolver = new FileSystemWharfResolver();
        resolver.setRepositoryCacheManager(settings.getDefaultRepositoryCacheManager());
        resolver.setRepository(new WharfURLRepository() {
            private RepositoryCopyProgressListener progress = new RepositoryCopyProgressListener(this) {
                @Override
                public void progress(CopyProgressEvent evt) {
                    super.progress(evt);
                    sleepSilently(sleep); // makes the file copy longer to test concurrency issues
                }
            };

            public RepositoryCopyProgressListener getProgressListener() {
                return progress;
            }
        });
        resolver.setName("test");
        resolver.setSettings(settings);
        resolver.addIvyPattern(repoTestRoot.getAbsolutePath() +
                "/1/[organisation]/[module]/[type]s/[artifact]-[revision].[ext]");
        resolver.addArtifactPattern(repoTestRoot.getAbsolutePath() +
                "/1/[organisation]/[module]/[type]s/[artifact]-[revision].[ext]");
        resolver.setChecksums("");
        settings.addResolver(resolver);
        return resolver;
    }

    private ResolveThread asyncResolve(IvySettingsTestHolder settings, FileSystemResolver resolver, String module, int loop) {
        ResolveThread thread = new ResolveThread(settings, resolver, module, loop);
        thread.start();
        return thread;
    }

    private void assertFound(String module, ResolvedModuleRevision rmr) {
        assertNotNull(rmr);
        assertEquals(module, rmr.getId().toString());
    }

    private ResolvedModuleRevision resolveModule(IvySettingsTestHolder settings, FileSystemResolver resolver, String module)
            throws ParseException {
        return resolver.getDependency(new DefaultDependencyDescriptor(ModuleRevisionId.parse(module), false),
                settings.data);
    }

    private void sleepSilently(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
        }
    }

    private class ResolveThread extends Thread {
        private IvySettingsTestHolder settings;
        private FileSystemResolver resolver;
        private String module;
        private final int loop;

        private ResolvedModuleRevision finalResult;
        private int count;

        public ResolveThread(IvySettingsTestHolder settings, FileSystemResolver resolver, String module, int loop) {
            this.settings = settings;
            this.resolver = resolver;
            this.module = module;
            this.loop = loop;
        }

        public ResolvedModuleRevision getFinalResult() {
            return finalResult;
        }

        public int getCount() {
            return count;
        }

        @Override
        public void run() {
            ResolvedModuleRevision rmr = null;
            for (int i = 0; i < loop; i++) {
                try {
                    rmr = resolveModule(settings, resolver, module);
                    if (rmr == null) {
                        throw new RuntimeException("module not found: " + module);
                    }
                    count++;
                } catch (ParseException e) {
                    Message.info("parse exception " + e);
                } catch (RuntimeException e) {
                    Message.info("exception " + e);
                    e.printStackTrace();
                    throw e;
                } catch (Error e) {
                    Message.info("exception " + e);
                    e.printStackTrace();
                    throw e;
                }
            }
            finalResult = rmr;
        }
    }
}
