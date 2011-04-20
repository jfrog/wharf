package org.jfrog.wharf.ivy.repository;


import org.apache.ivy.plugins.repository.AbstractRepository;
import org.apache.ivy.plugins.repository.RepositoryCopyProgressListener;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.repository.TransferEvent;
import org.apache.ivy.util.url.ApacheURLLister;
import org.jfrog.wharf.ivy.checksum.ChecksumType;
import org.jfrog.wharf.ivy.resolver.WharfResolver;
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

    private final RepositoryCopyProgressListener progress;
    private final Map<String, WharfUrlResource> resourcesCache;
    private final WharfResolver resolver;
    private EnumSet<ChecksumType> checksums;

    public WharfURLRepository(WharfResolver resolver) {
        this.resolver = resolver;
        this.progress = new RepositoryCopyProgressListener(this);
        this.resourcesCache = new HashMap<String, WharfUrlResource>();
        // Only SHA1 by default
        checkOnlySha1();
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

    public void setChecksums(String checksums) {
        throw new UnsupportedOperationException("Wharf resolvers enforce the usage of checksums from type only!");
    }

    @Override
    public Resource getResource(String source) throws IOException {
        return getWharfResource(source);
    }

    public WharfUrlResource getWharfResource(String source) throws IOException {
        WharfUrlResource res = resourcesCache.get(source);
        if (res == null) {
            URL url;
            try {
                url = new URL(source);
            } catch (MalformedURLException e) {
                url = new File(source).toURI().toURL();
            }
            res = new WharfUrlResource(url);
            resourcesCache.put(source, res);
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
                progress.setTotalLength(totalLength);
            }
            WharfUtils.getWharfUrlHandler().download(res, destination, progress);
        } catch (IOException ex) {
            fireTransferError(ex);
            throw ex;
        } catch (RuntimeException ex) {
            fireTransferError(ex);
            throw ex;
        } finally {
            progress.setTotalLength(null);
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
                for (ListIterator iter = urls.listIterator(); iter.hasNext();) {
                    URL url = (URL) iter.next();
                    ret.add(url.toExternalForm());
                }
                return ret;
            }
        } else if (parent.startsWith("file")) {
            String path;
            try {
                path = new URI(parent).getPath();
            } catch (URISyntaxException e) {
                IOException ioe = new IOException("Couldn't list content of '" + parent + "'");
                ioe.initCause(e);
                throw ioe;
            }

            File file = new File(path);
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
            throw new IOException("Resource "+wharfUrlResource.toString()+" has a wrong checksum for "+checksums);
        }
    }

    private boolean checkResourceChecksum(WharfUrlResource wharfUrlResource, ChecksumType checksumType) {
        String actualChecksum = wharfUrlResource.getActual().get(checksumType);
        String remoteChecksum = wharfUrlResource.getRemote().get(checksumType);
        return actualChecksum != null && remoteChecksum != null && actualChecksum.equals(remoteChecksum);
    }
}
