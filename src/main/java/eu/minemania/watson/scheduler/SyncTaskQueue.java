package eu.minemania.watson.scheduler;

import java.util.concurrent.ConcurrentLinkedQueue;

public class SyncTaskQueue
{
    private static final SyncTaskQueue INSTANCE = new SyncTaskQueue();
    protected ConcurrentLinkedQueue<Runnable> _taskQueue = new ConcurrentLinkedQueue<>();

    public static SyncTaskQueue getInstance()
    {
        return INSTANCE;
    }

    public void addTask(Runnable task)
    {
        _taskQueue.add(task);
    }

    private static final int MAX_TASKS_PER_TICK = 200;

    public void runTasks()
    {
        int processed = 0;
        while (processed < MAX_TASKS_PER_TICK)
        {
            Runnable task = _taskQueue.poll();
            if (task == null) break;
            task.run();
            processed++;
        }
    }
}