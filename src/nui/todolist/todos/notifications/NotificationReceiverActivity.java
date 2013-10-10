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

import nui.todolist.todos.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

/** 
 * Triggered when clicking a notification
 */
public class NotificationReceiverActivity extends Activity 
{
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.result_notification);
    setFinishOnTouchOutside(true);
    
    TextView text = (TextView) findViewById(R.id.textView_notification);
    Intent message = getIntent();
    setTitle(message.getStringExtra("windowTitle"));
    String content = message.getStringExtra("taskContent");
    text.setText(content + "\n\n");
  }
} 