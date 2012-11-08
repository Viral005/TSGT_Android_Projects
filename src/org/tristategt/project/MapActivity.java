package org.tristategt.project;

import java.io.File;

import org.tristategt.common.CalloutLongPressListener;
import org.tristategt.common.DBAction.FeaturesDBAdapter;
import org.tristategt.common.Dialogs.DBManagerDialog;
import org.tristategt.common.Draw.CreateLineNoteTouchListener;
import org.tristategt.common.Draw.CreatePolygonNoteTouchListener;
import org.tristategt.common.Draw.CreatePtNoteTouchListener;
import org.tristategt.common.GenericMapTouchListener;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationService;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;


public class MapActivity extends Activity {
	
	private File fileBase;
	private Boolean createdDirs;
	private MapView mMapView;
	private ArcGISLocalTiledLayer gisLayers;
	private ArcGISTiledMapServiceLayer bgLayerStreet, bgLayerAerial;
	private GraphicsLayer graphicsLayer, scratchGraphicsLayer;
	private LocationService ls;
	private Menu myMenu;
	private FeaturesDBAdapter dbAdapter;
	private CreatePtNoteTouchListener drawPtTouchListener;
	private CreateLineNoteTouchListener drawLineTouchListener;
	private CreatePolygonNoteTouchListener drawPolygonTouchListener;
	private CalloutLongPressListener calloutLongPressListener;
	private GenericMapTouchListener genericMapTouchListener;
	private FragmentManager fm;
	private Drawable drawableBlueFlag, drawableRedFlag, drawableRedLine, drawableBlueLine, drawableRedPolygon, drawableBluePolygon;
	private TextView noteView;
	private View content;
	private Callout callout;
	private static final String aerialURL = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer";
	private static final String streetURL = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		mMapView = (MapView)findViewById(R.id.map);
		createdDirs = createRequiredDirs();
		
		callout = mMapView.getCallout();
		content = createContent();
		
		graphicsLayer = new GraphicsLayer();
		scratchGraphicsLayer = new GraphicsLayer();
		bgLayerStreet = new ArcGISTiledMapServiceLayer(streetURL);
		bgLayerAerial = new ArcGISTiledMapServiceLayer(aerialURL);
				
		//Add layer to the map if it exist else load base map
		if(!createdDirs){
			mMapView.addLayer(gisLayers);
			mMapView.addLayer(scratchGraphicsLayer);
			mMapView.addLayer(graphicsLayer);
		}else{
			mMapView.addLayer(bgLayerStreet);
			mMapView.addLayer(scratchGraphicsLayer);
			mMapView.addLayer(graphicsLayer);
		}
		
		dbAdapter = new FeaturesDBAdapter(this);
		
		drawableBlueFlag = getResources().getDrawable(R.drawable.flag_blue);
		drawableRedFlag = getResources().getDrawable(R.drawable.flag_red);
		drawableRedLine = getResources().getDrawable(R.drawable.redline);
		drawableBlueLine = getResources().getDrawable(R.drawable.line);
		drawableRedPolygon = getResources().getDrawable(R.drawable.redpolygon);
		drawableBluePolygon = getResources().getDrawable(R.drawable.polygon);
		fm = getFragmentManager();
		drawPtTouchListener = new CreatePtNoteTouchListener(MapActivity.this, mMapView, graphicsLayer, drawableBlueFlag, dbAdapter, fm);	
		drawLineTouchListener =  new CreateLineNoteTouchListener(MapActivity.this, mMapView, graphicsLayer, scratchGraphicsLayer, dbAdapter, fm);
		drawPolygonTouchListener = new CreatePolygonNoteTouchListener(MapActivity.this, mMapView, graphicsLayer, scratchGraphicsLayer, dbAdapter, fm);		
		calloutLongPressListener = new CalloutLongPressListener(content, callout, graphicsLayer, mMapView);
		genericMapTouchListener = new GenericMapTouchListener(MapActivity.this, mMapView);
		mMapView.setOnLongPressListener(calloutLongPressListener);
		mMapView.setOnTouchListener(genericMapTouchListener);
    }

	@Override 
	protected void onDestroy() { 
		super.onDestroy();
 }
	@Override
	protected void onPause() {
		super.onPause();
		mMapView.pause();
 }
	@Override 	protected void onResume() {
		super.onResume(); 
		mMapView.unpause();
	}
	
	//GPS Service
	public void getMyLocation(){
		ls = mMapView.getLocationService();
			
		if(ls.isStarted() == true){
			ls.stop();
			return;
		}
						
		ls.setAutoPan(true);		
		ls.start();
	}
	
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mapmenu, menu);
		myMenu = menu;
		
		return true;		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId())
		{
			case R.id.itemDropPoint:
				Drawable d = item.getIcon();
				boolean b = d.equals(drawableBlueFlag);
				
				Drawable _icon = b ? drawableRedFlag: drawableBlueFlag;
				item.setIcon(_icon);
				
				//set other items to be red
				item = myMenu.findItem(R.id.itemDrawLine);
			    item.setIcon(drawableRedLine);
			    
			    item = myMenu.findItem(R.id.itemDrawPolygon);
			    item.setIcon(drawableRedPolygon);
				
				if(b)
					mMapView.setOnTouchListener(new GenericMapTouchListener(mMapView.getContext(), mMapView));
				else
					mMapView.setOnTouchListener(drawPtTouchListener);
				break;
			case R.id.itemDrawLine:
				d = item.getIcon();
				b = d.equals(drawableBlueLine);
				
				_icon = b ? drawableRedLine: drawableBlueLine;
				item.setIcon(_icon);
				
				//set other items to be red
				item = myMenu.findItem(R.id.itemDropPoint);
			    item.setIcon(drawableRedFlag);
			    
			    item = myMenu.findItem(R.id.itemDrawPolygon);
			    item.setIcon(drawableRedPolygon);
				
				if(b)
					mMapView.setOnTouchListener(new GenericMapTouchListener(mMapView.getContext(), mMapView));
				else
					mMapView.setOnTouchListener(drawLineTouchListener);
				break;
			case R.id.itemDrawPolygon:
				d = item.getIcon();
				b = d.equals(drawableBluePolygon);
				
				_icon = b ? drawableRedPolygon: drawableBluePolygon;
				item.setIcon(_icon);
				
				//set other items to be red
				item = myMenu.findItem(R.id.itemDropPoint);
			    item.setIcon(drawableRedFlag);
			    
			    item = myMenu.findItem(R.id.itemDrawLine);
			    item.setIcon(drawableRedLine);
				
				if(b)
					mMapView.setOnTouchListener(new GenericMapTouchListener(mMapView.getContext(), mMapView));
				else
					mMapView.setOnTouchListener(drawPolygonTouchListener);
				break;
			case R.id.itemMyLocation:
				getMyLocation();
				break;
			case R.id.itemManager:				
				createDBManagerDialog();
				break;
			case R.id.itemAddAerial:
				mMapView.addLayer(bgLayerAerial, 0);
				if(mMapView.getLayerByURL(streetURL) != null){
					mMapView.removeLayer(bgLayerStreet);
				}
				break;
			case R.id.itemAddStreet:
				mMapView.addLayer(bgLayerStreet, 0);
				if(mMapView.getLayerByURL(aerialURL) != null){
					mMapView.removeLayer(bgLayerAerial);
				}
				break;
		}
		return true;
	}
	
	private void createDBManagerDialog(){
		FragmentTransaction ft = getFragmentManager().beginTransaction(); 
		DBManagerDialog myDialog = DBManagerDialog.newInstance("Message");
		myDialog.setDbAdapter(dbAdapter);
		myDialog.setGraphicsLayer(graphicsLayer);
		myDialog.setDrawable(drawableBlueFlag);
		myDialog.show(ft, "");
	}
	
	//creates the content of the callout
	public View createContent() {
		// create linear layout for the entire view
		LinearLayout layout = new LinearLayout(this);
		layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.VERTICAL);

		// create TextView for the title
		noteView = new TextView(this);
		noteView.setId(1);

		noteView.setTextColor(Color.WHITE);
		noteView.setTextSize(12);
		noteView.setMaxWidth(300);
		layout.addView(noteView);

		return layout;
	}
	
	public boolean createRequiredDirs()
	{
		if (Environment.getExternalStorageState() == null) {
			fileBase = new File(Environment.getDataDirectory() + "/GIS_Cache");
			if(!fileBase.exists()) {
				fileBase.mkdirs();
				return true;
			}
		}else if (Environment.getExternalStorageState() != null){
			fileBase = new File(Environment.getExternalStorageDirectory() + "/GIS_Cache");
			if(!fileBase.exists()) {
				fileBase.mkdirs();
				return true;
			}
		}		
		try{
			gisLayers = new ArcGISLocalTiledLayer("file://" + fileBase.getAbsolutePath());
		}catch(Exception e){
			return true;
		}
		return false;
	}
}