package info.liuqy.adc.myjourney;

import android.content.Context;

import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class MyMyLocationOverlay extends MyLocationOverlay {
    private Context context;
    private MapView mapView;

	public MyMyLocationOverlay(Context context, MapView mapView) {
		super(context, mapView);
		
        this.context = context;
        this.mapView = mapView;
	}

	@Override
	protected boolean dispatchTap() {
		// TODO Auto-generated method stub
		return super.dispatchTap();
	}

}
