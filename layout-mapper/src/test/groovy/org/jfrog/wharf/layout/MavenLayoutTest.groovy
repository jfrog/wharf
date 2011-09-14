package org.jfrog.wharf.layout

import org.jfrog.wharf.layout.base.ArtifactPathMapperFactoryImpl
import spock.lang.Specification

/**
 * Date: 9/12/11
 * Time: 3:39 PM
 * @author Fred Simon
 */
class MavenLayoutTest extends Specification {
    def "basic Maven artifact mapper"() {
        def factory = new ArtifactPathMapperFactoryImpl()
        def mapper = factory.createMavenMapper("root")

        expect:
        def artifact = mapper.fromMap(maps)
        artifact.isValid()
        fields.every { k, v -> artifact.get(k) == v }
        mapper.toPath(artifact) == paths
        def artFromPath = mapper.fromPath(paths)
        artFromPath.isValid()
        fields.every { k, v -> artFromPath.get(k) == v }

        where:
        maps << [
                [groupId: "com.acme", artifactId: "myMod", version: "1.0"],
                [groupId: "com.acme", artifactId: "myMod", version: "1.0", ext: "pom"],
                [groupId: "com.acme", artifactId: "myMod", version: "1.0", classifier: "sources"],
                [groupId: "com.acme", artifactId: "myMod", version: "1.0", classifier: "javadoc"],
                [groupId: "com.acme", artifactId: "myMod", version: "1.0-SNAPSHOT"],
                [groupId: "com.acme", artifactId: "myMod", baseRev: "1.0", status: "integration"],
                [groupId: "path.to", artifactId: "artifact", fileItegRev: "20110824.072445-1", baseRev: "8.2.1-m13", status: "integration"],
                [groupId: "org.test", artifactId: "Builder", fileItegRev: "20110614.154032-1", baseRev: "14062011-013758GMT-plugged", status: "integration", classifier: "javadoc", ext: "zip"]
        ]
        paths << [
                "root/com/acme/myMod/1.0/myMod-1.0.jar",
                "root/com/acme/myMod/1.0/myMod-1.0.pom",
                "root/com/acme/myMod/1.0/myMod-1.0-sources.jar",
                "root/com/acme/myMod/1.0/myMod-1.0-javadoc.jar",
                "root/com/acme/myMod/1.0-SNAPSHOT/myMod-1.0-SNAPSHOT.jar",
                "root/com/acme/myMod/1.0-SNAPSHOT/myMod-1.0-SNAPSHOT.jar",
                "root/path/to/artifact/8.2.1-m13-SNAPSHOT/artifact-8.2.1-m13-20110824.072445-1.jar",
                "root/org/test/Builder/14062011-013758GMT-plugged-SNAPSHOT/Builder-14062011-013758GMT-plugged-20110614.154032-1-javadoc.zip"
        ]
        fields << [
                [orgPath: "com/acme", module: "myMod", artifact: "myMod", extension: "jar", baseRev: "1.0", type: "jar", status: "release"],
                [org: "com.acme", module: "myMod", artifact: "myMod", extension: "pom", baseRev: "1.0", type: "pom", status: "release"],
                [orgPath: "com/acme", module: "myMod", artifact: "myMod", extension: "jar", baseRev: "1.0", type: "source", status: "release", classifier: "sources"],
                [orgPath: "com/acme", module: "myMod", artifact: "myMod", extension: "jar", baseRev: "1.0", type: "jar", status: "release", classifier: "javadoc"],
                [orgPath: "com/acme", module: "myMod", artifact: "myMod", extension: "jar", revision: "1.0-SNAPSHOT", baseRev: "1.0", status: "integration"],
                [orgPath: "com/acme", module: "myMod", artifact: "myMod", extension: "jar", revision: "1.0-SNAPSHOT", baseRev: "1.0", status: "integration"],
                [org: "path.to", module: "artifact", revision: "8.2.1-m13-20110824.072445-1", baseRev: "8.2.1-m13", status: "integration", folderItegRev: "SNAPSHOT"],
                [org: "org.test", module: "Builder", revision: "14062011-013758GMT-plugged-20110614.154032-1", fileItegRev: "20110614.154032-1", baseRev: "14062011-013758GMT-plugged", status: "integration", classifier: "javadoc", ext: "zip", folderItegRev: "SNAPSHOT"]
        ]
    }

    def "invalid Maven artifacts from map"() {
        def factory = new ArtifactPathMapperFactoryImpl()
        def mapper = factory.createMavenMapper("bad-root")

        expect:
        def artifact = mapper.fromMap(maps)
        !artifact.isValid()

        where:
        maps << [
                [groupId: "com.acme", artifact: "ivy"],
                [groupId: "com/acme", artifactId: "myMod", version: "1.0"],
                [groupId: "com.acme", version: "1.0", classifier: "sources"],
                [groupId: "com.acme", artifactId: "myMod", version: "1.0-SNAPSHOT", baseRev: "1.1"],
                [groupId: "com.acme", artifactId: "myMod", version: "1.0-NOTGOODPATTERN", baseRev: "1.0", status: "integration"],
                [groupId: "path.to", artifactId: "artifact", revision: "8.2.1-m13-SNAPSHOT", baseRev: "8.2.2-m13"],
                [groupId: "path.to", artifactId: "artifact", revision: "8.2.1-m13-SNAPSHOT", fileItegRev: "20110824.072445-1", baseRev: "8.2.1-m13", status: "integration"],
                [groupId: "org.test", artifactId: "Builder", fileItegRev: "20110614.154032-1", baseRev: "14062011-013758GMT-plugged", status: "release", classifier: "javadoc", ext: "zip"],
                [groupId: "org.test", artifactId: "Builder", folderItegRev: "not-snapshot", fileItegRev: "20110614.154032-1", baseRev: "14062011-013758GMT-plugged", status: "release"]
        ]
    }

    def "invalid Maven paths"() {
        def factory = new ArtifactPathMapperFactoryImpl()
        def mapper = factory.createMavenMapper("bad-root")

        expect:
        def artFromPath = mapper.fromPath(paths)
        !artFromPath.isValid()

        where:
        paths << [
                "root/com/acme/not-myMod/1.0/myMod-1.0.jar",
                "root/com/acme/myMod/1.0-not/myMod-1.0.pom",
                "bad-root/path/to/artifact/8.2.1-m13-SNAPSHOT/artifact-8.2.1-m13.jar"
        ]
    }
}
