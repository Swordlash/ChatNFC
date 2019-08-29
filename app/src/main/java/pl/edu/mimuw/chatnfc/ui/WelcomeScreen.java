package pl.edu.mimuw.chatnfc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import pl.edu.mimuw.chatnfc.R;

/**
 * Ekran powitalny dla użytkownika
 */
public class WelcomeScreen extends AppCompatActivity {

    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
	
	    loginButton = findViewById(R.id.welcome_login);
	    registerButton = findViewById(R.id.welcome_register);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login = new Intent(WelcomeScreen.this, ActivityLogin.class);
                startActivity(login);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register = new Intent(WelcomeScreen.this, ActivityRegister.class);
                startActivity(register);
            }
        });
    }
}
