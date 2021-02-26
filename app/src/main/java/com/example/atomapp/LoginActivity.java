package com.example.atom_task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity{

    private String TAG = "Login Activity";

    // Variable Declarations
    private LoginActivity binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;

    private ProgressBar progressBar;
    private MaterialButton guestLoginButton;
    private MaterialButton googleLoginButton;
    private TextView privacy;

    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        guestLoginButton = findViewById(R.id.guest_login_btn);
        googleLoginButton = findViewById(R.id.google_login_btn);
        progressBar = findViewById(R.id.progress_circular);
        privacy = findViewById(R.id.privacy_txt);
        String termService = "<a href=\"https://sachinduhan.me\" target=\"_blank\">Term of Service</a>";
        String privacyPolicy = "<a href=\"https://sachinduhan.me\" target=\"_blank\">Privacy Policy</a>";
        String tandp = "By creating an account you agree to our "+termService+" & "+privacyPolicy;
        
        Spanned policy = Html.fromHtml(tandp);
        privacy.setText(policy);
        binding = this;
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // listening to clicks 
        clickListeners();
    }

    private void AnonymousSignIn() {
        start_loader();
        mAuth.AnonymousSignIn()
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        updateActivity(true);
                        hide_loader();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "Anonymous Sign In : FAILURE", exception);
                        updateActivity(false);
                        hide_loader();
                    }
                });
    }

    private void updateActivity(Boolean status) {
        if (status){
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
            finish();
        }else{
            Toast.makeText(getApplicationContext(),"Login Failed: OOPS! Something is not right!",Toast.LENGTH_SHORT).show();
        }
    }

    private void signInGoogle(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                _google_sign_in(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void _google_sign_in(String token) {
        start_loader();
        AuthCredential creds = GoogleAuthProvider.getCredential(token, null);
        mAuth.signInWithCredential(creds)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(),"Authentication Failed",Toast.LENGTH_SHORT).show();
                            updateActivity(false);
                        }
                        hide_loader();
                        updateActivity(true);
                    }
                });
    }

    private void clickListeners(){
        guestLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnonymousSignIn();
            }
        });

        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInGoogle();
            }
        });
    }

    private void start_loader() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    private void hide_loader() {
        binding.progressBar.setVisibility(View.INVISIBLE);
    }
}