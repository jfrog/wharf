package org.jfrog.wharf.layout

import spock.lang.*
import org.jfrog.wharf.layout.base.ArtifactPathMapperFactoryImpl

/**
 * Date: 9/12/11
 * Time: 3:39 PM
 * @author Fred Simon
 */
class MavenLayoutTest extends Specification {
    def "basic Maven artifact info builder"() {
        def factory = new ArtifactPathMapperFactoryImpl()
        def mapper = factory.createMavenMapper("root")

        expect:
        def artifact = mapper.fromMap(maps)
        fields.every { k,v -> artifact.get(k) == v }
        mapper.toPath(artifact) == paths

        where:
        maps << [
                [groupId:"com.acme", artifactId:"myMod", version:"1.0"],
                [groupId:"com.acme", artifactId:"myMod", version:"1.0", ext:"pom"],
                [groupId:"com.acme", artifactId:"myMod", version:"1.0", classifier:"sources"],
                [groupId:"com.acme", artifactId:"myMod", version:"1.0", classifier:"javadoc"],
                [groupId:"com.acme", artifactId:"myMod", version:"1.0-SNAPSHOT"],
                [groupId:"com.acme", artifactId:"myMod", baseRev:"1.0", status: "integration"]
        ]
        paths << [
                "root/com/acme/myMod/1.0/myMod-1.0.jar",
                "root/com/acme/myMod/1.0/myMod-1.0.pom",
                "root/com/acme/myMod/1.0/myMod-1.0-sources.jar",
                "root/com/acme/myMod/1.0/myMod-1.0-javadoc.jar",
                "root/com/acme/myMod/1.0-SNAPSHOT/myMod-1.0-SNAPSHOT.jar",
                "root/com/acme/myMod/1.0-SNAPSHOT/myMod-1.0-SNAPSHOT.jar"
        ]
        fields << [
                [orgPath:"com/acme", module:"myMod", artifact:"myMod", extension:"jar", baseRev:"1.0", type:"jar", status:"release"],
                [org:"com.acme", module:"myMod", artifact:"myMod", extension:"pom", baseRev:"1.0", type:"pom", status:"release"],
                [orgPath:"com/acme", module:"myMod", artifact:"myMod", extension:"jar", baseRev:"1.0", type:"source", status:"release", classifier:"sources"],
                [orgPath:"com/acme", module:"myMod", artifact:"myMod", extension:"jar", baseRev:"1.0", type:"jar", status:"release", classifier:"javadoc"],
                [orgPath:"com/acme", module:"myMod", artifact:"myMod", extension:"jar", revision: "1.0-SNAPSHOT", baseRev:"1.0", status:"integration"],
                [orgPath:"com/acme", module:"myMod", artifact:"myMod", extension:"jar", revision: "1.0-SNAPSHOT", baseRev:"1.0", status:"integration"]
        ]
    }
}
