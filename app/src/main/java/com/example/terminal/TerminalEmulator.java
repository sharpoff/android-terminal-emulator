package com.example.terminal;

import static com.example.terminal.JNI.close;
import static com.example.terminal.JNI.createPty;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;


public class TerminalEmulator {
    private static final int TERMINAL_BUFFER_SIZE = 4096;
    // Sequences codes
    private static final int NONE = 0;
    private static final int ESC = 1;
    private static final int ESC_CSI = 2;
    private static final int CSI_ARGUMENT = 3;
    private static final int ESC_CSI_QUESTION = 4;

    private int currentSequence = NONE;
    private boolean continueSequence = false;
    private int[] sequencesArgs = new int[10];
    private int sequencesArgsIndex = 0;
    private int parseIndex = 0;

    public boolean isCtrlChecked = false;
    public boolean isAltChecked = false;

    private ByteArrayOutputStream terminalBufferStream = new ByteArrayOutputStream();

    private static final String LOG_TAG = "TerminalEmulator";
    private FileDescriptor terminalFd = null;
    private TerminalView terminalView = null;

    TerminalEmulator(TerminalView textView, String homePath) {
        // creating pty
        int pid = createPty("sh", homePath);
        Log.d(LOG_TAG, "PID: " + pid);
        terminalFd = createTerminalFd(pid);
        terminalView = textView;

        // thread that is reading from terminal file descriptor and adding it to terminalOut TextView
        new Thread() {
            @Override
            public void run() {
                try (InputStream inputStream = new FileInputStream(terminalFd)) {
                    while(true) {
                        byte[] bytes = new byte[4096];
                        int read = inputStream.read(bytes);
                        if (read <= 0) {
                            Log.d(LOG_TAG, "Closing pid");
                            close(pid);
                            break;
                        } else {
                            // parse bytes and add to output
                            terminalBufferStream = new ByteArrayOutputStream();
                            for (byte b : bytes) {
                                parseByte(b);
                            }
                            // TODO: after successful parse it can be removed, but for now that's fine
                            String text = new String(terminalBufferStream.toByteArray(), StandardCharsets.UTF_8).replaceAll("\0", "");

                            terminalView.appendText(text);
                            Log.d(LOG_TAG, "Message:\n" + new String(bytes, StandardCharsets.UTF_8).replaceAll("\0", ""));
                        }
                    }
                } catch (IOException e) {}
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
            Log.e(LOG_TAG, e.toString());
        }
        return fd;
    }

    void writeBytes(byte[] bytes) {
        // writing command to pty
        try (OutputStream outputStream = new FileOutputStream(terminalFd)) {
            outputStream.write(bytes);
        } catch (Exception e) {}
    }

    void parseByte(byte b) {
        switch (b) {
            case 0: // null byte
                break;
            case 7: // BEL
                break;
            case 8: // BS
                break;
            case 9: // HT
                break;
            case 11: // VT
                break;
            case 10: // LF
            case 12: // FF
                terminalBufferStream.write(b);
            case 13: // CR
                break;
            case 27: // ESC
                continueSequence = true;
                currentSequence = ESC;
            case 127: // DEL
                break;
            default:
                continueSequence = false;
                switch (currentSequence) {
                    case NONE: // regular character after escape sequence
                        sequencesArgsIndex = 0; // TODO: should it be here?
                        sequencesArgs = new int[10];
                        terminalBufferStream.write(b);
                    case ESC:
                        switch (b) {
                            case '[':
                                currentSequence = ESC_CSI;
                        }
                        continueSequence = true;
                        break;
                    case ESC_CSI: // CSI
                        Log.d("ParseTag", "ESC_CSI encountered");
                        switch (b) {
                            case 'A': // cursor up
                                break;
                            case 'B': // cursor down
                                break;
                            case 'C': // cursor forward
                                break;
                            case 'D': // cursor back
                                break;
                            case 'E': // cursor next line
                                break;
                            case 'F': // cursor prev line
                                break;
                            case 'G': // cursor horizontal absolute
                                break;
                            case 'H': // cursor position
                                break;
                            case 'J': // erase in display
                                Log.d("ParseTag", "J encountered in ESC_CSI");
                                clearDisplay(sequencesArgs[0]);
                                Log.d("ParseTag", "clearing screen, arg: " + sequencesArgs[0]);
                                continueSequence = true;
                                break;
                            case 'K': // erase in line
                                break;
                            case 'S': // scroll up
                                break;
                            case 'T': // scroll down
                                break;
                            case 'f': // horizontal vertical position
                                break;
                            case 'm':
                                break;
                            case 's': // save current cursor position
                                break;
                            case 'u': // restore saved cursor position
                                break;
                            // case '<':
                            //     break;
                            // case '=':
                            //     break;
                            // case '>':
                            //     break;
                            case '?':
                                currentSequence = ESC_CSI_QUESTION;
                                continueSequence = true;
                                break;
                            default:
                                parseArgument(b);
                                break;
                        }
                        break;
                    case ESC_CSI_QUESTION:
                        switch (b) {
                            case 'l':
                                break;
                            case 'h':
                                break;
                            default:
                                parseArgument(b);
                                Log.i("ParseTag", "Parsed CSI_QUESTION arg: " + sequencesArgsIndex);
                                continueSequence = true;
                        }
                        break;
                }
                if (!continueSequence) currentSequence = NONE;
                break;
        }
    }

    private void clearDisplay(int arg0) {
        switch (arg0) {
            case 0: // from cursor to end
                break;
            case 1: // from cursor to beginning
                break;
            case 2: // clear entire screen (and moves cursor to upper left)
                terminalView.clearDisplay();
                break;
            case 3: // clear entire screen and delete all lines saved in the scrollback buffer
                break;
        }
    }

    private void parseArgument(byte b) {
        if (sequencesArgsIndex >= sequencesArgs.length)
            return;

        if (b >= '0' && b <= '9') {
            Log.d("ParseTag", "byte: " + (b - '0'));
            int current = b - '0';
            int previous = sequencesArgs[sequencesArgsIndex];
            int value = previous >= 0 ? previous * 10 + current : current;

            if (value > 9999) value = 9999;
            sequencesArgs[sequencesArgsIndex] = value;

            continueSequence = true;
        } else if (b == ';' || b == ':') {
            sequencesArgsIndex++;
            continueSequence = true;
        } else {
            Log.e("ParseTag", "Unknown sequence: " + b);
        }
    }

    public void moveCursorLeft() {
        terminalView.moveCursorLeft();
    }

    public void moveCursorRight() {
        terminalView.moveCursorRight();
    }

    public void moveCursorUp() {
        terminalView.moveCursorUp();
    }

    public void moveCursorDown() {
        terminalView.moveCursorDown();
    }
}
