/*
 * Copyright (C) 2013 Ido Gold & Sahar Rehani
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package nui.todolist.todos.notifications;

import nui.todolist.todos.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

/** 
 * Processes Geocoder's getFromLocationName on a background task
 * to retrieve best location matches
 */
public class GeocoderProgressTask extends AsyncTask<Void, Void, Boolean> 
{
	private ProgressDialog dialog;
	private int position;
	private String location;
	private Context context;
	private List<Address> addresses;

	public GeocoderProgressTask(Context context, int position, String location)
	{
		this.context = context;
		this.position = position;
		this.location = location;
		dialog = new ProgressDialog(context);
	}

	protected void onPreExecute()
	{
		// showing progress dialog
		this.dialog.setMessage("Searching locations, please wait...");
		this.dialog.show();
	}

	protected Boolean doInBackground(Void... args)
	{
		try
		{
			// generate from given user location, best Geocoder location matches
			Geocoder geocoder = new Geocoder(context, Locale.getDefault());
			addresses = geocoder.getFromLocationName(location, Utils.NUMBER_OF_GPS_RESULTS);
		} 
		catch (IOException e) 
		{
			// no GPS or network connection
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@Override
	protected void onPostExecute(final Boolean success)
	{
		// dismiss the dialog
		if (dialog.isShowing()) 
		{
			dialog.dismiss();
		}

		if (success) 
		{
			if (addresses.isEmpty()) 
			{
				Utils.makeToast(context, "No results were found");
			}
			else
			{
				// create a dynamic choice alert dialog with the location results
				new TaskAlarms(context).createLocationChoiceAlert(addresses,position);
			}
		}
		else
		{
			Utils.makeToast(context, "No GPS or network connection, please enable it");
		}
	}
}