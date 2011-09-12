package org.jfrog.wharf.layout.base;

import com.google.common.collect.Maps;
import org.jfrog.wharf.layout.ArtifactInfo;
import org.jfrog.wharf.layout.field.*;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.jfrog.wharf.layout.field.DefaultFieldDefinitions.artifactFieldDefinitions;

/**
 * Date: 9/11/11
 * Time: 6:02 PM
 *
 * @author Fred Simon
 */
public class MavenArtifactInfo implements ArtifactInfo {

    private final Map<String, FieldDefinition> customFieldsDefinitions = Maps.newHashMap();
    private final Map<String, String> fields = Maps.newHashMap();

    protected FieldDefinition getFieldDefinition(String key) {
        FieldDefinition fieldDefinition = customFieldsDefinitions.get(key);
        if (fieldDefinition == null) {
            return artifactFieldDefinitions.get(key);
        }
        return fieldDefinition;
    }

    @Override
    public String get(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        String k = key.toString();
        FieldDefinition fieldDefinition = getFieldDefinition(k);
        if (fieldDefinition == null) {
            return null;
        }
        String value = fields.get(fieldDefinition.id());
        if (value == null) {
            value = fieldDefinition.provider().extractFromOthers(fields);
            if (value != null) {
                // TODO: not sure I like changing map on get method ?!?
                fields.put(fieldDefinition.id(), value);
            }
        }
        return value;
    }

    @Override
    public String put(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        FieldDefinition fieldDefinition = getFieldDefinition(key);
        if (fieldDefinition == null) {
            fieldDefinition = new DefaultFieldDefinition(key, null);
            customFieldsDefinitions.put(key, fieldDefinition);
        }
        return fields.put(fieldDefinition.id(), fieldDefinition.provider().convert(value));
    }

    @Override
    public String remove(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        String k = key.toString();
        FieldDefinition fieldDefinition = getFieldDefinition(k);
        if (fieldDefinition == null) {
            return null;
        }
        return fields.remove(fieldDefinition.id());
    }

    @Override
    public String[] getSerializableFields() {
        return new String[] {
                ModuleFields.org.id(),
                ModuleFields.module.id(),
                ModuleRevisionFields.revision.id(),
                ModuleRevisionFields.baseRev.id(),
                ModuleRevisionFields.status.id(),
                ArtifactFields.artifact.id(),
                ArtifactFields.classifier.id(),
                ArtifactFields.ext.id()
        };
    }

    @Override
    public String getArtifactName() {
        return get(ArtifactFields.artifact.id());
    }

    @Override
    public String getExtension() {
        return get(ArtifactFields.ext.id());
    }

    @Override
    public String getType() {
        return get(ArtifactFields.type.id());
    }

    @Override
    public String getClassifier() {
        return get(ArtifactFields.classifier.id());
    }

    @Override
    public String getGroup() {
        return get(ModuleFields.org.id());
    }

    @Override
    public String getGroupPath() {
        return get(ModuleFields.orgPath.id());
    }

    @Override
    public String getModuleName() {
        return get(ModuleFields.module.id());
    }

    @Override
    public String getRevision() {
        return get(ModuleRevisionFields.revision.id());
    }

    @Override
    public String getBaseRevision() {
        return get(ModuleRevisionFields.baseRev.id());
    }

    @Override
    public String getStatus() {
        return get(ModuleRevisionFields.status.id());
    }

    @Override
    public int size() {
        return fields.size();
    }

    @Override
    public boolean isEmpty() {
        return fields.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return artifactFieldDefinitions.containsKey(key) || customFieldsDefinitions.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return fields.containsValue(value);
    }


    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        for (Entry<? extends String, ? extends String> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        fields.clear();
    }

    @Override
    public Set<String> keySet() {
        return fields.keySet();
    }

    @Override
    public Collection<String> values() {
        return fields.values();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return fields.entrySet();
    }
}
