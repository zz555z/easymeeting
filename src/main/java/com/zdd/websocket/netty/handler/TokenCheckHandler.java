package com.zdd.websocket.netty.handler;

import com.zdd.component.RedisComponent;
import com.zdd.entry.dto.UserTokenDTO;
import com.zdd.websocket.netty.ChannelContext;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Slf4j
@Component
@ChannelHandler.Sharable   //多个处理器之间共享
public class TokenCheckHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private ChannelContext channelContext;


    /**
     * 重写channelRead0方法以处理接收到的FullHttpRequest
     * 该方法主要用于解析请求中的token参数，并进行有效性验证
     *
     * @param channelHandlerContext 上下文处理器，用于处理通道的状态
     * @param fullHttpRequest       完整的HTTP请求对象，包含请求的所有信息
     * @throws Exception 如果在处理请求时发生错误，抛出异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        // 获取请求的URI
        String uri = fullHttpRequest.getUri();
        // 使用QueryStringDecoder解析URI中的参数
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        // 获取token参数的值
        List<String> params = queryStringDecoder.parameters().get("token");
        // 检查token参数是否存在
        if (CollectionUtils.isEmpty(params)) {
            log.info("token 不存在。。。");
            // 如果token不存在，发送错误响应并终止处理
            sendErrorResponse(channelHandlerContext);
            return;
        }

        UserTokenDTO userTokenDTO = checkToken(params.get(0));
        // 进一步检查token的有效性
        if (ObjectUtils.isEmpty(userTokenDTO)){
            log.info("token 校验失败。。。");
            // 如果token无效，发送错误响应并终止处理
            sendErrorResponse(channelHandlerContext);
            return;
        }

        // 如果token验证通过，继续处理管道中的其他处理器
        channelHandlerContext.fireChannelRead(fullHttpRequest.retain());

        //todo 连接成功后初始化信息
        channelContext.addChannel(userTokenDTO.getUserId(), channelHandlerContext.channel());

    }


    private UserTokenDTO checkToken(String token) {

        return StringUtils.isEmpty(token) ? null : redisComponent.getUserTokenDTO(token);

    }


    /**
     * 发送错误响应给客户端
     * 当token无效时调用此方法，向客户端发送HTTP状态码为403（FORBIDDEN）的响应
     *
     * @param channelHandlerContext ChannelHandlerContext对象，用于处理和管理网络通信通道的上下文
     */
    private void sendErrorResponse(ChannelHandlerContext channelHandlerContext) {
        // 创建一个包含HTTP版本、状态码和错误信息的响应对象
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, Unpooled.copiedBuffer(" token invalid。。。", CharsetUtil.UTF_8));
        // 设置响应头，指定内容类型为文本，字符集为UTF-8
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        // 设置响应头，指定内容长度
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        // 将响应对象写入通道并冲刷，以确保数据被发送出去，同时添加监听器以在发送完成后关闭通道
        channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

}
