package info.liuqy.adc.myjourney;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dean
 * Date: 12-5-23
 * Time: 上午10:58
 * To change this template use File | Settings | File Templates.
 */
public class SimpleMapView extends MapView {

    private int currentZoomLevel = -1;
    private GeoPoint currentCenter;
    private List<ZoomChangeListener> zoomEvents = new ArrayList<ZoomChangeListener>();
    private List<PanChangeListener> panEvents = new ArrayList<PanChangeListener>();

    public SimpleMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SimpleMapView(Context context, String apiKey) {
        super(context, apiKey);
    }

    public SimpleMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    /**
     *
     * @return
     */
    public int[][] getBounds() {

        GeoPoint center = getMapCenter();
        int latitudeSpan = getLatitudeSpan();
        int longtitudeSpan = getLongitudeSpan();
        int[][] bounds = new int[2][2];

        bounds[0][0] = center.getLatitudeE6() - (latitudeSpan / 2);
        bounds[0][1] = center.getLongitudeE6() - (longtitudeSpan / 2);

        bounds[1][0] = center.getLatitudeE6() + (latitudeSpan / 2);
        bounds[1][1] = center.getLongitudeE6() + (longtitudeSpan / 2);
        return bounds;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            GeoPoint centerGeoPoint = this.getMapCenter();
            if (currentCenter == null ||
                    (currentCenter.getLatitudeE6() != centerGeoPoint.getLatitudeE6()) ||
                    (currentCenter.getLongitudeE6() != centerGeoPoint.getLongitudeE6()) ) {
                firePanEvent(currentCenter, this.getMapCenter());
            }
            currentCenter = this.getMapCenter();
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if(getZoomLevel() != currentZoomLevel){
            fireZoomLevel(currentZoomLevel, getZoomLevel());
            currentZoomLevel = getZoomLevel();
        }
    }

    @Override
    public void setSatellite(boolean on){
        super.setSatellite(on);
    }

    @Override
    public MapController getController(){
        return super.getController();
    }

    private void fireZoomLevel(int old, int current){
        for(ZoomChangeListener event : zoomEvents){
            event.onZoom(old, current);
        }
    }

    private void firePanEvent(GeoPoint old, GeoPoint current){
        for(PanChangeListener event : panEvents){
            event.onPan(old, current);
        }
    }

    public void addZoomChangeListener(ZoomChangeListener listener){
        this.zoomEvents.add(listener);
    }

    public void addPanChangeListener(PanChangeListener listener){
        this.panEvents.add(listener);
    }
}
