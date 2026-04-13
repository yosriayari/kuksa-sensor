package com.kuksa.client;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.grpc.ManagedChannel;
import kuksa.val.v2.Types;
import kuksa.val.v2.VALGrpc;
import kuksa.val.v2.Val;

public final class ServiceClient implements AutoCloseable {
    private final ManagedChannel channel;
    private final VALGrpc.VALBlockingStub blockStub;
    private final VALGrpc.VALStub asyncStub;
    private final long timeoutMs;

    public ServiceClient(ManagedChannel channel, long timeoutMs) {
        this.channel = channel;
        this.blockStub = VALGrpc.newBlockingStub(channel);
        this.asyncStub = VALGrpc.newStub(channel);
        this.timeoutMs = timeoutMs;
    }

    public Val.GetServerInfoResponse getServerInfo() {
        Val.GetServerInfoRequest req = Val.GetServerInfoRequest.newBuilder().build();

        return blockStub
                .withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS)
                .getServerInfo(req);
    }

    public Val.GetValueResponse getValueByPath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path is blank");
        }

        Types.SignalID id = Types.SignalID.newBuilder()
                .setPath(path)
                .build();

        Val.GetValueRequest req = Val.GetValueRequest.newBuilder()
                .setSignalId(id)
                .build();

        return blockStub
                .withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS)
                .getValue(req);
    }

    public Val.GetValuesResponse getValuesByPaths(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            throw new IllegalArgumentException("paths is empty");
        }

        Val.GetValuesRequest.Builder reqB = Val.GetValuesRequest.newBuilder();

        for (String path : paths) {
            if (path == null || path.isBlank()) {
                throw new IllegalArgumentException("path is blank");
            }

            Types.SignalID id = Types.SignalID.newBuilder()
                    .setPath(path)
                    .build();

            reqB.addSignalIds(id);
        }

        Val.GetValuesRequest req = reqB.build();

        return blockStub
                .withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS)
                .getValues(req);
    }

    public Val.PublishValueResponse publishValueByPath(String path, Types.Datapoint dp) {
        Types.SignalID id = Types.SignalID.newBuilder()
                .setPath(path)
                .build();

        Val.PublishValueRequest req = Val.PublishValueRequest.newBuilder()
                .setSignalId(id)
                .setDataPoint(dp)
                .build();

        return blockStub
                .withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS)
                .publishValue(req);
    }


    public void subscribeByPaths(
            Set<String> paths,
            Consumer<Val.SubscribeResponse> onMessage,
            Consumer<Throwable> onError,
            Runnable onCompleted
    ) {
        Val.SubscribeRequest.Builder reqB = Val.SubscribeRequest.newBuilder();

        for (String p : paths) {
            reqB.addSignalPaths(p);
        }

        reqB.setBufferSize(1000);

        Val.SubscribeRequest req = reqB.build();

        asyncStub.subscribe(req, new io.grpc.stub.StreamObserver<>() {
            @Override public void onNext(Val.SubscribeResponse value) { onMessage.accept(value); }
            @Override public void onError(Throwable error) { onError.accept(error); }
            @Override public void onCompleted() { onCompleted.run(); }
        });
    }

    @Override
    public void close() {
        channel.shutdown();
        try {
            channel.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        channel.shutdownNow();
    }
}