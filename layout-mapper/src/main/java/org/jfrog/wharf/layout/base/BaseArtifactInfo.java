package org.jfrog.wharf.layout.base;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.jfrog.wharf.layout.ArtifactInfo;
import org.jfrog.wharf.layout.field.FieldDefinition;
import org.jfrog.wharf.layout.field.FieldValueProvider;
import org.jfrog.wharf.layout.field.definition.ArtifactFields;
import org.jfrog.wharf.layout.field.definition.DefaultFieldDefinition;
import org.jfrog.wharf.layout.field.definition.ModuleFields;
import org.jfrog.wharf.layout.field.definition.ModuleRevisionFields;
import org.jfrog.wharf.layout.field.provider.BaseFieldProvider;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.jfrog.wharf.layout.base.LayoutUtils.convertToValidField;
import static org.jfrog.wharf.layout.field.DefaultFieldDefinitions.artifactFieldDefinitions;

/**
 * Date: 9/14/11
 * Time: 3:05 PM
 *
 * @author Fred Simon
 */
public abstract class BaseArtifactInfo implements ArtifactInfo {
    private final Map<String, FieldValueProvider> customFieldsDefinitions = Maps.newHashMap();
    private final Map<String, String> fields = Maps.newHashMap();
    private boolean populated = false;

    protected FieldValueProvider getFieldProvider(String key) {
        FieldValueProvider fieldValueProvider = customFieldsDefinitions.get(key);
        if (fieldValueProvider == null) {
            FieldDefinition fieldDefinition = artifactFieldDefinitions.get(key);
            if (fieldDefinition != null) {
                return getBasicProviders().get(fieldDefinition.id());
            }
        }
        return fieldValueProvider;
    }

    protected abstract ImmutableMap<String, FieldValueProvider> getBasicProviders();

    @Override
    public String get(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        String k = key.toString();
        FieldValueProvider fieldProvider = getFieldProvider(k);
        if (fieldProvider == null) {
            return null;
        }
        return fields.get(fieldProvider.id());
    }

    @Override
    public String put(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        FieldValueProvider fieldProvider = getFieldProvider(key);
        if (fieldProvider == null) {
            fieldProvider = new BaseFieldProvider(new DefaultFieldDefinition(false, key));
            customFieldsDefinitions.put(key, fieldProvider);
        }
        return fields.put(fieldProvider.id(), convertToValidField(value));
    }

    @Override
    public String remove(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        String k = key.toString();
        FieldDefinition fieldDefinition = getFieldProvider(k);
        if (fieldDefinition == null) {
            return null;
        }
        return fields.remove(fieldDefinition.id());
    }

    @Override
    public String[] getSerializableFields() {
        return new String[]{
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
    public void populate() {
        if (populated) {
            throw new IllegalStateException("Cannot populate an Artifact twice! Use copy from map!");
        }
        populated = true;
        for (FieldValueProvider provider : getBasicProviders().values()) {
            if (fields.get(provider.id()) == null) {
                provider.populate(fields);
            }
        }
    }

    @Override
    public boolean isValid() {
        if (!populated) {
            populate();
        }
        for (FieldValueProvider provider : getBasicProviders().values()) {
            if (!provider.isValid(fields)) {
                return false;
            }
        }
        return true;
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
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        String k = key.toString();
        if (customFieldsDefinitions.containsKey(k)) {
            return true;
        }
        if (!artifactFieldDefinitions.containsKey(k)) {
            return false;
        }
        FieldDefinition fieldDefinition = getFieldProvider(k);
        return StringUtils.isNotBlank(get(fieldDefinition.id()));
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
