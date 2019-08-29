package pl.edu.mimuw.chatnfc.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.edu.mimuw.chatnfc.R;
import pl.edu.mimuw.chatnfc.config.Contact;
import pl.edu.mimuw.chatnfc.config.UserProfile;
import pl.edu.mimuw.chatnfc.messanging.ImageMessage;
import pl.edu.mimuw.chatnfc.messanging.Message;
import pl.edu.mimuw.chatnfc.tools.FirebaseTools;
import pl.edu.mimuw.chatnfc.tools.ObjectAcquireListener;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private String saveToInternalStorage(Bitmap bitmapImage) {
        // path to /data/data/yourapp/app_data/imageDir
	    File directory = UnificApp.getUnificApp().getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private List<Message<?>> messageList;

    public MessageAdapter(List<Message<?>> messageList) {
        this.messageList = new ArrayList<>();

        this.messageList.addAll(messageList);
    }

    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View vi = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(vi);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public TextView messageTime;
        public CircleImageView messageImage;
        public RelativeLayout relativeLayout;
        public RelativeLayout relativeLayout2;
        public RelativeLayout relativeLayout3;
        public ImageView image;

        public MessageViewHolder(View view) {
            super(view);

            messageText = view.findViewById(R.id.single_message_text);
            messageTime = view.findViewById(R.id.single_message_date);
            messageImage = view.findViewById(R.id.single_message_image);
            relativeLayout = view.findViewById(R.id.single_message_layout);
            relativeLayout2 = view.findViewById(R.id.single_message_layout2);
            relativeLayout3 = view.findViewById(R.id.single_message_layout3);
            image = view.findViewById(R.id.single_message_image2);
        }
    }

    public List<Message<?>> getMessageList() {
        return messageList;
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        Message msg = messageList.get(position);
        holder.messageText.setText(msg.getMessageContent().toString());

        if (FirebaseTools.getInstance().getCurrentUser().getUid().equals(msg.getSenderUID())) {
            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.WHITE);
            holder.messageImage.setImageDrawable(null);
            holder.relativeLayout.setGravity(Gravity.RIGHT);
            holder.relativeLayout2.setGravity(Gravity.RIGHT);
            holder.relativeLayout3.setGravity(Gravity.RIGHT);
        } else {
            holder.messageText.setBackgroundResource(R.drawable.message_text_background2);
            holder.messageText.setTextColor(Color.BLACK);
	
	        Contact friend = UserProfile.getLocalProfile().getContactByUID(msg.getSenderUID());
	
	        if (friend.getAvatar() != null)
		        holder.messageImage.setImageBitmap(friend.getAvatar());
	        else holder.messageImage.setImageResource(R.drawable.avatar);

            holder.messageImage.setImageResource(R.drawable.avatar);

            holder.relativeLayout.setGravity(Gravity.LEFT);
            holder.relativeLayout2.setGravity(Gravity.LEFT);
            holder.relativeLayout3.setGravity(Gravity.LEFT);
        }

        Date date = new Date(Long.valueOf(msg.getTimestamp()));
        DateFormat formatter = SimpleDateFormat.getDateTimeInstance();
        holder.messageTime.setText(formatter.format(date));

        if (msg.getMessageType() == Message.Type.IMAGE_MESSAGE) {
            holder.image.setImageBitmap(((ImageMessage) msg).getMessageContent());

            holder.image.setVisibility(View.VISIBLE);
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(MenuActivity.APPLICATION_CONTEXT, "Test", Toast.LENGTH_SHORT)
//                            .show();
	                Intent chatIntent = new Intent(UnificApp
			                .getCurrentActivity(), ImageActivity.class);
	
	                UserProfile prof = UserProfile.getLocalProfile();
	                String name = prof.getUserID().equals(msg.getSenderUID()) ?
			                String.format("%s %s", prof.getName(), prof.getSurname()) :
			                prof.getContactByUID(msg.getSenderUID()).getNameSurname();
	                
                    chatIntent.putExtra("time", formatter.format(date));

                    String url = new String();
                    try {
                        url = saveToInternalStorage(((ImageMessage) msg).getMessageContent());
                    } catch (Exception e) {

                    }
	
	
	                chatIntent.putExtra("image", url);
	                chatIntent.putExtra("author", name);
	                UnificApp.getCurrentActivity().startActivity(chatIntent);
                }
            });
            holder.relativeLayout.setVisibility(View.GONE);
            holder.messageText.setVisibility(View.GONE);
            holder.relativeLayout2.setVisibility(View.VISIBLE);
        } else {
            holder.relativeLayout2.setVisibility(View.GONE);
            holder.image.setVisibility(View.GONE);
            holder.relativeLayout.setVisibility(View.VISIBLE);
            holder.messageText.setVisibility(View.VISIBLE);
	        String UID = FirebaseTools.getInstance().getCurrentUser().getUid();
	        FirebaseTools.getInstance()
			        .acquireDataFromDB("Users/" + UID + "/color_primary", new ObjectAcquireListener()
			        {
				        @Override
				        public void onObjectAcquired(Object obj)
				        {
					        if (obj != null)
					        {
						        holder.messageText
								        .setBackgroundColor(Integer.parseInt(obj.toString()));
					        }
				        }
				
				        @Override
				        public void onError(DatabaseError err)
				        {
					
				        }
			        });
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}