package org.jfrog.wharf.layout.base;

import org.apache.commons.lang.StringUtils;
import org.jfrog.wharf.layout.ArtifactInfo;
import org.jfrog.wharf.layout.ArtifactPathMapper;
import org.jfrog.wharf.layout.regex.NamedMatcher;
import org.jfrog.wharf.layout.regex.NamedPattern;

import java.util.Map;

import static org.jfrog.wharf.layout.regex.RepoLayoutPatterns.IVY_PATTERN;
import static org.jfrog.wharf.layout.regex.RepoLayoutPatterns.generateNamedRegexFromLayoutPattern;

/**
 * Date: 9/11/11
 * Time: 4:22 PM
 *
 * @author Fred Simon
 */
public class IvyArtifactPathMapper implements ArtifactPathMapper {

//    public static final ImmutableMap<String, FieldValueProvider> valueProviders;

    private final NamedPattern ivyPathPattern;
    private final NamedPattern artPathPattern;

    private final String rootPath;
    private final String ivyPattern;
    private final String artifactPattern;

    public IvyArtifactPathMapper() {
        this("", IVY_PATTERN, IVY_PATTERN);
    }

    public IvyArtifactPathMapper(String rootPath, String ivyPattern, String artifactPattern) {
        this.rootPath = rootPath;
        this.ivyPattern = ivyPattern;
        this.artifactPattern = artifactPattern;
        this.ivyPathPattern = NamedPattern.compile(generateNamedRegexFromLayoutPattern(ivyPattern, true));
        this.artPathPattern = NamedPattern.compile(generateNamedRegexFromLayoutPattern(artifactPattern, true));
    }

    @Override
    public ArtifactInfo fromMap(Map<String, String> map) {
        IvyArtifactInfo result = new IvyArtifactInfo();
        result.putAll(map);
        result.populate();
        return result;
    }

    @Override
    public String toPath(ArtifactInfo artifact) {
        if (!artifact.isValid()) {
            throw new IllegalArgumentException("Artifact " + artifact + " is not a valid artifact!");
        }
        return (StringUtils.isNotBlank(rootPath) ? rootPath + "/" : "") + artifact.getGroupPath() +
                "/" + artifact.getModuleName() +
                "/" + artifact.getBaseRevision() + ("integration".equals(artifact.getStatus()) ? "-SNAPSHOT" : "") +
                "/" + artifact.getModuleName() + "-" + artifact.getRevision() +
                (StringUtils.isNotBlank(artifact.getClassifier()) ? "-" + artifact.getClassifier() : "") +
                "." + artifact.getExtension();
    }

    @Override
    public ArtifactInfo fromPath(String path) {
        if (StringUtils.isBlank(path)) {
            throw new IllegalArgumentException("Cannot construct an artifact info object from a blank item path.");
        }
        if (StringUtils.isNotBlank(rootPath) && path.startsWith(rootPath)) {
            path = path.substring(rootPath.length());
        }
        path = LayoutUtils.convertToValidField(path);
        NamedMatcher namedMatcher = this.ivyPathPattern.matcher(path);
        if (namedMatcher.matches()) {
            return fromMap(namedMatcher.namedGroups());
        }
        return new IvyArtifactInfo();
    }
}
