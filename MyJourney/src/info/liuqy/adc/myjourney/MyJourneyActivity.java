package info.liuqy.adc.myjourney;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.*;

public class MyJourneyActivity extends MapActivity implements SensorEventListener {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int REQUEST_TAKE_PHOTO = 100;
    public static final int REQUEST_RECORD_VIDEO = 200;

    private SimpleMapView mapView;
    private MapController mapCtrl;
    private LocationManager locationManager;
    private List<Overlay> mapOverlays;
    private MyLocationOverlay myLocationOverlay;
    private Location lastLocation;
    Projection projection;
    FootprintOverlay fpOverlay;
    Uri mCapturedImageURI;

    // For shake motion detection.
    private SensorManager sensorMgr;
    private Sensor accelerometer;
    private long lastUpdate = -1;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 800;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        sensorMgr = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mapView = (SimpleMapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);

        mapOverlays = mapView.getOverlays();

        Drawable defaultMarker = this.getResources().getDrawable(R.drawable.flag_red);
        fpOverlay = new FootprintOverlay(defaultMarker, this);
        fpOverlay.loadSavedMarkers(mapView);
        mapOverlays.add(fpOverlay);

        myLocationOverlay = new MyMyLocationOverlay(this, mapView);
        mapOverlays.add(myLocationOverlay);

        mapCtrl = mapView.getController();
        projection = mapView.getProjection();

        mapView.addPanChangeListener(new PanChangeListener() {

            @Override
            public void onPan(GeoPoint old, GeoPoint current) {
                fpOverlay.loadSavedMarkers(mapView);
            }
        });

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);

        Location location = locationManager.getLastKnownLocation(provider);
        updateWithNewLocation(location);

        locationManager.requestLocationUpdates(provider, 100, 0, locationListener);

    }

    private final LocationListener locationListener = new LocationListener()
    {

        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(MyJourneyActivity.this, "onLocationChanged:" + location, Toast.LENGTH_SHORT);
            updateWithNewLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateWithNewLocation(null);
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    };

    private void updateWithNewLocation(Location location) {
        if (lastLocation != null) {
            float distance = lastLocation.distanceTo(location);
            if (distance > 500){
                addFootprint(location, Footprints.FLAG.F, null);
                lastLocation = location;
            }

        } else {
            lastLocation = location;
        }
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
               String uri = null;
               if(data == null) {
                   uri = mCapturedImageURI.toString();
               } else {
                   uri = data.getDataString();
               }
               // Image captured and saved to fileUri specified in the Intent
               Toast.makeText(this, "Image saved to:\n" +
                       uri, Toast.LENGTH_LONG).show();


               Location loc = this.myLocationOverlay.getLastFix();
               if (loc != null) {
                   addFootprint(loc, Footprints.FLAG.P, uri);
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
               Log.v("Intent data",data.toString());
               Toast.makeText(this, "Video saved to:\n" +
                        data.getData(), Toast.LENGTH_LONG).show();
               String uri = data.getDataString();
               Location loc = this.myLocationOverlay.getLastFix();
               if (loc != null) {
                   addFootprint(loc, Footprints.FLAG.V, uri);
                   this.fpOverlay.loadSavedMarkers(mapView); //reload markers
               }
           } else if (resultCode == RESULT_CANCELED) {
               //TODO User cancelled the video capture
           } else {
               //TODO Video capture failed, advise user
           }
       }

   }

    public void addFootprint(Location loc, Footprints.FLAG flag, String uri) {
        double lat0 = loc.getLatitude();
        double long0 = loc.getLongitude();
        Footprints db = new Footprints(this);
        db.open();
        db.saveFootprintAt(lat0, long0, flag, uri);
        db.close();
    }

    @Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.equals(accelerometer)) {
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate)>100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float x = event.values[SensorManager.DATA_X];
                float y = event.values[SensorManager.DATA_Y];
                float z = event.values[SensorManager.DATA_Z];

                float speed = Math.abs(x + y + z - last_x - last_y - last_z)
                        / diffTime * 10000;
                if (speed > SHAKE_THRESHOLD) {
                    // yes, this is a shake action! Do something about it!
                    Toast.makeText(this,"this is a shake action",Toast.LENGTH_SHORT).show();
                    if (myLocationOverlay!=null && myLocationOverlay.getMyLocation()!=null)
                    {
                        mapCtrl.animateTo(myLocationOverlay.getMyLocation());
                        mapCtrl.setZoom(15);
                        fpOverlay.loadSavedMarkers(mapView);
                    }

                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }
}