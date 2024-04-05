package com.degressly.proxy.server;

import com.degressly.proxy.handlers.ClientChannelHandler;
import com.degressly.proxy.mysql.MySQLConnectionState;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class MysqlProxyChannelInitializer extends ChannelInitializer<SocketChannel> {

	@Autowired
	AtomicLong taskId;

	@Autowired
	ApplicationContext applicationContext;

	@Override
	protected void initChannel(SocketChannel ch) {
		log.info("Channel initialized");

		MySQLConnectionState mySQLConnectionState = applicationContext.getBean(MySQLConnectionState.class,
				taskId.getAndIncrement());

		ClientChannelHandler bean = applicationContext.getBean(ClientChannelHandler.class, mySQLConnectionState);
		log.info("Bean initialized: {}", bean);
		ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO), bean);
	}

}
