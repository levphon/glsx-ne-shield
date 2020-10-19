package cn.com.glsx.notify.netty;

import cn.com.glsx.notify.netty.utils.NettyByteAndStringUtils;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.util.Objects;

@Slf4j
public class NettySendThread implements Runnable {

    private ChannelHandlerContext ctx;

    private String data;

    public NettySendThread(ChannelHandlerContext ctx, String data) {
        this.ctx = ctx;
        this.data = data;
    }

    @Override
    public void run() {
        String bcc = NettyDataSvervice.sendData(data);
        log.info("接收需要返回的消息：" + bcc);
        if (Objects.isNull(bcc)) {
            return;
        }
        //数据的发送与接收都是16进制
        byte[] sendData = NettyByteAndStringUtils.hexToByte(bcc);
        log.info("发送数据时间: " + LocalTime.now());
        ctx.writeAndFlush(sendData);
    }
}
