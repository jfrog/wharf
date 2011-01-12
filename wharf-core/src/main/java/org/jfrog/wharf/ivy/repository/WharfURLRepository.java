package org.jfrog.wharf.ivy.repository;


import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.repository.url.URLRepository;
import org.jfrog.wharf.ivy.resource.WharfUrlResource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tomer Cohen
 */
public class WharfURLRepository extends URLRepository {

    private Map<String, Resource> resourcesCache = new HashMap<String, Resource>();

    @Override
    public Resource getResource(String source) throws IOException {
        Resource res = resourcesCache.get(source);
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
}
