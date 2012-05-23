package info.liuqy.adc.myjourney;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * Created with IntelliJ IDEA.
 * User: dean
 * Date: 12-5-22
 * Time: 上午8:05
 * To change this template use File | Settings | File Templates.
 */
public class PhotoViewer extends Activity {
    String uriString;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photpviewer);
        Intent i = this.getIntent();
        uriString = i.getStringExtra("uri");
        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageURI(Uri.parse(uriString));
    }
}