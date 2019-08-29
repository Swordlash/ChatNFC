package pl.edu.mimuw.chatnfc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;

import pl.edu.mimuw.chatnfc.R;
import pl.edu.mimuw.chatnfc.tools.FirebaseTools;
import pl.edu.mimuw.chatnfc.tools.ObjectAcquireListener;

import org.xdty.preference.colorpicker.ColorPickerDialog;
import org.xdty.preference.colorpicker.ColorPickerSwatch;

public class SettingsActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar mToolbar;
    private int mSelectedColor;
    private TextView textPrimary;
//    private TextView textLight;
//    private TextView textAccent;
    private Button changePrimary;
//    private Button changeLight;
//    private Button changeAccent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = findViewById(R.id.settings_toolbar);
        String UID = FirebaseTools.getInstance().getCurrentUser().getUid();
        FirebaseTools.getInstance().acquireDataFromDB("Users/" + UID + "/color_primary", new ObjectAcquireListener() {
            @Override
            public void onObjectAcquired(Object obj) {
                if (obj != null) {
                    mToolbar.setBackgroundColor(Integer.parseInt(obj.toString()));
                }
            }

            @Override
            public void onError(DatabaseError err) {

            }
        });
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mSelectedColor = ContextCompat.getColor(this, R.color.flamingo);

        textPrimary = findViewById(R.id.text_primary);
//        textLight = findViewById(R.id.text_light);
//        textAccent = findViewById(R.id.text_accent);
        changePrimary = findViewById(R.id.change_primary);
//        changeLight = findViewById(R.id.change_light);
//        changeAccent = findViewById(R.id.change_accent);

        textPrimary.setTextColor(getResources().getColor(R.color.colorPrimary));
//        textLight.setTextColor(getResources().getColor(R.color.colorPrimaryLight));
//        textAccent.setTextColor(getResources().getColor(R.color.colorAccent));

        FirebaseTools.getInstance().acquireDataFromDB("Users/" + UID + "/color_primary", new ObjectAcquireListener() {
            @Override
            public void onObjectAcquired(Object obj) {
                if(obj != null) {
                    textPrimary.setTextColor(Integer.parseInt(obj.toString()));
                }
            }

            @Override
            public void onError(DatabaseError err) {

            }
        });

        int[] mColors = getResources().getIntArray(R.array.default_rainbow);

        ColorPickerDialog dialog1 = ColorPickerDialog.newInstance(R.string.color_picker_default_title,
                mColors,
                mSelectedColor,
                5, // Number of columns
                ColorPickerDialog.SIZE_SMALL,
                true // True or False to enable or disable the serpentine effect
                //0, // stroke width
                //Color.BLACK // stroke color
        );

        dialog1.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                mSelectedColor = color;
                textPrimary.setTextColor(mSelectedColor);
                FirebaseTools.getInstance().setValueInDB("Users/" + UID + "/color_primary", mSelectedColor);
                Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        changePrimary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog1.show(getFragmentManager(), "color_primary");
            }
        });
    }
}
