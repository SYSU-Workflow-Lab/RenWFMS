package org.sysu.renResourcing.context.contextService;

import org.springframework.stereotype.Service;
import org.sysu.renCommon.enums.WorkQueueContainerType;
import org.sysu.renResourcing.GlobalContext;
import org.sysu.renResourcing.context.WorkQueueContainer;

/**
 * Created by Skye on 2018/12/22.
 * <p>
 * Usage : WorkQueueContainer Handler.
 */

@Service
public class WorkQueueContainerService {

    /**
     * Get the queue container of a specific worker.
     *
     * @param workerId worker global id, {@code GlobalContext.WORKQUEUE_ADMIN_PREFIX} if admin user
     * @return Work queue container of this worker
     */
    public WorkQueueContainer GetContext(String workerId) {
        WorkQueueContainer retContainer;
        // admin queue
        if (workerId.startsWith(GlobalContext.WORKQUEUE_ADMIN_PREFIX)) {
            retContainer = new WorkQueueContainer(workerId, WorkQueueContainerType.AdminSet);
        }
        // participant queue
        else {
            retContainer = new WorkQueueContainer(workerId, WorkQueueContainerType.ParticipantSet);
        }
        return retContainer;
    }
}
