package com.geostax.scheduler.core.thread;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.geostax.scheduler.core.enums.RegistryConfig;
import com.geostax.scheduler.core.model.RegistryParam;
import com.geostax.scheduler.core.model.ReturnT;
import com.geostax.scheduler.core.util.AdminApiUtil;
import com.geostax.scheduler.core.util.IpUtil;

/**
 * Created by xuxueli on 17/3/2.
 */
public class ExecutorRegistryThread extends Thread {
    private static Logger logger = LoggerFactory.getLogger(ExecutorRegistryThread.class);

    private static ExecutorRegistryThread instance = new ExecutorRegistryThread();
    public static ExecutorRegistryThread getInstance(){
        return instance;
    }

    private Thread registryThread;
    private boolean toStop = false;
    public void start(final int port, final String ip, final String appName){

        // valid
        if ( !(AdminApiUtil.allowCallApi() && (appName!=null && appName.trim().length()>0)) ) {
            logger.warn(">>>>>>>>>>>> xxl-job, executor registry config fail");
            return;
        }

        // executor address (generate addredd = ip:port)
        final String executorAddress;
        if (ip != null && ip.trim().length()>0) {
            executorAddress = ip.trim().concat(":").concat(String.valueOf(port));
        } else {
            executorAddress = IpUtil.getIpPort(port);
        }

        registryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!toStop) {
                    try {
                        RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(), appName, executorAddress);
                        ReturnT<String> registryResult = AdminApiUtil.callApiFailover(AdminApiUtil.REGISTRY, registryParam);
                        logger.info(">>>>>>>>>>> xxl-job Executor registry {}, RegistryParam:{}, registryResult:{}",
                                new Object[]{(registryResult.getCode()==ReturnT.SUCCESS_CODE?"success":"fail"), registryParam.toString(), registryResult.toString()});
                    } catch (Exception e) {
                        logger.error(">>>>>>>>>>> xxl-job ExecutorRegistryThread Exception:", e);
                    }

                    try {
                        TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });
        registryThread.setDaemon(true);
        registryThread.start();
    }

    public void toStop() {
        toStop = true;
    }

}
