package edu.scut.cs.hm.common.fc;

import lombok.Data;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
class QIndexFile implements AutoCloseable, IndexFile {
    private static final int HEADER_SIZE = FbUtils.SIGN_LEN + 1 + 1;
    private static final long MAX_SIZE = 1024 * 1024 /* 1MiB */;
    private static final byte TYPE = 0x10;
    private static final byte VERSION = 0x01;
    private final File file;
    private final List<String> list = new CopyOnWriteArrayList<>();
    private String id;
    private long maxFilesSize;
    private int maxFiles;
    private int maxSize;
    private boolean loaded;

    QIndexFile(File dir) {
        this.file = new File(dir, "index");
    }

    /**
     * QIndexFile {
     *  SIGN(3byte) TYPE(1byte) VERSION(1byte) id{len(1byte) payload(${len}byte)}
     *  maxFilesSize(1long) maxFiles(1int) maxSize(1int)
     *  listSize(1short)
     *  [
     *   fileName{len(1byte) payload(${len}byte)}
     *   ...
     *   ...${listSize}
     *  ]
     * }
     * @throws IOException
     */
    synchronized void load() throws IOException {
        if (loaded) {
            return;
        }

        Path path = file.toPath();
        ByteBuffer buff;
        try(FileChannel fc = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.READ)) {
            long size = fc.size();
            if (size > MAX_SIZE) {
                throw new FbException("Index file: " + path + " has too larger size: " + size);
            }
            buff = ByteBuffer.allocate((int) size).order(ByteOrder.BIG_ENDIAN);
            fc.read(buff);
        }

        buff.flip();

        byte[] tmp = new byte[FbUtils.MAX_STR_LEN];
        //==================== File header ============================
        FbUtils.readSign(buff);
        FbUtils.readAndValidate(buff, TYPE);
        FbUtils.readAndValidate(buff, VERSION);

        //==================== id ====================================
        FbUtils.validate(id, FbUtils.readString(tmp, buff), "id") ;

        //=================== storage config =========================
        FbUtils.validate(this.maxFilesSize, buff.getLong(), "maxFilesSize");
        FbUtils.validate(this.maxFiles, buff.getInt(), "maxFiles");
        FbUtils.validate(this.maxSize, buff.getInt(), "maxSize");
        int listSize = buff.getShort();

        //=================== file list ==============================
        List<String> list = new ArrayList<>(listSize);
        //our files can not have name larger that 256 bytes
        for(int i = 0; i < listSize; i++) {
            String str = FbUtils.readString(tmp, buff);
            list.add(str);
        }
        this.list.addAll(list); // file name list
        this.loaded = true;
    }

    synchronized void save() throws IOException {
        int listSize = list.size();
        Assert.isTrue(listSize < Short.MAX_VALUE, "Too large list");
        byte[] idBs = FbUtils.toBytes(id);

        int fileSize = HEADER_SIZE      // header sign type version
                + idBs.length + 1       // id str
                + 8                     // maxFilesSize
                + 4                     // maxFiles
                + 4                     // maxSize
                + 2                     // listSize
                ;
        List<byte[]> fileNamesBs = new ArrayList<>(listSize);
        for (String fileName : list) {
            byte[] fileNameBs = FbUtils.toBytes(fileName);
            fileNamesBs.add(fileNameBs);
            int length = fileNameBs.length;
            fileSize += length + 1;
        }

        ByteBuffer buff = ByteBuffer.allocate(fileSize).order(ByteOrder.BIG_ENDIAN);
        FbUtils.writeSign(buff);
        buff.put(TYPE);
        buff.put(VERSION);
        FbUtils.writeString(idBs, buff);
        buff.putLong(this.maxFilesSize);
        buff.putInt(this.maxFiles);
        buff.putInt(this.maxSize);
        buff.putShort((short) listSize);
        for (byte[] fileNameBs: fileNamesBs) {
            FbUtils.writeString(fileNameBs, buff);
        }

        buff.flip();

        Path path = file.toPath();
        try (FileChannel fc = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            fc.write(buff);
        }
    }

    synchronized void delete() {
        this.file.delete();
    }

    @Override
    public synchronized void close() throws Exception {
        save();
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list)  {
        //below not thread safe
        this.list.clear();
        this.list.addAll(list);
    }

    public boolean isExists() {
        return this.file.exists();
    }
}
