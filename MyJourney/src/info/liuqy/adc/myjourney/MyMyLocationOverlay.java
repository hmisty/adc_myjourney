package info.liuqy.adc.myjourney;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class MyMyLocationOverlay extends MyLocationOverlay {
    private MyJourneyActivity context;
    private MapView mapView;

	public MyMyLocationOverlay(MyJourneyActivity context, MapView mapView) {
		super(context, mapView);

		this.context = context;
		this.mapView = mapView;
	}

	@Override
	protected boolean dispatchTap() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(R.string.tap_dialog_title);
		dialog.setSingleChoiceItems(R.array.tap_dialog_choices, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						switch (item) {
		                case 0: //just flag it
		                    Location loc = MyMyLocationOverlay.this.getLastFix();
		                    if (loc != null) {
		                        double lat0 = loc.getLatitude();
		                        double long0 = loc.getLongitude();
		                        Footprints db = new Footprints(context);
		                        db.open();
		                        db.saveFootprintAt(lat0, long0, Footprints.FLAG.F, null);
		                        db.close();
		                        dialog.dismiss();
		                        context.fpOverlay.loadSavedMarkers(mapView); //reload markers
		                    }
		                    break;
		                case 1: //take a photo
		                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		                    Uri fileUri = MyJourneyActivity.getOutputMediaFileUri(MyJourneyActivity.MEDIA_TYPE_IMAGE); // create a file to save the image
		                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
		                    context.startActivityForResult(intent, MyJourneyActivity.REQUEST_TAKE_PHOTO);
		                    break;
						default:
							Toast.makeText(
									context,
									context.getResources().getStringArray(
											R.array.tap_dialog_choices)[item],
									Toast.LENGTH_SHORT).show();
						}
					}
				});
		dialog.show();
		return true;
	}

}
