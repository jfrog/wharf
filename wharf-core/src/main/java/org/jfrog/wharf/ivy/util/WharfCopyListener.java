package org.jfrog.wharf.ivy.util;


import org.apache.ivy.util.CopyProgressEvent;
import org.apache.ivy.util.CopyProgressListener;
import org.apache.ivy.util.Message;

/**
 * @author Tomer Cohen
 */
public class WharfCopyListener implements CopyProgressListener {

    @Override
    public void start(CopyProgressEvent evt) {
        Message.info(evt.toString());
    }

    @Override
    public void progress(CopyProgressEvent evt) {
    }

    @Override
    public void end(CopyProgressEvent evt) {
    }

}
