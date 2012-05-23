package info.liuqy.adc.myjourney;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
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
                                context.addFootprint(loc, Footprints.FLAG.F, null);
		                        dialog.dismiss();
		                        context.fpOverlay.loadSavedMarkers(mapView); //reload markers
		                    }
		                    break;
		                case 1: //take a photo
		                	dialog.dismiss();
                            // Setting parameters to camera intent
                            String fileName = String.valueOf(System.currentTimeMillis()) + ".jpg";

                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Images.Media.TITLE, fileName);
                            context.mCapturedImageURI =context.getContentResolver().insert(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

		                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		                    //Uri fileUri = MyJourneyActivity.getOutputMediaFileUri(MyJourneyActivity.MEDIA_TYPE_IMAGE); // create a file to save the image
		                    intent.putExtra(MediaStore.EXTRA_OUTPUT, context.mCapturedImageURI); // set the image file name
                            Log.v("take a photo", context.mCapturedImageURI.toString());
		                    context.startActivityForResult(intent, MyJourneyActivity.REQUEST_TAKE_PHOTO);
		                    break;
		                case 2: //record a video
		                    dialog.dismiss();
		                    Intent intent2 = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		                    //FIXME doesn't work on SE LT18i Android 2.3.4
		                    Uri fileUri2 = MyJourneyActivity.getOutputMediaFileUri(MyJourneyActivity.MEDIA_TYPE_VIDEO);  // create a file to save the video
		                    intent2.putExtra(MediaStore.EXTRA_OUTPUT, fileUri2);  // set the image file name
		                    intent2.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set the video image quality to high
                            Log.v("record a video", fileUri2.toString());
		                    context.startActivityForResult(intent2, MyJourneyActivity.REQUEST_RECORD_VIDEO);
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
