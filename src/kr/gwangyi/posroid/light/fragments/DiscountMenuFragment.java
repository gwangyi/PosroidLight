package kr.gwangyi.posroid.light.fragments;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;
import kr.gwangyi.posroid.light.R;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ToggleButton;

public class DiscountMenuFragment extends Fragment implements LocationListener
{
	private String name;
	private String [] menu;
	private double latitude, longitude;
	private MapPoint point = null;
	private MapView map = null;

	public static DiscountMenuFragment newInstance(String name, String[] menu, double latitude, double longitude)
	{
		DiscountMenuFragment frag = new DiscountMenuFragment();
		Bundle args = new Bundle();
		args.putString("name", name);
		args.putStringArray("menu", menu);
		args.putDouble("latitude", latitude);
		args.putDouble("longitude", longitude);
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments();
		name = args.getString("name");
		menu = args.getStringArray("menu");
		latitude = args.getDouble("latitude");
		longitude = args.getDouble("longitude");
		point = MapPoint.mapPointWithGeoCoord(latitude, longitude);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout view = (LinearLayout)inflater.inflate(R.layout.discount_menu, container, false);
		ListView menu = (ListView)view.findViewById(R.id.menu);
		map = new MapView(getActivity());
		
		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		map.setLayoutParams(param);
		if(this.menu != null)
			menu.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, this.menu));
		else
			menu.setVisibility(View.GONE);
		map.setDaumMapApiKey("DAUM_MAP_API_KEY"); // TODO: Replace to your real API key
		map.setMapType(MapView.MapType.Standard);
		
		MapPOIItem poi = new MapPOIItem();
		poi.setItemName(name);
		poi.setMapPoint(point);
		poi.setMarkerType(MapPOIItem.MarkerType.BluePin);
		poi.setShowAnimationType(MapPOIItem.ShowAnimationType.DropFromHeaven);
		map.addPOIItem(poi);
		map.fitMapViewAreaToShowAllPOIItems();
		
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				map.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
				map.fitMapViewAreaToShowAllPOIItems();
			}
		});

		ToggleButton tracking = (ToggleButton)view.findViewById(R.id.track);
		tracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
				{
					LocationManager lm = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
					lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, DiscountMenuFragment.this);
					lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, DiscountMenuFragment.this);
					map.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
				}
				else
				{
					LocationManager lm = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
					lm.removeUpdates(DiscountMenuFragment.this);
					map.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
					map.fitMapViewAreaToShowAllPOIItems();
				}
			}
		});
		
		view.addView(map);
		
		return view;
	}

	@Override
	public void onLocationChanged(Location location) {
		MapPoint currentPoint = MapPoint.mapPointWithGeoCoord(location.getLatitude(), location.getLongitude());
		map.fitMapViewAreaToShowMapPoints(new MapPoint[] { currentPoint, point });
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
	@Override
	public void onDestroy() {
		LocationManager lm = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
		lm.removeUpdates(DiscountMenuFragment.this);
		super.onDestroy();
	}
}
