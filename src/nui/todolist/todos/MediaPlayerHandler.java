/*
 * Copyright (C) 2013 Ido Gold & Sahar Rehani
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package nui.todolist.todos;

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
