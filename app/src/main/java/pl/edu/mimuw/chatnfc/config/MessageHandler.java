package pl.edu.mimuw.chatnfc.config;

import pl.edu.mimuw.chatnfc.messanging.ConfigMessage;
import pl.edu.mimuw.chatnfc.messanging.Message;
import pl.edu.mimuw.chatnfc.tools.ObjectIO;

public class MessageHandler
{
	private MessageHandler()
	{
	}
	
	public static void dispatchMessage(String friendContact, Message<?> msg)
	{
		if (msg.getMessageType() == Message.Type.CONFIG_MESSAGE && !msg.getSenderUID()
				.equals(UserProfile.getLocalProfile().getUserID()))
		{
			dispatchConfigMessage(friendContact, (ConfigMessage) msg);
		}
	}
	
	private static void dispatchConfigMessage(String friendContact, ConfigMessage msg)
	{
		UserProfile prof = UserProfile.getLocalProfile();
		
		if ((msg.getMessageFlags() & ConfigMessage.CONFIG_FLAG_CHANGE_AVATAR) != 0)
		{
			prof.getContactByUID(friendContact).setAvatar(
					ObjectIO.bitmapFromString(msg.getMessageContent().get("IMAGE")));
			UserProfileProvider.saveLocalUserProfile(prof);
		}
		if ((msg.getMessageFlags() & ConfigMessage.CONFIG_FLAG_CHANGE_STATUS) != 0)
		{
			prof.getContactByUID(friendContact).setStatus(msg.getMessageContent().get("STATUS"));
			UserProfileProvider.saveLocalUserProfile(prof);
		}
	}
}
