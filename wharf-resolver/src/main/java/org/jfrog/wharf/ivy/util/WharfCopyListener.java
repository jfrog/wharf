package org.jfrog.wharf.ivy.util;


import org.apache.ivy.util.CopyProgressEvent;
import org.apache.ivy.util.CopyProgressListener;
import org.apache.ivy.util.Message;

/**
 * @author Tomer Cohen
 */
public class WharfCopyListener implements CopyProgressListener {

    public void start(CopyProgressEvent evt) {
        Message.info(evt.toString());
    }

    public void progress(CopyProgressEvent evt) {
    }

    public void end(CopyProgressEvent evt) {
    }

}
