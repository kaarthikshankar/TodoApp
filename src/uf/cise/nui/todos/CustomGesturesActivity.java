package uf.cise.nui.todos;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.widget.Toast;
import android.gesture.Gesture;
import java.util.ArrayList;

public class CustomGesturesActivity extends Activity implements OnGesturePerformedListener {

	private GestureLibrary gLibrary;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_custom_gestures);
		
		gLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!gLibrary.load()) {
            finish();
       }  
        
        GestureOverlayView gOverlay = 
                (GestureOverlayView) findViewById(R.id.gOverlay);
            gOverlay.addOnGesturePerformedListener(this); 


	}
	
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        ArrayList<Prediction> predictions = gLibrary.recognize(gesture);
        
        if (predictions.size() > 0 && predictions.get(0).score > 1.0) {
            
        	String action = predictions.get(0).name;
        	
            Toast.makeText(this, action, Toast.LENGTH_SHORT).show();
        }
    }
    }