package org.tristategt.project;

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


public class MapActivity extends Activity {
	
	MapView mMapView;
	ArcGISLocalTiledLayer gisLayers;
	GraphicsLayer graphicsLayer, scratchGraphicsLayer;
	LocationService ls;
	final static double SEARCH_RADIUS = 5;
	Menu myMenu;
	FeaturesDBAdapter dbAdapter;
	CreatePtNoteTouchListener drawPtTouchListener;
	CreateLineNoteTouchListener drawLineTouchListener;
	CreatePolygonNoteTouchListener drawPolygonTouchListener;
	CalloutLongPressListener calloutLongPressListener;
	GenericMapTouchListener genericMapTouchListener;
	FragmentManager fm;
	Drawable drawableBlueFlag, drawableRedFlag, drawableRedLine, drawableBlueLine, drawableRedPolygon, drawableBluePolygon;
	TextView noteView;
	View content;
	Callout callout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		mMapView = (MapView)findViewById(R.id.map);
		
		callout = mMapView.getCallout();
		content = createContent();
		
		graphicsLayer = new GraphicsLayer();
		scratchGraphicsLayer = new GraphicsLayer();
		gisLayers = new ArcGISLocalTiledLayer("file:///mnt/sdcard/GIS_Cache");
		mMapView.addLayer(gisLayers);
		mMapView.addLayer(scratchGraphicsLayer);
		mMapView.addLayer(graphicsLayer);
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
}