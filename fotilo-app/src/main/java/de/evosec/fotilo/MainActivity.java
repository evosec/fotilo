package de.evosec.fotilo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

public class MainActivity extends AppCompatActivity {

	private static final Logger LOG =
	        LoggerFactory.getLogger(MainActivity.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			startCameraFragment();
		}
	}

	private void startCameraFragment() {
		LOG.info("start Fragment");
		getSupportFragmentManager().beginTransaction()
		    .add(R.id.rootView, CamFragment.newInstance(), "CamFragment")
		    .commitAllowingStateLoss();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
			((CamFragment) getSupportFragmentManager()
			    .findFragmentByTag("CamFragment")).onKeyDown(keyCode);
		} catch (Exception e) {
			LOG.debug("onKeyDown() : " + e);
		}
		return super.onKeyDown(keyCode, event);
	}

}
