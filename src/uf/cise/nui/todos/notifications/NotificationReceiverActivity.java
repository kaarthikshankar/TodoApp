package uf.cise.nui.todos.notifications;

import uf.cise.nui.todos.R;
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