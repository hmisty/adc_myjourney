package info.liuqy.adc.myjourney;

import com.google.android.maps.GeoPoint;

/**
 * Created with IntelliJ IDEA.
 * User: dean
 * Date: 12-5-23
 * Time: 上午11:03
 * To change this template use File | Settings | File Templates.
 */
public interface PanChangeListener {
    void onPan(GeoPoint old, GeoPoint current);
}
