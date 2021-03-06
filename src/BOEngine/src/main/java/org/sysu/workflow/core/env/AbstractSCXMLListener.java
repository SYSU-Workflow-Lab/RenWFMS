
package org.sysu.workflow.core.env;

import org.sysu.workflow.core.SCXMLListener;
import org.sysu.workflow.core.model.EnterableState;
import org.sysu.workflow.core.model.Transition;
import org.sysu.workflow.core.model.TransitionTarget;

/**
 * An abstract adapter class for the <code>SXCMLListener</code> interface.
 * This class exists as a convenience for creating listener objects, and as
 * such all the methods in this class are empty.
 *
 * @since 0.7
 */
public abstract class AbstractSCXMLListener implements SCXMLListener {

    /**
     * @see SCXMLListener#onEntry(EnterableState)
     */
    public void onEntry(final EnterableState state) {
        // empty
    }

    /**
     * @see SCXMLListener#onExit(EnterableState)
     */
    public void onExit(final EnterableState state) {
        // empty
    }

    /**
     * @see SCXMLListener#onTransition(TransitionTarget, TransitionTarget, Transition, String)
     */
    public void onTransition(final TransitionTarget from,
                             final TransitionTarget to, final Transition transition, final String event) {
        // empty
    }

}

