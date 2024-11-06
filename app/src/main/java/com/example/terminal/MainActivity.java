package com.example.terminal;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    Terminal terminal = null;
    private static final String LOG_TAG = "DebugTag";

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

        TextView terminalTextView = findViewById(R.id.terminal_out);
        EditText terminalEditText = findViewById(R.id.terminal_in);
        terminal = new Terminal(terminalTextView);

        terminalTextView.setMovementMethod(new ScrollingMovementMethod()); // set scroll

        terminalEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_GO) {
                    byte[] inputBytes = (terminalEditText.getText().toString() + '\n').getBytes(StandardCharsets.UTF_8);
                    terminal.writeBytes(inputBytes);
                    terminalEditText.setText("");
                    handled = true;
                }
                return handled;
            }
        });

        // handle buttons
        ToggleButton btnCtrl = findViewById(R.id.btnCtrl);
        btnCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnCtrl.isChecked()) { // ON
                    terminal.isCtrlChecked = true;
                } else { // OFF
                    terminal.isCtrlChecked = false;
                }
            }
        });
    }
}