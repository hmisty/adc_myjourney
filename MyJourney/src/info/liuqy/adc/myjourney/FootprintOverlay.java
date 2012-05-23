package info.liuqy.adc.myjourney;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.*;
import android.graphics.drawable.Drawable;

import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.google.android.maps.*;

import static com.google.android.maps.OverlayItem.ITEM_STATE_FOCUSED_MASK;

public class FootprintOverlay extends ItemizedOverlay<OverlayItem> {
    private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
    private ArrayList<OverlayItem> overlaysSelected = new ArrayList<OverlayItem>();
    private Context context;
    private Drawable defaultMarker;

    public FootprintOverlay(Drawable defaultMarker, Context context) {
        super(boundCenterBottom(defaultMarker)); //adjust (0,0) to center bottom
        this.context = context;
        this.defaultMarker = defaultMarker;
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
    public boolean onTap(GeoPoint geoPoint, MapView mapView) {
        Point ptTap = mapView.getProjection().toPixels(geoPoint, null);
        overlaysSelected.clear();
        for(OverlayItem item : overlays) {
            Point itemPoint = mapView.getProjection().toPixels(item.getPoint(), null);
            int relativeX = ptTap.x - itemPoint.x;
            int relativeY = ptTap.y - itemPoint.y;
            Drawable marker = item.getMarker(ITEM_STATE_FOCUSED_MASK);
            if(marker == null) {
                marker = this.defaultMarker;
            }
            if(hitTest(item, marker, relativeX, relativeY)){
                overlaysSelected.add(item);
            }
        }
        if(overlaysSelected.size()>1){
            Toast.makeText(this.context, "overlaysSelected:"+overlaysSelected.size(), Toast.LENGTH_SHORT).show();
            showTapList();
            return true;
        }else {
            return super.onTap(geoPoint, mapView);
        }
    }

    private  void  showTapList(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(R.string.tap_dialog_title);
        ArrayList<String> tapItems = new ArrayList<String>();
        for(OverlayItem item : overlaysSelected) {
            tapItems.add(item.getTitle());
        }

        dialog.setSingleChoiceItems(tapItems.toArray(new CharSequence[tapItems.size()]),
                -1,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                 // dialogInterface.dismiss();
                onTapItem(overlaysSelected.get(i));
            }
        });
        dialog.show();
    }

    @Override
    protected boolean onTap(int index) {
        OverlayItem item = overlays.get(index);
        onTapItem(item);
        return true;
    }

    private void onTapItem(OverlayItem item) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(item.getTitle());
        dialog.setMessage(item.getSnippet());

        Footprints.FLAG flag = Footprints.FLAG.valueOf(item.getTitle());
        if (flag == Footprints.FLAG.P) {
            final String uriString = item.getSnippet();
            dialog.setMessage(uriString + " is a Photo. Show it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent i = new Intent(context, PhotoViewer.class);
                            i.putExtra("uri", uriString);
                            context.startActivity(i);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
        }else if (flag == Footprints.FLAG.V) {
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
    }


    @Override
    public boolean draw(Canvas canvas, MapView mapView, boolean b, long l) {

        if(overlays !=null && overlays.size()>1){
            Paint   mPaint = new Paint();
            mPaint.setDither(true);
            mPaint.setColor(Color.BLUE);
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(4);

            Projection projection = ((MyJourneyActivity)this.context).projection;

            Path path = new Path();
            boolean bMoveTo = true;
            for(OverlayItem item : overlays) {
                Point point = new Point();
                GeoPoint geoPoint = item.getPoint();
                projection.toPixels(geoPoint, point);
                if(bMoveTo){
                    path.moveTo(point.x, point.y);
                    bMoveTo = false;
                } else {
                    path.lineTo(point.x, point.y);
                }
            }
            canvas.drawPath(path, mPaint);
        }

        return super.draw(canvas, mapView, b, l);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
