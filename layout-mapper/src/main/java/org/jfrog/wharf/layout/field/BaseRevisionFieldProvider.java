package org.jfrog.wharf.layout.field;

import static org.jfrog.wharf.layout.field.definition.ModuleRevisionFields.*;

/**
 * Date: 9/11/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public class BaseRevisionFieldProvider extends AnyRevisionFieldProvider {

    public BaseRevisionFieldProvider() {
        super(baseRev);
    }

}
