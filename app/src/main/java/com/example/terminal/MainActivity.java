package com.example.terminal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import static android.graphics.Color.RED;

public class MainActivity extends Activity {
    EditText etText;
    Button btnSend;
    Button button_clear_serial;
    TextView tvSent;
    ProgressDialog progress;
    ScrollView scrollView;
    BluetoothAdapter myBluetooth = null;
    BluetoothManager bluetoothManager;
    BluetoothSocket btSocket = null;
    boolean isBtConnected = false;
    String address = "F0:08:D1:5F:F0:36";
    UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String s;
    byte[] buffer = new byte[256];
    int Byte;
    InputStream inputStream = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //view of the LightControl
        setContentView(R.layout.activity_main);

        //call the widgets
        etText = (EditText) findViewById(R.id.editText);
        btnSend = (Button) findViewById(R.id.buttonSend);
        button_clear_serial = (Button) findViewById(R.id.button_clear_serial);
        tvSent = findViewById(R.id.tvMessageSent);
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        myBluetooth = bluetoothManager.getAdapter();
        scrollView = ((ScrollView) findViewById(R.id.scrollView_terminal));
        Set<BluetoothDevice> devices = myBluetooth.getBondedDevices();
        for (BluetoothDevice device : devices) {
            tvSent.append("\nDevice: " + device.getName() + ", " + device);
        }
        //Disconnect();
//        if (myBluetooth == null || !myBluetooth.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, 1);
//        }
        Clear_Serial();
        new MainActivity.ConnectBT().execute(); //Call the class to connect
    }

    public void Clear_Serial() {
        button_clear_serial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvSent.setText("");
            }
        });
    }

    class Send_Data implements Runnable {
        @Override
        public void run() {
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    s = etText.getText().toString();
//                    s.concat("\n");
                    try {
                        btSocket.getOutputStream().write(etText.getText().toString().getBytes());
                        btSocket.getOutputStream().flush();
                        etText.setText("");
                    } catch (IOException e) {
                        msg("Error");
                    }
                    tvSent.append("\nTransmit:   " + s);
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                }
            });
        }
    }

    class Receive_Data implements Runnable {
        @SuppressWarnings("deprecation")
        @Override
        public void run() {
            while (true) {
                try {
                    inputStream = btSocket.getInputStream();
                    inputStream.skip(inputStream.available());
                    Byte = inputStream.read(buffer);
                    String readMessage = new String(buffer, 0, Byte);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            tvSent.append("Receive data: " + readMessage + "\n");
                            String readStringBuf = "Receive:   " + readMessage;
                            Spannable spannable = new SpannableString(readStringBuf);
                            spannable.setSpan(new ForegroundColorSpan(Color.rgb(0, 150, 200)), 0, readStringBuf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            tvSent.append(spannable);
                            scrollView.post(new Runnable() {
                                @Override
                                public void run() {
                                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                }
                            });
                        }
                    });

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        Disconnect();
        super.onDestroy();
    }

    private void Disconnect() {
        if (btSocket != null) //If the btSocket is busy
        {
            try {
                btSocket.close(); //close connection
            } catch (IOException e) {
                msg("Error");
            }
        }
        finish(); //return to the first layout

    }

    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @SuppressLint("StaticFieldLeak")
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                    new Thread(new Send_Data()).start();
                    new Thread(new Receive_Data()).start();
                    Log.d("Finish", "doInBackground: ");
                }

            } catch (IOException e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

    @Override
    protected void onPause() {
        progress.dismiss();
        super.onPause();
    }

    @Override
    protected void onStop() {
        progress.dismiss();
        super.onStop();
    }
}
