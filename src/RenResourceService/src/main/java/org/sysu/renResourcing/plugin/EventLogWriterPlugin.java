/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renResourcing.plugin;

import org.sysu.renCommon.enums.LogLevelType;
import org.sysu.renResourcing.entity.RenRseventlogEntity;
import org.sysu.renResourcing.dao.RenRseventlogEntityDAO;
import org.sysu.renResourcing.utility.LogUtil;
import org.sysu.renResourcing.utility.SpringContextUtil;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Author: Rinkako
 * Date  : 2018/2/10
 * Usage : A log writer can run asynchronously.
 */
public class EventLogWriterPlugin extends AsyncRunnablePlugin {

    /**
     * Running flag.
     */
    private boolean isRunning = false;

    /**
     * Event queue for pending for writing.
     */
    private ConcurrentLinkedQueue<RenRseventlogEntity> logEvtQueue = new ConcurrentLinkedQueue<>();

    /**
     * Create a new log writer.
     */
    public EventLogWriterPlugin() { }

    /**
     * Add a log entity to be written later.
     * @param eventEntity log entity
     * @param rtid process rtid
     */
    public void AddEvent(RenRseventlogEntity eventEntity, String rtid) {
        if (this.isRunning) {
            LogUtil.Log(String.format("Try to add event to a running writer, ignored. (Wid: %s, Pid: %s, WorkerId: %s, Evt: %s)",
                    eventEntity.getWid(), eventEntity.getProcessid(), eventEntity.getWorkerid(), eventEntity.getEvent()),
                    EventLogWriterPlugin.class.getName(), LogLevelType.WARNING, rtid);
            return;
        }
        this.logEvtQueue.add(eventEntity);
    }

    /**
     * Run plugin asynchronously.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        this.isRunning = true;
        this.DoWrite();
    }

    /**
     * Write a log to entity asynchronously.
     */
    private void DoWrite() {
        if (logEvtQueue == null || logEvtQueue.isEmpty()) {
            return;
        }
        while (!logEvtQueue.isEmpty()) {
            RenRseventlogEntity eventEntity = logEvtQueue.poll();
            try {
                RenRseventlogEntityDAO renRseventlogEntityDAO = (RenRseventlogEntityDAO) SpringContextUtil.getBean("renRseventlogEntityDAO");
                renRseventlogEntityDAO.saveOrUpdate(eventEntity);
            }
            catch (Exception ex) {
                LogUtil.Echo(String.format("Fail to insert RS event log to entity. (Wid: %s, Pid: %s, WorkerId: %s, Evt: %s), %s",
                        eventEntity.getWid(), eventEntity.getProcessid(), eventEntity.getWorkerid(), eventEntity.getEvent(), ex),
                        EventLogWriterPlugin.class.getName(), LogLevelType.ERROR);
            }
        }
    }
}
