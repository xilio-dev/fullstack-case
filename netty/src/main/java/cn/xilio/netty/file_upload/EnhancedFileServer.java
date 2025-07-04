package cn.xilio.netty.file_upload;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class EnhancedFileServer {
    // 认证令牌缓存 (令牌 -> 过期时间)
    private static final Map<String, Long> authTokens = new ConcurrentHashMap<>();
    // 文件上传状态缓存 (客户端ID -> 上传状态)
    private static final Map<String, FileUploadState> uploadStates = new ConcurrentHashMap<>();
    // 最大文件大小 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    // 令牌有效期 (5分钟)
    private static final long TOKEN_EXPIRE_TIME = TimeUnit.MINUTES.toMillis(5);

    public static void main(String[] args) throws Exception {
        // 定期清理过期令牌
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                cleanExpiredTokens();
            }
        }, 0, TimeUnit.MINUTES.toMillis(1));

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 添加字符串编解码器
                            pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
                            pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
                            // 添加对象编解码器
                            pipeline.addLast(new ObjectEncoder());
                            pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            // 添加认证处理器
                            pipeline.addLast(new AuthHandler());
                            // 添加文件上传处理器
                            pipeline.addLast(new FileUploadHandler());
                        }
                    });

            ChannelFuture future = bootstrap.bind(8080).sync();
            System.out.println("Enhanced File Server started on port 8080");
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    // 清理过期令牌
    private static void cleanExpiredTokens() {
        long now = System.currentTimeMillis();
        authTokens.entrySet().removeIf(entry -> entry.getValue() < now);
    }

    // 生成认证令牌
    private static String generateAuthToken() {
        String token = UUID.randomUUID().toString();
        authTokens.put(token, System.currentTimeMillis() + TOKEN_EXPIRE_TIME);
        return token;
    }

    // 验证令牌
    private static boolean validateToken(String token) {
        Long expireTime = authTokens.get(token);
        if (expireTime == null || expireTime < System.currentTimeMillis()) {
            return false;
        }
        return true;
    }

    // 文件上传状态
    private static class FileUploadState {
        String clientId;
        String fileName;
        String expectedMd5;
        long fileSize;
        long receivedSize;
        AsynchronousFileChannel fileChannel;
        MessageDigest md5Digest;

        public FileUploadState(String clientId, String fileName, String expectedMd5, long fileSize) throws Exception {
            this.clientId = clientId;
            this.fileName = fileName;
            this.expectedMd5 = expectedMd5;
            this.fileSize = fileSize;
            this.receivedSize = 0;
            this.md5Digest = MessageDigest.getInstance("MD5");

            // 创建目标文件
            File dir = new File("uploads");
            if (!dir.exists()) {
                dir.mkdir();
            }
            Path path = Paths.get("uploads", fileName);
            this.fileChannel = AsynchronousFileChannel.open(
                    path,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
        }

        // 写入数据块
        public synchronized void writeChunk(byte[] chunk, long position) {
            try {
                ByteBuffer buffer = ByteBuffer.wrap(chunk);
                fileChannel.write(buffer, position).get();
                md5Digest.update(chunk);
                receivedSize += chunk.length;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 完成上传
        public String complete() throws Exception {
            fileChannel.close();
            byte[] digest = md5Digest.digest();
            String actualMd5 = bytesToHex(digest);

            if (!actualMd5.equalsIgnoreCase(expectedMd5)) {
                throw new RuntimeException("MD5校验失败");
            }

            return actualMd5;
        }

        // 清理资源
        public void cleanup() {
            try {
                if (fileChannel != null) {
                    fileChannel.close();
                }
                if (receivedSize < fileSize) {
                    // 删除未完成上传的文件
                    new File("uploads", fileName).delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 字节数组转十六进制字符串
        private String bytesToHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
    }

    // 认证处理器
    private static class AuthHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            if ("AUTH_REQUEST\n".equals(msg)) {
                // 生成并返回认证令牌
                String token = generateAuthToken();
                ctx.writeAndFlush("AUTH_TOKEN:" + token);
            } else if (msg.startsWith("AUTH:")) {
                // 验证客户端令牌
                String token = msg.substring(5);
                if (validateToken(token)) {
                    ctx.writeAndFlush("AUTH_SUCCESS");
                    // 认证通过后移除当前处理器 只是当前连接的认证处理器 其他channel不受影响，因为一个连接分配一个channel
                    ctx.pipeline().remove(this);
                } else {
                    ctx.writeAndFlush("AUTH_FAILED");
                    ctx.close();
                }
            } else {
                // 未认证的请求
                ctx.writeAndFlush("AUTH_REQUIRED");
                ctx.close();
            }
        }
    }

    // 文件上传处理器
    private static class FileUploadHandler extends SimpleChannelInboundHandler<Object> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof FileUploadRequest) {
                // 处理文件上传请求
                FileUploadRequest request = (FileUploadRequest) msg;

                // 检查文件大小限制
                if (request.fileSize > MAX_FILE_SIZE) {
                    ctx.writeAndFlush("ERROR:File too large");
                    return;
                }

                // 初始化上传状态
                String clientId = ctx.channel().id().asShortText();
                FileUploadState state = new FileUploadState(
                        clientId,
                        request.fileName,
                        request.md5,
                        request.fileSize);

                uploadStates.put(clientId, state);
                ctx.writeAndFlush("READY:" + request.fileName);

            } else if (msg instanceof FileChunk) {
                // 处理文件数据块
                String clientId = ctx.channel().id().asShortText();
                FileUploadState state = uploadStates.get(clientId);

                if (state == null) {
                    ctx.writeAndFlush("ERROR:Upload not initialized");
                    return;
                }

                FileChunk chunk = (FileChunk) msg;

                // 检查是否超出预期文件大小
                if (state.receivedSize + chunk.data.length > state.fileSize) {
                    state.cleanup();
                    uploadStates.remove(clientId);
                    ctx.writeAndFlush("ERROR:File size exceeded");
                    return;
                }

                // 使用AsynchronousFileChannel异步写入文件
                state.writeChunk(chunk.data, state.receivedSize);

                // 返回进度
                int progress = (int) (state.receivedSize * 100 / state.fileSize);
                ctx.writeAndFlush("PROGRESS:" + progress);

                // 检查是否完成
                if (state.receivedSize >= state.fileSize) {
                    String actualMd5 = state.complete();
                    uploadStates.remove(clientId);
                    ctx.writeAndFlush("COMPLETE:" + actualMd5);
                }
            } else {
                ctx.writeAndFlush("ERROR:Invalid message type");
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            // 清理未完成的上传
            String clientId = ctx.channel().id().asShortText();
            FileUploadState state = uploadStates.remove(clientId);
            if (state != null) {
                state.cleanup();
            }
            super.channelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }

    // 文件上传请求对象
    public static class FileUploadRequest implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public String fileName;
        public String md5;
        public long fileSize;
    }

    // 文件数据块对象
    public static class FileChunk implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public byte[] data;
        public int chunkIndex;
    }
}
