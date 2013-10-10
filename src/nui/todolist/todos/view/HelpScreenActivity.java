/*
 * Copyright (C) 2013 Ido Gold & Sahar Rehani
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package nui.todolist.todos.view;

import nui.todolist.todos.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

/**
 * Shows help screen  
 */
public class HelpScreenActivity extends Activity implements OnClickListener
{
	// number of pages in the help screen
	private int numberOfPages;
	// current page number
	private int pageIndex = 1;
	// holds the pages id's
	private int[] pages = { R.drawable.help1, R.drawable.help2, R.drawable.help3 };
	private RelativeLayout layout;
	
	@Override
	  protected void onCreate(Bundle savedInstanceState)
	  {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	    setContentView(R.layout.help_screen);
	    setFinishOnTouchOutside(true);
	    
	    layout = (RelativeLayout) findViewById(R.id.helpLayout);
	    Button nextBtn = (Button) findViewById(R.id.nextSlide);
	    nextBtn.setOnClickListener(this);
	    
	    numberOfPages = pages.length;
	  }

	@Override
	public void onClick(View view) 
	{
		if (pageIndex == numberOfPages + 1)
		{
			finish();
		}
		
		try
		{
			layout.setBackgroundResource(pages[pageIndex - 1]);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			finish();
		}
		
		pageIndex++;
	}
}
