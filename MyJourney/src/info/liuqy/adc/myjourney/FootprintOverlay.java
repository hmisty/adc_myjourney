package info.liuqy.adc.myjourney;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
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

}
