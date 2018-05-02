package edu.scut.cs.hm.common.http.async;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;

class ByteBufHolderAdapter implements ChunkedInputStream.Adapter<ByteBufHolder> {

    static final ByteBufHolderAdapter INSTANCE = new ByteBufHolderAdapter();

    private ByteBufHolderAdapter() {
    }

    @Override
    public void onAdd(ByteBufHolder chunk) {
        chunk.retain();
    }

    @Override
    public void onRemove(ByteBufHolder chunk) {
        chunk.release();
    }

    @Override
    public int readByte(ByteBufHolder chunk) {
        ByteBuf buf = chunk.content();
        if (buf.readableBytes() == 0) {
            return ChunkedInputStream.EOF;
        }
        return buf.readByte();
    }

    @Override
    public int readBytes(ByteBufHolder chunk, byte[] arr, int off, int len) {
        ByteBuf buf = chunk.content();
        int avail = buf.readableBytes();
        if (avail == 0) {
            return ChunkedInputStream.EOF;
        }
        int readed = Math.min(len, avail);
        buf.readBytes(arr, off, readed);
        return readed;
    }
}
