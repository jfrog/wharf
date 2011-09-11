package org.jfrog.wharf.ivy.repository;


import org.apache.ivy.core.IvyContext;
import org.apache.ivy.plugins.repository.AbstractRepository;
import org.apache.ivy.plugins.repository.RepositoryCopyProgressListener;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.repository.TransferEvent;
import org.apache.ivy.util.url.ApacheURLLister;
import org.jfrog.wharf.ivy.checksum.ChecksumType;
import org.jfrog.wharf.ivy.resource.WharfUrlResource;
import org.jfrog.wharf.ivy.util.WharfUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * @author Tomer Cohen
 */
public class WharfURLRepository extends AbstractRepository {
    private static final ApacheURLLister lister = new ApacheURLLister();
    public static final String ALWAYS_CHECK_RESOURCES = "wharf.alwaysCheckResources";

    private final Map<String, WharfUrlResource> resourcesCache;
    private final RepositoryCopyProgressListener progressListener;
    private EnumSet<ChecksumType> checksums;

    public WharfURLRepository() {
        this.progressListener = new RepositoryCopyProgressListener(this);
        this.resourcesCache = new HashMap<String, WharfUrlResource>();
        // Only SHA1 by default
        checkOnlySha1();
    }

    public RepositoryCopyProgressListener getProgressListener() {
        return progressListener;
    }

    public EnumSet<ChecksumType> getChecksums() {
        return checksums;
    }

    public void noChecksumCheck() {
        checksums.clear();
    }

    public void checkOnlySha1() {
        checksums = EnumSet.of(ChecksumType.sha1);
    }

    public void checkOnlyMd5() {
        checksums = EnumSet.of(ChecksumType.sha1);
    }

    public void checkAny() {
        checksums = EnumSet.allOf(ChecksumType.class);
    }

    public boolean supportsWrongSha1() {
        return checksums.size() != 1 || checksums.contains(ChecksumType.md5);
    }

    public String[] getChecksumAlgorithms() {
        String[] result = new String[checksums.size()];
        int i = 0;
        for (ChecksumType checksum : checksums) {
            result[i] = checksum.alg();
            i++;
        }
        return result;
    }

    /**
     * Fill the checksums algorithm that need to be verified to accept an artifact.
     *
     * @param checksumsList a comma separated list of checksum algorithms to use with this repository
     */
    public void setChecksums(String checksumsList) {
        checksums.clear();
        String[] checks = checksumsList.split(",");
        for (String check : checks) {
            String cs = check.trim();
            if (!"".equals(cs) && !"none".equals(cs)) {
                checksums.add(ChecksumType.valueOf(cs));
            }
        }
    }

    public static boolean isAlwaysCheck() {
        Boolean alwaysCheck = (Boolean) IvyContext.getContext().get(ALWAYS_CHECK_RESOURCES);
        return alwaysCheck != null && alwaysCheck;
    }

    public static void setAlwaysCheck(boolean alwaysCheck) {
        IvyContext.getContext().set(ALWAYS_CHECK_RESOURCES, alwaysCheck);
    }

    public Resource getResource(String source) throws IOException {
        return getWharfResource(source);
    }

    public WharfUrlResource getWharfResource(String source) throws IOException {
        WharfUrlResource res = resourcesCache.get(source);
        if (res == null || isAlwaysCheck()) {
            URL url;
            try {
                url = new URL(source);
            } catch (MalformedURLException e) {
                url = new File(source).toURI().toURL();
            }
            res = new WharfUrlResource(url);
            resourcesCache.put(source, res);
            resourcesCache.put(url.toExternalForm(), res);
            if (isAlwaysCheck()) {
                setAlwaysCheck(false);
            }
        }
        return res;
    }

    public void get(String source, File destination) throws IOException {
        WharfUrlResource res = getWharfResource(source);
        get(res, destination);
    }

    public void get(WharfUrlResource res, File destination) throws IOException {
        fireTransferInitiated(res, TransferEvent.REQUEST_GET);
        try {
            long totalLength = res.getContentLength();
            if (totalLength > 0) {
                getProgressListener().setTotalLength(totalLength);
            }
            WharfUtils.getWharfUrlHandler().download(res, destination, getProgressListener());
        } catch (IOException ex) {
            fireTransferError(ex);
            throw ex;
        } catch (RuntimeException ex) {
            fireTransferError(ex);
            throw ex;
        } finally {
            getProgressListener().setTotalLength(null);
        }
    }

    public void put(File source, String destination, boolean overwrite) throws IOException {
        throw new UnsupportedOperationException("put in not supported by " + getName());
    }

    public List list(String parent) throws IOException {
        if (parent.startsWith("http")) {
            List urls = lister.listAll(new URL(parent));
            if (urls != null) {
                List ret = new ArrayList(urls.size());
                for (ListIterator iter = urls.listIterator(); iter.hasNext(); ) {
                    URL url = (URL) iter.next();
                    ret.add(url.toExternalForm());
                }
                return ret;
            }
        } else {
            File file;
            String path;
            if (parent.startsWith("file")) {
                try {
                    path = new URI(parent).getPath();
                    file = new File(path);
                } catch (URISyntaxException e) {
                    IOException ioe = new IOException("Couldn't list content of '" + parent + "'");
                    ioe.initCause(e);
                    throw ioe;
                }
            } else {
                file = new File(parent);
                URI uri = file.toURI();
                path = uri.getPath();
                parent = uri.toURL().toExternalForm();
            }

            if (file.exists() && file.isDirectory()) {
                String[] files = file.list();
                List ret = new ArrayList(files.length);
                URL context = path.endsWith("/") ? new URL(parent) : new URL(parent + "/");
                for (int i = 0; i < files.length; i++) {
                    ret.add(new URL(context, files[i]).toExternalForm());
                }
                return ret;
            } else {
                return Collections.EMPTY_LIST;
            }
        }
        return null;
    }

    public void checkChecksums(WharfUrlResource wharfUrlResource) throws IOException {
        if (checksums.isEmpty()) {
            return;
        }
        boolean oneGood = false;
        for (ChecksumType checksumType : checksums) {
            if (checkResourceChecksum(wharfUrlResource, checksumType)) {
                oneGood = true;
            }
        }
        if (!oneGood) {
            throw new IOException("Resource " + wharfUrlResource.toString() + " has a wrong checksum for " + checksums);
        }
    }

    private boolean checkResourceChecksum(WharfUrlResource wharfUrlResource, ChecksumType checksumType) {
        String actualChecksum = wharfUrlResource.getActual().get(checksumType);
        String remoteChecksum = wharfUrlResource.getRemote().get(checksumType);
        return actualChecksum != null && remoteChecksum != null && actualChecksum.equals(remoteChecksum);
    }
}
