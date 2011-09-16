package org.jfrog.wharf.ivy.http;

import org.jfrog.wharf.ivy.handler.WharfUrlHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
* Date: 9/16/11
* Time: 3:28 PM
*
* @author Fred Simon
*/
class MyTracer implements WharfUrlHandler.TraceCounter {
    final boolean shouldNotDownload;
    final boolean shouldNotSendHeads;
    final Map<String, List<Integer>> counter = new HashMap<String, List<Integer>>();

    MyTracer(boolean shouldNotDownload) {
        this.shouldNotDownload = shouldNotDownload;
        this.shouldNotSendHeads = false;
    }

    MyTracer(boolean shouldNotDownload, boolean shouldNotSendHeads) {
        this.shouldNotDownload = shouldNotDownload;
        this.shouldNotSendHeads = shouldNotSendHeads;
    }

    public void add(String query, int status) {
        if (shouldNotSendHeads) {
            // TODO: Maven metadata xml file is directly (and all the time) open by Ivy in org.apache.ivy.plugins.resolver.IBiblioResolver.findSnapshotVersion()
            // TODO: Cache the 404, 409 answers
            if (query.endsWith("maven-metadata.xml") || status == 404 || status == 409) {
                System.out.println("Try to avoid head query " + query + " got " + status);
            } else {
                fail("Query " + query + " should not happen with status " + status);
            }
        }
        assertFalse("Query " + query + " should not happen!", shouldNotDownload && query.startsWith("GET") && status == 200);
        List<Integer> count = counter.get(query);
        if (count == null) {
            count = new ArrayList<Integer>();
            counter.put(query, count);
        }
        count.add(status);
    }

    public void check() {
        // There should be only one status per URL, and every HEAD 200 should have a GET 200 in download mode
        for (Map.Entry<String, List<Integer>> entry : counter.entrySet()) {
            assertEquals(1, entry.getValue().size());
            if (!shouldNotDownload) {
                String query = entry.getKey();
                if (query.startsWith("HEAD") && entry.getValue().get(0) == 200) {
                    String getQuery = query.replace("HEAD", "GET");
                    // TODO: Maven metadata xml file is directly (and all the time) open by Ivy in org.apache.ivy.plugins.resolver.IBiblioResolver.findSnapshotVersion()
                    // TODO: Find a way to clean resource handling of maven metadata xml
                    if (!getQuery.endsWith("maven-metadata.xml")) {
                        assertTrue("HEAD query " + query + " does not have a GET in " + counter, counter.containsKey(getQuery));
                        assertTrue("GET query for " + getQuery + " did not return 200", counter.get(getQuery).get(0) == 200);
                    }
                }
            }
        }
    }
}
