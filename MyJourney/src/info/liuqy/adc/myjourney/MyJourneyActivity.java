package info.liuqy.adc.myjourney;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class MyJourneyActivity extends MapActivity implements SensorEventListener {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int REQUEST_TAKE_PHOTO = 100;
    public static final int REQUEST_RECORD_VIDEO = 200;
    
    MapView mapView;
    MapController mapCtrl;
    LocationManager locationManager;
    LocationListener locationListener;
    List<Overlay> mapOverlays;
    MyLocationOverlay myLocationOverlay;
    FootprintOverlay fpOverlay;
    
    SensorManager sensorMgr;
    Sensor accelerometer;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        sensorMgr = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);

        mapOverlays = mapView.getOverlays();

        Drawable defaultMarker = this.getResources().getDrawable(R.drawable.flag_red);
        fpOverlay = new FootprintOverlay(defaultMarker, this);
        fpOverlay.loadSavedMarkers(mapView);
        mapOverlays.add(fpOverlay);

        myLocationOverlay = new MyMyLocationOverlay(this, mapView);
        mapOverlays.add(myLocationOverlay);

        mapCtrl = mapView.getController();
    }

	@Override
	protected void onPause() {
		super.onPause();
		
        myLocationOverlay.disableMyLocation();
        myLocationOverlay.disableCompass();
        
        sensorMgr.unregisterListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableCompass();
        myLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                mapCtrl.animateTo(myLocationOverlay.getMyLocation());
                mapCtrl.setZoom(15); //FIXME magic number
                fpOverlay.loadSavedMarkers(mapView);
            }
        });
        
        sensorMgr.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
    /** Create a file Uri for saving an image or video */
   static Uri getOutputMediaFileUri(int type){
          return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
   static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                  Environment.DIRECTORY_PICTURES), "MyJourney");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.e("MyJourney", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "IMG_"+ timestamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "VID_"+ timestamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (requestCode == REQUEST_TAKE_PHOTO) {
           if (resultCode == RESULT_OK) {
               // Image captured and saved to fileUri specified in the Intent
               Toast.makeText(this, "Image saved to:\n" +
                        data.getData(), Toast.LENGTH_LONG).show();
               Location loc = this.myLocationOverlay.getLastFix();
               if (loc != null) {
                   double lat0 = loc.getLatitude();
                   double long0 = loc.getLongitude();
                   Footprints db = new Footprints(this);
                   db.open();
                   db.saveFootprintAt(lat0, long0, Footprints.FLAG.P, data.getData().toString());
                   db.close();
                   this.fpOverlay.loadSavedMarkers(mapView); //reload markers
               }
           } else if (resultCode == RESULT_CANCELED) {
               //TODO User cancelled the image capture
           } else {
               //TODO Image capture failed, advise user
           }
       }
       
       if (requestCode == REQUEST_RECORD_VIDEO) {
           if (resultCode == RESULT_OK) {
               // Video captured and saved to fileUri specified in the Intent
               Toast.makeText(this, "Video saved to:\n" +
                        data.getData(), Toast.LENGTH_LONG).show();
               Location loc = this.myLocationOverlay.getLastFix();
               if (loc != null) {
                   double lat0 = loc.getLatitude();
                   double long0 = loc.getLongitude();
                   Footprints db = new Footprints(this);
                   db.open();
                   db.saveFootprintAt(lat0, long0, Footprints.FLAG.V, data.getData().toString());
                   db.close();
                   this.fpOverlay.loadSavedMarkers(mapView); //reload markers
               }
           } else if (resultCode == RESULT_CANCELED) {
               //TODO User cancelled the video capture
           } else {
               //TODO Video capture failed, advise user
           }
       }

   }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

    private static final int SHIFT_LAT = 100000, SHIFT_LONG = 100000;
    
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.equals(accelerometer)) {
			float accX = event.values[0];
			float accY = event.values[1];
			GeoPoint p = mapView.getMapCenter();
			GeoPoint p1, p2;
			float accThreshold = 1.0f;
			if (accX > accThreshold) { // lean left
				p1 = new GeoPoint(p.getLatitudeE6(), p.getLongitudeE6()	- SHIFT_LONG);
			} else if (accX < -accThreshold) { // lean right
				p1 = new GeoPoint(p.getLatitudeE6(), p.getLongitudeE6()	+ SHIFT_LONG);
			} else {
				p1 = p;
			}
			if (accY > accThreshold) { // roll backward
				p2 = new GeoPoint(p1.getLatitudeE6() - SHIFT_LAT, p1.getLongitudeE6());
			} else if (accY < -accThreshold) { // roll forward
				p2 = new GeoPoint(p1.getLatitudeE6() + SHIFT_LAT, p1.getLongitudeE6());
			} else {
				p2 = p1;
			}
			mapCtrl.animateTo(p2);
		}
	}

}