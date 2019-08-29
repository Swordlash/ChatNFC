package pl.edu.mimuw.chatnfc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;

import pl.edu.mimuw.chatnfc.R;
import pl.edu.mimuw.chatnfc.config.UserProfile;
import pl.edu.mimuw.chatnfc.security.KeyStoreProvider;
import pl.edu.mimuw.chatnfc.tools.FirebaseTools;
import pl.edu.mimuw.chatnfc.tools.ObjectAcquireListener;
import pl.edu.mimuw.chatnfc.tools.OnlineProvider;

public class MenuActivity extends AppCompatActivity
{

    private android.support.v7.widget.Toolbar toolbar;
    private ViewPager viewPager;
    private SectionPagerAdapter sectionPageAdapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Firebase.setAndroidContext(this);
        KeyStoreProvider.initializeKeyStore();

        toolbar = findViewById(R.id.menu_toolbar);
        if (FirebaseTools.getInstance().getCurrentUser() != null) {
            String UID = FirebaseTools.getInstance().getCurrentUser().getUid();
            FirebaseTools.getInstance().acquireDataFromDB("Users/" + UID + "/color_primary", new ObjectAcquireListener() {
                @Override
                public void onObjectAcquired(Object obj) {
                    if (obj != null) {
                        toolbar.setBackgroundColor(Integer.parseInt(obj.toString()));
                    }
                }

                @Override
                public void onError(DatabaseError err) {

                }
            });
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Menu");
        viewPager = findViewById(R.id.menu_view_pager);
        sectionPageAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionPageAdapter);
        tabLayout = findViewById(R.id.menu_tabs);
        if (FirebaseTools.getInstance().getCurrentUser() != null) {

            FirebaseTools.getInstance().acquireDataFromDB("Users/" + FirebaseTools.getInstance().getCurrentUser().getUid() + "/color_primary", new ObjectAcquireListener() {
                @Override
                public void onObjectAcquired(Object obj) {
                    if (obj != null) {
                        tabLayout.setBackgroundColor(Integer.parseInt(obj.toString()));
                    }
                }

                @Override
                public void onError(DatabaseError err) {

                }
            });
        }
        tabLayout.setupWithViewPager(viewPager);

        OnlineProvider.setOnline();
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser usr = FirebaseTools.getInstance().getCurrentUser();

        if (usr == null ||
                UserProfile.getProfile(usr.getUid()) == null) {
            sendToWelcomeScreen();
        } else {
            OnlineProvider.setOnline();
        }

        viewPager.setCurrentItem(getIntent().getIntExtra("menu_fragment", 0));
    }

    @Override
    public void onRestart() {
        super.onRestart();
        FirebaseUser usr = FirebaseTools.getInstance().getCurrentUser();

        if (usr == null ||
                UserProfile.getProfile(usr.getUid()) == null) {
            sendToWelcomeScreen();
        } else {
            OnlineProvider.setOnline();
        }

        viewPager.setCurrentItem(getIntent().getIntExtra("menu_fragment", 0));
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser usr = FirebaseTools.getInstance().getCurrentUser();

        if (usr == null ||
                UserProfile.getProfile(usr.getUid()) == null) {
            sendToWelcomeScreen();
        } else {
            OnlineProvider.setOnline();
        }

        viewPager.setCurrentItem(getIntent().getIntExtra("menu_fragment", 0));
    }

    @Override
    public void onLocalVoiceInteractionStopped() {
        super.onLocalVoiceInteractionStopped();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (FirebaseTools.getInstance().getCurrentUser() == null)
            sendToWelcomeScreen();
        else {
//            OnlineProvider.setOffline();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OnlineProvider.setOffline();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean res = super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_menu_log_out) {
            FirebaseAuth.getInstance().signOut();
            sendToWelcomeScreen();
        }
        if (item.getItemId() == R.id.main_menu_settings) {
            sendToSettings();
        }
        if (item.getItemId() == R.id.main_menu_nfc) {
            sendToNFC();
        }

        return res;
    }

    private void sendToSettings() {
        Intent settingsIntent = new Intent(MenuActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void sendToWelcomeScreen() {
        Intent welcomeIntent = new Intent(MenuActivity.this, WelcomeScreen.class);
        startActivity(welcomeIntent);
        finish();
    }

    private void sendToNFC() {
        Intent nfcIntent = new Intent(MenuActivity.this, ActivityAddContactNFC.class);
        startActivity(nfcIntent);
    }

    public int getFrontFragment() {
        return viewPager.getCurrentItem();
    }
}
