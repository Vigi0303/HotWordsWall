package com.example.vigi.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vigi on 2015/5/8.
 */
public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	HotWordsWall wallView;
	Button refreshButton;

	RequestQueue requestQueue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		wallView = (HotWordsWall) findViewById(R.id.wall);
		refreshButton = (Button) findViewById(R.id.refresh_bt);

		refreshButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshWall();
			}
		});

		wallView.setOnWordClickListener(new HotWordsWall.OnWordClickListener() {
			@Override
			public void onClick(String word) {
				Toast.makeText(MainActivity.this, word + ": you tap me!", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		requestQueue = Volley.newRequestQueue(this);
		refreshWall();
	}

	private void refreshWall() {
		requestQueue.add(new StringRequest("http://search.acfun.tv/suggest?q=%E5%A5%BD",
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						JSONObject jo = JSON.parseObject(response);
						List<SuggestSearch> list = JSON.parseArray(jo.getString("data"), SuggestSearch.class);
						List<String> listStr = new ArrayList<>();
						for (int i = 0; i < list.size(); ++i) {
							listStr.add(list.get(i).getName());
						}
						listStr.remove(0);
						listStr.add("我有十八个字呢我有十八个字呢哈哈哈哈");   //to test whether the max length can work
						wallView.hangWordsTo(listStr);
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {

					}
				}));
	}
}
