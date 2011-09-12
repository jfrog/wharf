package org.jfrog.wharf.layout.base;

import org.apache.commons.lang.StringUtils;
import org.jfrog.wharf.layout.ArtifactInfo;
import org.jfrog.wharf.layout.ArtifactPathMapper;
import org.jfrog.wharf.layout.field.ArtifactFields;
import org.jfrog.wharf.layout.field.ModuleFields;
import org.jfrog.wharf.layout.field.ModuleRevisionFields;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Date: 9/11/11
 * Time: 4:22 PM
 *
 * @author Fred Simon
 */
public class MavenArtifactPathMapper implements ArtifactPathMapper {
    private final static Set<String> MANDATORY_FIELDS = new HashSet<String>();

    static {
        MANDATORY_FIELDS.add(ModuleFields.org.id());
        MANDATORY_FIELDS.add(ModuleFields.module.id());
        MANDATORY_FIELDS.add(ModuleRevisionFields.baseRev.id());
        MANDATORY_FIELDS.add(ModuleRevisionFields.status.id());
        MANDATORY_FIELDS.add(ModuleRevisionFields.revision.id());
        MANDATORY_FIELDS.add(ArtifactFields.ext.id());
    }

    private final String rootPath;

    public MavenArtifactPathMapper() {
        this("");
    }

    public MavenArtifactPathMapper(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public ArtifactInfo fromMap(Map<String, String> map) {
        MavenArtifactInfo result = new MavenArtifactInfo();
        result.putAll(map);
        return result;
    }

    @Override
    public boolean isValid(ArtifactInfo artifact) {
        for (String mandatoryField : MANDATORY_FIELDS) {
            if (!artifact.containsKey(mandatoryField)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toPath(ArtifactInfo artifact) {
        for (String mandatoryField : MANDATORY_FIELDS) {
            if (!artifact.containsKey(mandatoryField)) {
                throw new IllegalArgumentException("Artifact " + artifact + " is not a valid Maven artifact!\n" +
                        "It does not contains the field " + mandatoryField);
            }
        }
        return ( StringUtils.isNotBlank(rootPath) ? rootPath + "/" : "" ) + artifact.getGroupPath() +
                "/" + artifact.getModuleName() +
                "/" + artifact.getBaseRevision() + ("integration".equals(artifact.getStatus()) ? "-SNAPSHOT" : "" ) +
                "/" + artifact.getModuleName() + "-" + artifact.getRevision() +
                ( StringUtils.isNotBlank(artifact.getClassifier()) ? "-" + artifact.getClassifier() : "" ) +
                "." + artifact.getExtension();
    }

    @Override
    public ArtifactInfo fromPath(String path) {
        throw new UnsupportedOperationException("TODO");
    }

}
