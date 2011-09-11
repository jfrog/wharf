package org.jfrog.wharf.ivy;

import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.jfrog.wharf.ivy.resolver.IBiblioWharfResolver;
import org.junit.Test;

/**
 * User: freds
 * Date: 7/27/11
 * Time: 2:34 AM
 */
public class ChecksumCheckTest extends AbstractDependencyResolverTest {

    @Test
    public void testIBiblioWharfResolver() throws Exception {
        IBiblioWharfResolver central = createIBiblioResolver("central", "http://repo1.maven.org/maven2");
        downloadAndCheck(ModuleRevisionId.newInstance("org.apache.ant", "ant-parent", "1.7.1"), central, 0);
        downloadAndCheck(ModuleRevisionId.newInstance("com.google.inject", "guice", "2.0"), central, 1);
    }

}
