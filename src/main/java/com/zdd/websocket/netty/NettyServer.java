package com.zdd.websocket.netty;

import com.zdd.config.AppConfig;
import com.zdd.websocket.netty.handler.HearBeatHandler;
import com.zdd.websocket.netty.handler.TokenCheckHandler;
import com.zdd.websocket.netty.handler.WebsocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
@Slf4j
public class NettyServer implements Runnable {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private TokenCheckHandler tokenCheckHandler;

    @Autowired
    private WebsocketHandler websocketHandler;

    // 线程组 处理连接
    private EventLoopGroup bossGroup = new NioEventLoopGroup(2); // 接受连接
    // 线程组 处理业务
    private EventLoopGroup workerGroup = new NioEventLoopGroup(2); // 处理

    // 6s 收不到心跳会断开
    private static final Long handshakeTimeoutMillis = 6000L;

    @Override
    public void run() {
        try {
            // 初始化服务器配置
            ServerBootstrap bootstrap = new ServerBootstrap();
            // 配置服务器的线程组
            bootstrap.group(bossGroup, workerGroup)
                    // 指定Channel的类型
                    .channel(NioServerSocketChannel.class)
                    // 设置日志处理器
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    // 设置Channel的初始化器
                    .childHandler(new ChannelInitializer<Channel>() {

                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            // 获取Channel的Pipeline
                            ChannelPipeline pipeline = channel.pipeline();
                            // 对http的解码器，编码器
                            pipeline.addLast(new HttpServerCodec());
                            // http的消息聚合器，将分片的http消息聚合成完整的http消息
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            /**
                             * int readerIdleTimeSeconds,  一段时间未收到客户端消息
                             * int writerIdleTimeSeconds,  一段时间未给客户端发送消息
                             * int allIdleTimeSeconds      读写都无活动
                             */
                            pipeline.addLast(new IdleStateHandler(20, 0, 0));
                            pipeline.addLast(new HearBeatHandler());
                            /**
                             * token校验
                             */
                            pipeline.addLast(tokenCheckHandler);

                            /**
                             *  websocket 协议处理器
                             * String websocketPath, 路径
                             * String subprotocols,  指定支持的自协议
                             * boolean allowExtensions, 是否允许websocket扩展
                             * int maxFrameSize,       最大帧大小
                             * boolean allowMaskMismatch, 允许掩码不匹配
                             * boolean checkStartsWith, 是否允许路径开头匹配
                             * long handshakeTimeoutMillis   握手超时时间
                             */
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, true, 64 * 1024,
                                    true, true, handshakeTimeoutMillis));

                            // 添加websocket业务处理器
                            pipeline.addLast(websocketHandler);
                        }
                    });
            // 绑定端口并启动服务器
            Channel channel = bootstrap.bind(appConfig.getWsPort()).sync().channel();
            // 日志输出服务器启动成功信息
            log.info("启动 Netty 服务器...成功" + appConfig.getWsPort());
            // 等待服务器端口关闭
            channel.closeFuture().sync();
        } catch (Exception e) {
            // 日志输出服务器启动异常信息
            log.error("启动 Netty 服务器异常：{}", e);
        } finally {
            // 关闭主线程组
            bossGroup.shutdownGracefully();
            // 关闭工作线程组
            workerGroup.shutdownGracefully();
        }
    }


    @PreDestroy
    public void destroy() {
        log.info("关闭 Netty 服务器...");
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
