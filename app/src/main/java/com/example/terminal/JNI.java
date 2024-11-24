package com.example.terminal;

final class JNI {

    static {
        System.loadLibrary("terminal");
    }

    public static native int createPty(String shell, String homePath);
    public static native void close(int fd);
}