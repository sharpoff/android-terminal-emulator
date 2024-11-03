package com.example.terminal;

import static com.example.terminal.JNI.close;
import static com.example.terminal.JNI.createPty;

import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

public class Terminal {

    private FileDescriptor terminalFd = null;
    private TextView terminalOut = null;

    Terminal(TextView termOut) {
        // creating pty
        int pid = createPty();
        Log.d("DebugTag", "pid: " + pid);
        terminalFd = createTerminalFd(pid);
        terminalOut = termOut;

        // thread that is reading from terminal file descriptor and adding it to terminalOut TextView
        new Thread() {
            @Override
            public void run() {
                byte[] bytes = new byte[4096];
                try (InputStream inputStream = new FileInputStream(terminalFd)) {
                    while(true) {
                        int read = inputStream.read(bytes);
                        //Log.d("DebugTag", "Read bytes " + read);
                        if (read <= 0) {
                            close(pid);
                            break;
                        } else {
                            terminalOut.append(new String(bytes, StandardCharsets.UTF_8).replaceAll("\0", ""));
                            Log.d("DebugTag", "message:\n" + new String(bytes, StandardCharsets.UTF_8).replaceAll("\0", ""));
                        }
                    }
                } catch (IOException e) {
                    Log.d("DebugTag", "IOException");
                }
            }
        }.start();
    }

    @NonNull
    private FileDescriptor createTerminalFd(int pid) {
        FileDescriptor fd = new FileDescriptor();
        try {
            Field fdField;
            fdField = FileDescriptor.class.getDeclaredField("descriptor");
            fdField.setAccessible(true);
            fdField.set(fd, pid);
        } catch (Exception e) {
            Log.e("DebugTag", e.toString());
        }

        return fd;
    }

    void writeBytes(byte[] bytes) {
        // writing command to pty
        try (OutputStream outputStream = new FileOutputStream(terminalFd)) {
            outputStream.write(bytes);
        } catch (Exception e) {}
    }


}
