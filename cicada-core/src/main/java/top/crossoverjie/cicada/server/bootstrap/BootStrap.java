package top.crossoverjie.cicada.server.bootstrap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import top.crossoverjie.cicada.base.log.LoggerBuilder;
import top.crossoverjie.cicada.server.bean.CicadaBeanManager;
import top.crossoverjie.cicada.server.config.AppConfig;
import top.crossoverjie.cicada.server.configuration.ApplicationConfiguration;
import top.crossoverjie.cicada.server.constant.CicadaConstant;
import top.crossoverjie.cicada.server.context.CicadaContext;
import top.crossoverjie.cicada.server.init.CicadaInitializer;
import top.crossoverjie.cicada.server.thread.ThreadLocalHolder;

import static top.crossoverjie.cicada.server.configuration.ConfigurationHolder.getConfiguration;
import static top.crossoverjie.cicada.server.constant.CicadaConstant.SystemProperties.APPLICATION_THREAD_SHUTDOWN_NAME;
import static top.crossoverjie.cicada.server.constant.CicadaConstant.SystemProperties.APPLICATION_THREAD_WORK_NAME;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 2018/9/10 21:56
 * @since JDK 1.8
 */
public class BootStrap {

    private final static Logger LOGGER = LoggerBuilder.getLogger(BootStrap.class);

    private static AppConfig appConfig = AppConfig.getInstance() ;
    private static EventLoopGroup boss = new NioEventLoopGroup(1,new DefaultThreadFactory("boss"));
    private static EventLoopGroup work = new NioEventLoopGroup(0,new DefaultThreadFactory(APPLICATION_THREAD_WORK_NAME));
    private static Channel channel ;

    /**
     * Start netty Server
     *
     * @throws Exception
     */
    public static void startCicada() throws Exception {
        // start
        startServer();

        // register shutdown hook
        shutDownServer();

        // synchronized channel
        joinServer();
    }

    /**
     * start netty server
     * @throws InterruptedException
     */
    private static void startServer() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(boss, work)
                .channel(NioServerSocketChannel.class)
                .childHandler(new CicadaInitializer());

        ChannelFuture future = bootstrap.bind(AppConfig.getInstance().getPort()).sync();
        if (future.isSuccess()) {
            appLog();
        }
        channel = future.channel();
    }

    private static void joinServer() throws Exception {
        channel.closeFuture().sync();
    }

    private static void appLog() {
        Long start = ThreadLocalHolder.getLocalTime();
        ApplicationConfiguration applicationConfiguration = (ApplicationConfiguration) getConfiguration(ApplicationConfiguration.class);
        long end = System.currentTimeMillis();
        LOGGER.info("Cicada started on port: {}.cost {}ms", applicationConfiguration.get(CicadaConstant.CICADA_PORT), end - start);
        LOGGER.info(">> access http://{}:{}{} <<","127.0.0.1",appConfig.getPort(),appConfig.getRootPath());
    }

    /**
     * shutdown server
     */
    private static void shutDownServer() {
        ShutDownThread shutDownThread = new ShutDownThread();
        shutDownThread.setName(APPLICATION_THREAD_SHUTDOWN_NAME);
        Runtime.getRuntime().addShutdownHook(shutDownThread);
    }

    private static class ShutDownThread extends Thread {
        @Override
        public void run() {
            LOGGER.info("Cicada server stop...");
            CicadaContext.removeContext();

            CicadaBeanManager.getInstance().releaseBean();

            boss.shutdownGracefully();
            work.shutdownGracefully();

            LOGGER.info("Cicada server has been successfully stopped.");
        }

    }
}
