package pl.edu.mimuw.chatnfc.ui;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.edu.mimuw.chatnfc.R;
import pl.edu.mimuw.chatnfc.config.UserProfile;
import pl.edu.mimuw.chatnfc.config.UserProfileProvider;
import pl.edu.mimuw.chatnfc.messanging.Messanging;
import pl.edu.mimuw.chatnfc.tools.FirebaseTools;
import pl.edu.mimuw.chatnfc.tools.ObjectAcquireListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private final int RESULT_LOAD_IMAGE = 1;
	
	@Override
	public void setUserVisibleHint(boolean visible)
	{
		super.setUserVisibleHint(visible);
		if (visible && getView() != null)
		{
			refresh();
		}
	}
	
	public void refresh()
	{
		final TextView statusData = getView().findViewById(R.id.settings_hello_view);
		final TextView personData = getView().findViewById(R.id.settings_name_view);
		final CircleImageView imageData = getView().findViewById(R.id.settings_profile_image);
		
		String userUID = FirebaseTools.getInstance().getCurrentUser().getUid();
		UserProfile profile = UserProfile.getProfile(userUID);
		
		statusData.setText(profile.getStatus());
		personData.setText(String.format("%s %s", profile.getName(), profile.getSurname()));
		
		if (profile.getAvatar() != null)
			imageData.setImageBitmap(profile.getAvatar());
		
		else imageData.setImageResource(R.drawable.avatar);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
	{
		refresh();

        Button editStatusButton = getView().findViewById(R.id.settings_change_status);

        editStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                startActivity(intent);
            }
        });

        Button editPictureButton = getView().findViewById(R.id.settings_change_image);
        String UID = FirebaseTools.getInstance().getCurrentUser().getUid();
        FirebaseTools.getInstance().acquireDataFromDB("Users/" + UID + "/color_primary", new ObjectAcquireListener() {
            @Override
            public void onObjectAcquired(Object obj) {
                if (obj != null) {
                    editPictureButton.setBackgroundColor(Integer.parseInt(obj.toString()));
                }
            }

            @Override
            public void onError(DatabaseError err) {

            }
        });
        editPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "SELECT IMAGE"), RESULT_LOAD_IMAGE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK) {
	
	        Uri resultUri = data.getData();
	        ProgressDialog dialog = new ProgressDialog(UnificApp.getCurrentActivity());
	        dialog.setTitle("Please wait...");
	        dialog.setMessage("Loading image and saving to realtime database");
	        dialog.setCanceledOnTouchOutside(false);
	        dialog.show();
	        
	        try
	        {
		        UserProfile local = UserProfile.getLocalProfile();
		        local.setAvatar(MediaStore.Images.Media
				        .getBitmap(getActivity().getContentResolver(), resultUri));
		
		        Log.e("Profile: ", local.getAvatar().toString());
		
		        UserProfileProvider.saveLocalUserProfile(local);
		        UserProfileProvider.saveRemoteUserProfileAsync(local);
		        Messanging.notifyChangeImage();
		        
		        CircleImageView imageData = getView().findViewById(R.id.settings_profile_image);
		        imageData.setImageBitmap(local.getAvatar());
		
		        dialog.dismiss();
		        Toast.makeText(getActivity(), "Successful image change!", Toast.LENGTH_SHORT)
				        .show();
	        }
	        catch (IOException e)
	        {
		        dialog.dismiss();
		        Log.e("Set avatar", e.getMessage());
		        Toast.makeText(getActivity(), "I/O Error occured!", Toast.LENGTH_SHORT).show();
	        }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
}
