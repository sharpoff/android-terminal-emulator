package com.example.terminal;

import static com.example.terminal.JNI.close;
import static com.example.terminal.JNI.createPty;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // creating pty
        int pid = createPty();
        Log.d("DebugTag", "pid: " + pid);
        FileDescriptor terminalFd = createTerminalFd(pid);

        // writing command to pty
        try (OutputStream outputStream = new FileOutputStream(terminalFd)) {
            outputStream.write("pwd\n".getBytes());
        } catch (Exception e) {}

        byte[] bytes = new byte[4096];
        String msgStr = "";

        // reading pty output
        try (InputStream inputStream = new FileInputStream(terminalFd)) {
            while(true) {
                int read = inputStream.read(bytes);
                //Log.d("DebugTag", "Read bytes " + read);
                msgStr += new String(bytes, StandardCharsets.UTF_8).replaceAll("\0", "");
                if (read <= 0) {
                    close(pid);
                    break;
                };
            }
        } catch (IOException e) {}

        Log.d("DebugTag", "message: " + msgStr);
        TextView textView = findViewById(R.id.tvMain);
        textView.setText(msgStr);
    }

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
}