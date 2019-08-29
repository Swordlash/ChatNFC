package pl.edu.mimuw.chatnfc.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

import pl.edu.mimuw.chatnfc.R;
import pl.edu.mimuw.chatnfc.config.UserProfile;
import pl.edu.mimuw.chatnfc.config.UserProfileProvider;
import pl.edu.mimuw.chatnfc.tools.FirebaseTools;

public class ActivityRegister extends AppCompatActivity {
	
	private TextInputLayout emailTextInput;
	private TextInputLayout passwordTextInput;
	private TextInputLayout repeatPasswordTextInput;
	private TextInputLayout nameTextInput;
	private TextInputLayout surnameTextInput;
    private Button registerButton;
	
	private android.support.v7.widget.Toolbar toolbar;
	
	private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
	
	    emailTextInput = findViewById(R.id.login_password);
	    passwordTextInput = findViewById(R.id.register_password);
	    repeatPasswordTextInput = findViewById(R.id.register_password2);
	    nameTextInput = findViewById(R.id.register_name);
	    surnameTextInput = findViewById(R.id.register_surname);
        registerButton = findViewById(R.id.register_button);
	
	    toolbar = findViewById(R.id.settings_toolbar);
	    setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.register_action));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	
	    progressDialog = new ProgressDialog(this);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
	            emailTextInput.setError(null);
	            passwordTextInput.setError(null);
	            repeatPasswordTextInput.setError(null);

                boolean error = false;
	
	            String email = ActivityRegister.this.emailTextInput.getEditText().getText()
			            .toString();
	            String password = passwordTextInput.getEditText().getText().toString();
	            String password2 = repeatPasswordTextInput.getEditText().getText().toString();
	            String name = nameTextInput.getEditText().getText().toString();
	            String surname = surnameTextInput.getEditText().getText().toString();
	
	            if (!email.matches("(^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$)")) {
		            ActivityRegister.this.emailTextInput
				            .setError(getString(R.string.error_invalid_email));
                    error = true;
                }
	            if (password.length() < 6)
	            {
		            passwordTextInput.setError(getString(R.string.error_short_password));
                    error = true;
                }
                if (!password.equals(password2)) {
	                repeatPasswordTextInput.setError(getString(R.string.error_diff_passwords));
                    error = true;
                }
	            if (name.isEmpty())
	            {
		            nameTextInput.setError("Name cannot be empty!");
		            error = true;
	            }
	            if (surname.isEmpty())
	            {
		            surnameTextInput.setError("Surname cannot be empty!");
		            error = true;
	            }
                

                if (error) {
                    return;
                }
	
	            progressDialog.setTitle(getString(R.string.register_loading));
	            progressDialog.setMessage(getString(R.string.register_wait));
	            progressDialog.show();
	            progressDialog.setCanceledOnTouchOutside(false);
	
	            registerUser(email, password, name, surname);
            }
	
	        private void registerUser(String email, String password, final String name, final String surname)
	        {
		        FirebaseTools.getInstance().createUserWithEmailAndPassword(email, password,
				        new OnCompleteListener<AuthResult>()
				        {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful()) {
	                        FirebaseUser user = FirebaseTools.getInstance().getCurrentUser();
	
	                        UserProfile local = UserProfile
			                        .createNewProfile(user.getUid(), password, name, surname);
	                        UserProfileProvider.saveLocalUserProfile(local);
	                        UserProfileProvider.saveRemoteUserProfile(local);
	
	                        Intent menu = new Intent(ActivityRegister.this, MenuActivity.class);
	                        menu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	                        startActivity(menu);
	                        finish();
                        }
                        else
                        {
	                        progressDialog.hide();
	                        Log.e("Error: ", task.getException().getMessage());
                            Toast.makeText(ActivityRegister.this, getString(R.string.register_failure), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}