package com.nubits.nubot.db;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A low level DB
 */
public class NuDB {

    private static RandomAccessFile dbfile;
    private static long pointer;

    /**
     * create a DB file
     * @param dbPath
     * @throws IOException
     */
    public static void createDB(String dbPath) throws IOException {
        File f = new File(dbPath);

        if (f.exists())
            throw new IOException("DB already exists");

        dbfile = new RandomAccessFile(dbPath, "rw");
        pointer = dbfile.getFilePointer();
    }

    /**
     * write an array to the DB
     * @param byteArray
     * @throws IOException
     */
    public static void writeTo(byte[] byteArray) throws IOException {
        dbfile.write(byteArray);
    }

    /**
     * close the database
     * @throws IOException
     */
    public static void closeDB() throws IOException {
        dbfile.close();
    }
}
