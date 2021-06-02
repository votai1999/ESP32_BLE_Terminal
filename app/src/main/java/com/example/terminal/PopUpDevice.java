package com.example.terminal;

import androidx.annotation.RequiresApi;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class PopUpDevice extends Activity {
    ListView listView;
    ArrayList<Spannable> mDeviceList = new ArrayList<Spannable>();
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    LocationManager manager;
    Intent intent = new Intent();
    SwipeRefreshLayout swipeRefreshLayout;
    Spannable spannable;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_device);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);// Trong suot nen PopUp
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .9), (int) (height * .5));
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = 300;
        getWindow().setAttributes(params);
        listView = (ListView) findViewById(R.id.listView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout);
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        if (listView != null)
            listView.setAdapter(null);
        mBluetoothAdapter.startDiscovery();
        getMacBle();
        Swiperefresh();
    }

    public void Swiperefresh() {
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    public void onRefresh() {
                        if (listView != null) {
                            mBluetoothAdapter.cancelDiscovery();
//                            listView.setAdapter(null);
                            mDeviceList.clear();
                        }
                        Log.d("dshjfasfg", "onRefresh: ");
                        mBluetoothAdapter.startDiscovery();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (listView != null)
            listView.setAdapter(null);
        mBluetoothAdapter.startDiscovery();
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null && getCategoryPos(spannable) == -1) {
                    String a = device.getName() + "\n" + device.getAddress();
                    spannable = new SpannableString(a);
                    spannable.setSpan(new ForegroundColorSpan(Color.rgb(255, 150, 0)),
                            0, device.getName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new ForegroundColorSpan(Color.rgb(0, 150, 150)),
                            device.getName().length() + 1, a.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mDeviceList.add(spannable);
                    Log.i("BT", device.getName() + "\n" + device.getAddress());
                    listView.setAdapter(new ArrayAdapter<Spannable>(context,
                            android.R.layout.simple_list_item_1, mDeviceList));
                    if (listView != null)
                        swipeRefreshLayout.setRefreshing(false);
                }
            }
        }
    };

    public int getCategoryPos(Spannable category) {
        return mDeviceList.indexOf(category);
    }

    public void getMacBle() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                String Mac = item.toString();
                intent.putExtra("Mac", Mac);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
