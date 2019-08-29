package pl.edu.mimuw.chatnfc.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import pl.edu.mimuw.chatnfc.R;


public class ImageActivity extends AppCompatActivity {

    private TextView author;
    private TextView time;

    private void loadImageFromStorage(String path) {

        try {
            File f = new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            ImageView image = findViewById(R.id.activity_image_image);
            image.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        author = findViewById(R.id.activity_image_author);
        time = findViewById(R.id.activity_image_time);
	
	    author.setText(getIntent().getStringExtra("author"));

        loadImageFromStorage(getIntent().getStringExtra("image"));

        time.setText(getIntent().getStringExtra("time"));
    }
}
