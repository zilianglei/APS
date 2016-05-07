package se.natusoft.osgi.aps.tools.services;

import se.natusoft.osgi.aps.tools.annotation.activator.ExecutorSvc;
import se.natusoft.osgi.aps.tools.annotation.activator.Managed;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider;
import se.natusoft.osgi.aps.tools.annotation.activator.Schedule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ConstantConditions")
@OSGiServiceProvider
public class APSActivatorExecutorServiceTestSvc implements TestService {

    @Managed
    @ExecutorSvc(parallelism = 5, type = ExecutorSvc.ExecutorType.Cached, unConfigurable = true)
    private ExecutorService fixed;

    @Managed(name = "scheduled")
    @ExecutorSvc(parallelism = 15, type = ExecutorSvc.ExecutorType.Scheduled)
    private ScheduledExecutorService sched;

    private boolean scheduledServiceRun1 = false;
    private boolean scheduledServiceRun2 = false;

    @Schedule(on = "scheduled", delay = 1, repeat = 1, timeUnit = TimeUnit.SECONDS)
    private Runnable myRunnable1 = () -> {
        System.out.println("Being run!");
        scheduledServiceRun1 = true;
    };

    // This will use an internal ScheduledExecutionService.
    @Schedule(delay = 1, repeat = 1, timeUnit = TimeUnit.SECONDS)
    private Runnable myRunnable2 = () -> {
        System.out.println("Being run 2!");
        scheduledServiceRun2 = true;
    };

    @Override
    public String getServiceInstanceInfo() throws Exception {
        Thread.sleep(2000);

        String status = "OK";

        if (this.fixed == null || this.sched == null) {
            status = "BAD";
        }

        // Verify that we have a delegate for fixed.
        if (!this.fixed.getClass().getName().equals("java.util.concurrent.Executors$DelegatedExecutorService")) {
            status = this.fixed.getClass().getName();
        }

        // Verify that we have a scheduled executor service for sched.
        if (!this.sched.getClass().getName().equals("java.util.concurrent.ScheduledThreadPoolExecutor")) {
            status = this.sched.getClass().getName();
        }

        if (!this.scheduledServiceRun1 ) {
            status = "Scheduled runnable 1 was never run!";
        }
        if (!this.scheduledServiceRun2 ) {
            status += "Scheduled runnable 2 was never run!";
        }

        return status;
    }
}