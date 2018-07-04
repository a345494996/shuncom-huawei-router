package com.shuncom.tcp.server.gateway;

import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;
import com.shuncom.util.CustomizedThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class Server {
	public static final Logger logger = LoggerFactory.getLogger(Server.class);
	public static final String GROUP = Server.class.getName() + ".GROUP";
	public static final String ATTRIBUTE_KEY_CHANNEL_CONTEXT = Server.class.getName() + ".ATTRIBUTE_KEY_CHANNEL_CONTEXT";
	private int port = 8091;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private EventExecutorGroup businessGroup;
	
	public Server() {
		this(8091);
	}
	
	public Server(int port) {
		this(port, 1, 3, new DefaultEventExecutorGroup(5, new CustomizedThreadFactory("gateway-handler-")));
	}
	
	public Server(int port, int bossEventLoops, int workerEventLoops, EventExecutorGroup businessGroup) {
		this.port= port;
		this.bossGroup = new NioEventLoopGroup(bossEventLoops, new CustomizedThreadFactory("gateway-boss-"));
        this.workerGroup = new NioEventLoopGroup(workerEventLoops, new CustomizedThreadFactory("gateway-worker-"));
        this.businessGroup = businessGroup;
	}

	public void start() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
         .channel(NioServerSocketChannel.class)
         .childHandler(new ChannelInitializer<SocketChannel>() {
             @Override
             public void initChannel(SocketChannel ch) throws Exception {
            	 //idle handler(5分钟)
            	 ch.pipeline().addLast(new IdleStateHandler(120, 0, 0));
            	 //input
            	 ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1048576, 2, 2 ,6, 0));
                 ch.pipeline().addLast(new PacketDecoderHandler());
                 ch.pipeline().addLast(new InboundVerifier());
                 ch.pipeline().addLast(new InboundConverter());
                 ch.pipeline().addLast(businessGroup, new RequestHandler());
                 //output
                 ch.pipeline().addLast(new PacketEncoderHandler());
                 ch.pipeline().addLast(new OutboundVerifier());
                 ch.pipeline().addLast(new OutboundConverter());
             }
         })
         .handler(new ChannelInboundHandlerAdapter() {
        	@Override
    	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    	            throws Exception {
    	    	logger.error("{} : {} ;at {}", cause.getClass().getName(), cause.getMessage(), cause.getStackTrace()[0]);
    	    }
         })
         .option(ChannelOption.SO_BACKLOG, 128)
         .childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.bind(port).sync().channel();
        logger.info("Gateway server listern on {}", port);
    }
	
	public void stop() {
		logger.info("Server closed");
		
		if (!workerGroup.isShutdown()) {
			workerGroup.shutdownGracefully();
		}
		if (!bossGroup.isShutdown()) {
			bossGroup.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) throws Exception {
		new Server().start();
	}
}
