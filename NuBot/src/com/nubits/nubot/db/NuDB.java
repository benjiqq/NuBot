package com.nubits.nubot.db;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A very simple low level DB.
 * Used for storing prices or other time-series data. Instead of using a flat file or SQL
 * with random access all basic operations can be implemented
 * storing a record and getting the latest
 * there is no update method, since time-series are just kept and never deleted
 */
public class NuDB {

    private static RandomAccessFile dbfile;
    private static long pointer;

    /**
     * create a DB file
     *
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
     *
     * @param byteArray
     * @throws IOException
     */
    public static void writeTo(byte[] byteArray) throws IOException {
        dbfile.write(byteArray);
    }

    /**
     * read the whole DB
     *
     * @return
     * @throws IOException
     */
    public static byte[] readAll() throws IOException {
        long beforepointer = dbfile.getFilePointer();
        dbfile.seek(0);
        long n = dbfile.length();
        byte[] allbytes = readNbytes((int) n);

        //reseth pointer
        pointer = beforepointer;
        dbfile.seek(beforepointer);
        return allbytes;
    }

    /**
     * read n number of bytes
     *
     * @param n
     * @return
     * @throws IOException
     */
    public static byte[] readNbytes(int n) throws IOException {
        //set pointer not needed
        //dbfile.seek(pointer);

        byte[] b = new byte[n];
        for (int i = 0; i < n; i++) {
            byte aByte = dbfile.readByte();
            b[i] = aByte;
        }

        //pointer has moved
        return b;

    }

    /**
     * close the database
     *
     * @throws IOException
     */
    public static void closeDB() throws IOException {
        dbfile.close();
    }

}
