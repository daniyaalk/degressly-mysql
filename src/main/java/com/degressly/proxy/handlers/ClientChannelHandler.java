package com.degressly.proxy.handlers;

import com.degressly.proxy.mysql.MySQLConnectionState;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Scope("prototype")
@Component
public class ClientChannelHandler extends ChannelInboundHandlerAdapter {

	private MySQLConnectionState mySQLConnectionState;

	@Autowired
	private ApplicationContext applicationContext;

	public ClientChannelHandler(MySQLConnectionState mySQLConnectionState) {
		this.mySQLConnectionState = mySQLConnectionState;
	}

	@PostConstruct
	public void init() {
		mySQLConnectionState = applicationContext.getBean(MySQLConnectionState.class, mySQLConnectionState.getId());
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		log.info("Channel active: {}", ctx);
		mySQLConnectionState.setClientChannel(ctx.channel());
		var clientChannel = mySQLConnectionState.getClientChannel();

		Bootstrap b = new Bootstrap();
		b.group(clientChannel.eventLoop())
			.channel(clientChannel.getClass())
			.handler(applicationContext.getBean(RemoteChannelHandler.class, mySQLConnectionState));
		ChannelFuture f = b.connect("localhost", 3307);
		var remoteChannel = f.channel();
		mySQLConnectionState.setRemoteChannel(remoteChannel);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ByteBuf recv = (ByteBuf) msg;
		byte[] byteArray = new byte[recv.readableBytes()];
		int i = 0;

		while (recv.isReadable()) {
			byteArray[i] = recv.readByte();
			i++;
		}
		// String hexchars = HexFormat.ofDelimiter(",
		// ").withPrefix("#").formatHex(byteArray);
		recv.release();

		// log.info("Client channel input: {}", new String(byteArray));
		// log.info("Hex: {}", hexchars);
		// log.info("Packet: {}", mySQLClientPacketDecoderService.process(byteArray));
		mySQLConnectionState.processClientMessage(byteArray);

		ByteBuf send = ctx.alloc().buffer(byteArray.length);
		send.writeBytes(byteArray);
		mySQLConnectionState.getRemoteChannel().writeAndFlush(send);
	}

}
