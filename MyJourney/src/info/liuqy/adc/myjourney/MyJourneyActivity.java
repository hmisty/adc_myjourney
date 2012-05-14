package info.liuqy.adc.myjourney;

import java.util.List;

import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class MyJourneyActivity extends MapActivity {
    MapView mapView;
    MapController mapCtrl;
    LocationManager locationManager;
    LocationListener locationListener;
    List<Overlay> mapOverlays;
    MyLocationOverlay myLocationOverlay;
    FootprintOverlay fpOverlay;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
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
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}