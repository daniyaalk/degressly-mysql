package com.degressly.proxy.handlers;

import com.degressly.proxy.mysql.MySQLConnectionState;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HexFormat;

@Slf4j
@Component
@Scope("prototype")
public class RemoteChannelHandler extends ChannelInboundHandlerAdapter {

	private MySQLConnectionState mySQLConnectionState;

	public RemoteChannelHandler(MySQLConnectionState mySQLConnectionState) {
		this.mySQLConnectionState = mySQLConnectionState;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		log.info("Remote channel active: {}", ctx);
		var remoteChannel = ctx.channel();
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
		recv.release();
		String hexChars = HexFormat.ofDelimiter(", ").withPrefix("#").formatHex(byteArray);

		log.info("Remote channel input: {}", new String(byteArray));
		log.info("Hex string: {}", hexChars);

		mySQLConnectionState.processRemoteMessage(byteArray);

		ByteBuf send = ctx.alloc().buffer(byteArray.length);
		send.writeBytes(byteArray);
		mySQLConnectionState.getClientChannel().writeAndFlush(send);
	}

}
