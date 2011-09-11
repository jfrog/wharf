package org.jfrog.wharf.layout.base;

import org.jfrog.wharf.layout.ArtifactInfo;
import org.jfrog.wharf.layout.field.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Date: 9/11/11
 * Time: 6:02 PM
 *
 * @author Fred Simon
 */
public class MavenArtifactInfo implements ArtifactInfo {

    private final Map<String, String> fields = new HashMap<String, String>();

    @Override
    public String getArtifactName() {
        return null;
    }

    @Override
    public String getExtension() {
        return get(ArtifactFields.ext.id());
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getClassifier() {
        return null;
    }

    @Override
    public String[] getSerializableFields() {
        return new String[]{"org"};
    }

    @Override
    public String getGroup() {
        return null;
    }

    @Override
    public String getGroupPath() {
        return null;
    }

    @Override
    public String getModuleName() {
        return null;
    }

    @Override
    public String getRevision() {
        return null;
    }

    @Override
    public String getBaseRevision() {
        return null;
    }

    @Override
    public String getStatus() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public String get(Object key) {
        String k = key.toString();

        return null;
    }

    @Override
    public String put(String key, String value) {
        return null;
    }

    @Override
    public String remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
    }

    @Override
    public void clear() {
    }

    @Override
    public Set<String> keySet() {
        return null;
    }

    @Override
    public Collection<String> values() {
        return null;
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return null;
    }
}
