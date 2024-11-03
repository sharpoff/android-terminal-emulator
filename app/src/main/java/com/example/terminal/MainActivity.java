package com.example.terminal;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    Terminal terminal = null;

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

        TextView terminalOut = findViewById(R.id.terminal_out);
        terminalOut.setMovementMethod(new ScrollingMovementMethod()); // set scroll
        EditText terminalIn = findViewById(R.id.terminal_in);

        terminal = new Terminal(terminalOut);

        // on enter write what you typed to the terminal and add '\n', so it can be interpreted
        terminalIn.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_GO) {
                    //Perform your Actions here.
                    //Log.d("DebugTag", "command: " + terminalIn.getText().toString() + '\n');
                    terminal.writeBytes((terminalIn.getText().toString() + '\n').getBytes());
                }
                return handled;
            }
        });
    }
}