package com.kuksa.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;

public final class ChannelFactory {
    private ChannelFactory() {}

    public static ManagedChannel createChannel(String addr) {
        addr = addr.trim();

        if (addr.startsWith("unix:")) {
            if (!Epoll.isAvailable()) {
                throw new IllegalStateException("Epoll is not available on this system; UDS channel cannot be created.");
            }
            String path = addr.substring("unix:".length());
            if (path.startsWith("//")) path = path.substring(1); 
            if (!path.startsWith("/")) path = "/" + path;

            return NettyChannelBuilder
                    .forAddress(new DomainSocketAddress(path))
                    .channelType(EpollDomainSocketChannel.class)
                    .usePlaintext()
                    .overrideAuthority("localhost")
                    .build();
        }

        return ManagedChannelBuilder
                .forTarget(addr)
                .usePlaintext()
                .build();
    }
}
