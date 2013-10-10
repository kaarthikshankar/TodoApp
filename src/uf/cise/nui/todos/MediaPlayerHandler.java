package uf.cise.nui.todos;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Controlling media player actions
 */
public class MediaPlayerHandler 
{
	private MediaPlayer mediaPlayer; 
	private Context context;
	
	public MediaPlayerHandler(Context context)
	{
		this.context = context;
	}
	
	/** 
	 * Handles playing audio in the application
	 *
	 * @param soundFile
	 */ 
	public void playAudio(int soundFile)
	{
		try 
		{
			killMediaPlayer();
			mediaPlayer = MediaPlayer.create(context, soundFile);
			mediaPlayer.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Release MediaPlayer resources
	 */
	public void killMediaPlayer()
	{
		if (mediaPlayer != null)
		{
			try
			{
				mediaPlayer.release();
				mediaPlayer = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
