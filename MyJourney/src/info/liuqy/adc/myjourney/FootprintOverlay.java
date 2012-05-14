package info.liuqy.adc.myjourney;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class FootprintOverlay extends ItemizedOverlay<OverlayItem> {
    private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
    private Context context;

	public FootprintOverlay(Drawable defaultMarker, Context context) {
        super(boundCenterBottom(defaultMarker)); //adjust (0,0) to center bottom
        this.context = context;
	}

	@Override
	protected OverlayItem createItem(int i) {
        return overlays.get(i);
    }

	@Override
	public int size() {
        return overlays.size();
	}

    public void loadSavedMarkers(MapView mapView) {
        GeoPoint p = mapView.getMapCenter();
        
        int lat1 = p.getLatitudeE6() - mapView.getLatitudeSpan()/2;
        int lat2 = p.getLatitudeE6() + mapView.getLatitudeSpan()/2;
        int long1 = p.getLongitudeE6() - mapView.getLongitudeSpan()/2;
        int long2 = p.getLongitudeE6() + mapView.getLongitudeSpan()/2;
        double lat1d = (double)lat1 / 1e6;
        double long1d = (double)long1 / 1e6;
        double lat2d = (double)lat2 / 1e6;
        double long2d = (double)long2 / 1e6;
        
        Footprints db = new Footprints(context);

        db.open();
        Cursor cur = db.getFootprintsIn(lat1d, long1d, lat2d, long2d);

        while (cur.moveToNext()) {
            double lat0 = cur.getDouble(cur.getColumnIndex(Footprints.FIELD_LATITUDE));
            double long0 = cur.getDouble(cur.getColumnIndex(Footprints.FIELD_LONGITUDE));
            String flag = cur.getString(cur.getColumnIndex(Footprints.FIELD_FLAG));
            String res = cur.getString(cur.getColumnIndex(Footprints.FIELD_RESOURCE));
            GeoPoint p0 = new GeoPoint((int)(lat0*1e6), (int)(long0*1e6));
            OverlayItem item = new OverlayItem(p0, flag.toString(), res);
            overlays.add(item);
        }
        
        cur.close();
        db.close();
        
        populate(); //draw the overlay
    }

    @Override
    protected boolean onTap(int index) {
      OverlayItem item = overlays.get(index);
      AlertDialog.Builder dialog = new AlertDialog.Builder(context);
      dialog.setTitle(item.getTitle());
      dialog.setMessage(item.getSnippet());
      
      Footprints.FLAG flag = Footprints.FLAG.valueOf(item.getTitle());
      if (flag == Footprints.FLAG.V) {
          final String uriString = item.getSnippet();
          dialog.setMessage(uriString + " is a video. Play it?")
          .setCancelable(false)
          .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                   Intent i = new Intent(context, VideoPlayer.class);
                   i.putExtra("uri", uriString);
                   context.startActivity(i);
               }
           })
           .setNegativeButton("No", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
               }
           });
      }

      dialog.show();
      return true;
    }

}
