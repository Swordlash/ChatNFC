package pl.edu.mimuw.chatnfc.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import pl.edu.mimuw.chatnfc.R;
import pl.edu.mimuw.chatnfc.config.UserProfileProvider;
import pl.edu.mimuw.chatnfc.security.KeyStoreProvider;
import pl.edu.mimuw.chatnfc.tools.FirebaseTools;

public class ActivityLogin extends AppCompatActivity {
    
    private TextInputLayout loginTextInput;
    private TextInputLayout passwordTextInput;
    private Button loginButton;
    
    private android.support.v7.widget.Toolbar toolbar;
    
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    
        loginTextInput = findViewById(R.id.login_email);
        passwordTextInput = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
    
        toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.login_action));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    
        progressDialog = new ProgressDialog(this);

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                loginTextInput.setError(null);
                passwordTextInput.setError(null);

                boolean error = false;
    
                String login = loginTextInput.getEditText().getText().toString();
                String password = passwordTextInput.getEditText().getText().toString();

                if (login.equals("")) {
                    loginTextInput.setError(getString(R.string.error_blank_login));
                    error = true;
                }
                if (password.equals("")) {
                    passwordTextInput.setError(getString(R.string.error_blank_password));
                    error = true;
                }
                if (error) {
                    return;
                }
                progressDialog.setTitle(getString(R.string.login_progress));
                progressDialog.setMessage(getString(R.string.login_wait));
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(false);

                loginUser(login, password);
            }
        });
    }

    private void loginUser(String login, String password) {
	    FirebaseTools.getInstance().loginUser(login, password, new OnCompleteListener<AuthResult>()
	    {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
    
                    KeyStoreProvider.encryptAndSaveUserPassword(
			                FirebaseTools.getInstance().getCurrentUser().getUid(),
			                password);
    
                    Intent menuIntent = new Intent(ActivityLogin.this, MenuActivity.class);
                    menuIntent
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    
                    UserProfileProvider.getRemoteUserProfileAndStartActivity(
                            FirebaseTools.getInstance().getCurrentUser().getUid(),
                            menuIntent);
	
	                progressDialog.dismiss();
                    finish();

                } else {
                    progressDialog.hide();
                    Toast.makeText(ActivityLogin.this, getString(R.string.login_error), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
