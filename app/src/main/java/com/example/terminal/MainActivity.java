package com.example.terminal;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.terminal.databinding.ActivityMainBinding;

import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    TerminalEmulator terminal = null;
    //private static final String LOG_TAG = "DebugTag";

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        this.getWindow().setStatusBarColor(getApplicationContext().getColor(R.color.black));
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TerminalView terminalView = binding.terminalOutView;
        EditText terminalEditText = binding.terminalIn;
        terminal = new TerminalEmulator(terminalView);

        terminalEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            boolean handled = false;
            if (i == EditorInfo.IME_ACTION_GO) {
                byte[] inputBytes;
                if (terminal.isCtrlChecked)
                    if (terminalEditText.getText().toString().equalsIgnoreCase("c"))
                        inputBytes = new byte[]{0x03};
                    else
                        inputBytes = (terminalEditText.getText().toString() + '\n').getBytes(StandardCharsets.UTF_8);
                else
                    inputBytes = (terminalEditText.getText().toString() + '\n').getBytes(StandardCharsets.UTF_8);

                terminal.writeBytes(inputBytes);
                terminalEditText.setText("");
                handled = true;
            }
            return handled;
        });

        // handle buttons
        binding.btnCtrl.setOnClickListener(view -> {
            if (binding.btnCtrl.isChecked()) { // ON
                terminal.isCtrlChecked = true;
            } else { // OFF
                terminal.isCtrlChecked = false;
            }
        });

        binding.btnLeft.setOnClickListener(v -> {
            terminalView.moveCursorLeft();
        });

        binding.btnRight.setOnClickListener(v -> {
            terminalView.moveCursorRight();
        });

        binding.btnDown.setOnClickListener(v -> {
            terminalView.moveCursorDown();
        });

        binding.btnUp.setOnClickListener(v -> {
            terminalView.moveCursorUp();
        });
    }
}