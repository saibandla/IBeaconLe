package com.example.bhargavbandla.lebeacon;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.LogRecord;


public class MainActivity extends ActionBarActivity {
    TextView deviceName;
    TextView deviceuuid;
    TextView deviceAddress;
    TextView major;
    TextView minor;
    TextView distance;
    TextView distanceE;
    int rssi;
    BluetoothAdapter bluetoothAdapter;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        deviceName = (TextView) findViewById(R.id.deviceName);
        deviceuuid = (TextView) findViewById(R.id.uuid);
        deviceAddress = (TextView) findViewById(R.id.deviceAddress);
        distance = (TextView) findViewById(R.id.distance);
        major = (TextView) findViewById(R.id.major);
        minor = (TextView) findViewById(R.id.minor);
        distanceE = (TextView) findViewById(R.id.distanceE);
        handler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getBaseContext(), "You Device Doesn't Support BLE", Toast.LENGTH_LONG).show();
            finish();
        }
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if ((bluetoothAdapter == null)) {
            Toast.makeText(getBaseContext(), "You Device Doesn't Support BlueTooth", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!bluetoothAdapter.isEnabled()) {
            Intent bluetoothRequestIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(bluetoothRequestIntent, 1);
        } else {
            scanForaBluetoothDevice();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
    }

    private void noNetworkAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Network Available")
                .setMessage("Do you want to turn on Wifi Setting")
                .setCancelable(false)
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getBaseContext(), "You may not get Promotional messages from the Provider", Toast.LENGTH_LONG).show();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean isNextworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
        for (int i = 0; i < networkInfos.length; i++) {
            NetworkInfo networkInfo = networkInfos[i];
            if (networkInfo.isConnected()) {
                return true;
            }
        }
        return false;
    }

    ProgressDialog progressDialog;
    public void scanForaBluetoothDevice() {

        progressDialog = ProgressDialog.show(MainActivity.this, "Scanning for Devices", "", false, false);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bluetoothAdapter.stopLeScan(scanCallback);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });

            }
        }, 10000);
        bluetoothAdapter.startLeScan(scanCallback);

    }

    BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {


            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    deviceName.setText("Device Name: " + device.getName());
                    deviceAddress.setText("Device Address " + device.getAddress());
                    ParseData(scanRecord, rssi);
                }
            });
        }
    };
    /*
                d6 be 89 8e 40 24 05 a2 17 6e 3d 71 02 01 1a 1a ff 4c 00 02 15 e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e0 00 00 00 00 c5 52 ab 8d 38 a5

                d6 be 89 8e # Access address for advertising data (this is always the same fixed value)
                40 # Advertising Channel PDU Header byte 0.  Contains: (type = 0), (tx add = 1), (rx add = 0)
                24 # Advertising Channel PDU Header byte 1.  Contains:  (length = total bytes of the advertising payload + 6 bytes for the BLE mac address.)
                05 a2 17 6e 3d 71 # Bluetooth Mac address (note this is a spoofed address)
                02 01 1a 1a ff 4c 00 02 15 e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e0 00 00 00 00 c5 # Bluetooth advertisement
                52 ab 8d 38 a5 # checksum


                02 # Number of bytes that follow in first AD structure
                01 # Flags AD type
                1A # Flags value 0x1A = 000011010
                   bit 0 (OFF) LE Limited Discoverable Mode
                   bit 1 (ON) LE General Discoverable Mode
                   bit 2 (OFF) BR/EDR Not Supported
                   bit 3 (ON) Simultaneous LE and BR/EDR to Same Device Capable (controller)
                   bit 4 (ON) Simultaneous LE and BR/EDR to Same Device Capable (Host)
                1A # Number of bytes that follow in second (and last) AD structure
                FF # Manufacturer specific data AD type
                4C 00 # Company identifier code (0x004C == Apple)
                02 # Byte 0 of iBeacon advertisement indicator
                15 # Byte 1 of iBeacon advertisement indicator
                e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e0 # iBeacon proximity uuid
                00 00 # major
                00 00 # minor
                c5 # Tx Power
            */

    private void ParseData(byte[] scanRecord, int rssi) {
        int startByte = 2;
        boolean patternFound = false;
        Log.d("Data Found", bytesToHex(scanRecord));
        while (startByte <= 5) {
            if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                    ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                patternFound = true;
                break;
            }
            startByte++;
        }

        if (patternFound) {
            bluetoothAdapter.stopLeScan(scanCallback);
            //Convert UUID to hex String
            byte[] uuidBytes = new byte[16];

            System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);

            String hexString = bytesToHex(uuidBytes);

            //UUID
            String uuid = hexString.substring(0, 8) + "-" +
                    hexString.substring(8, 12) + "-" +
                    hexString.substring(12, 16) + "-" +
                    hexString.substring(16, 20) + "-" +
                    hexString.substring(20, 32);
            deviceuuid.setText("UUID :" + uuid);

            // Major value
            int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);

            //Minor value
            int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);

            //Tx Power
            int strength = ((scanRecord[startByte + 24]));

            this.major.setText(String.format("Major :%d", major));
            this.minor.setText(String.format("Minor :%d", minor));

            /*
            RSSI = TxPower - 10 * n * lg(distance)
            distance= 10 ^ ((TxPower - RSSI) / (10 * n))
             */

            //Distance
            double dis = Math.pow(10d, ((double) strength - rssi) / (10 * 2));
            distance.setText(String.format("Distance :%.2f m", dis));
            String proximity = "";
            if (dis <= 0.8) {
                proximity = "Immediate";
            } else if (dis <= 8.0) {
                proximity = "Near";
            } else if (dis > 8.0) {
                proximity = "Far";
            }
            distanceE.setText("Proximty Range :" + proximity);
            progressDialog.dismiss();

            if (proximity != "") {
                if (isNextworkAvailable()) {
                    String url = "http://www.nivansys.com/iBeacon.php?proximity=" + proximity;
                    RestAPICalls restAPICalls = new RestAPICalls();
                    restAPICalls.execute(url);
                } else {
                    noNetworkAlertDialog();
                }
            }
            Log.d("Parse iBeacon  Data", String.format("Found Proxity UUID %s, major-%d, minor-%d Txpower-%f", uuid, major, minor, dis));
        } else {
            Log.d("Parse iBeacon  Data", "Unable to Parse iBeacon");
        }
    }

    private class RestAPICalls extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            JSONParser jsonParser = new JSONParser();
            return jsonParser.excecuteGetTypeResquestFromUrl(params[0]);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            Log.d("Tag", jsonObject.toString());
            try {
                if (!jsonObject.getBoolean("error")) ;
                {
                    notifyUser(jsonObject.getString("message"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyUser(String message) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(android.R.drawable.stat_notify_chat)
                        .setContentTitle("Ibeacon")
                        .setContentText(message);

        Intent intent = new Intent(getBaseContext(), ReadNotificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("NotificationMsg", message);

        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        mBuilder.setAutoCancel(true);

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        Log.d("Notify", "Notified");
        notificationManager.notify((int) (Math.random() * 100), mBuilder.build());
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
    }

    static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_refresh) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent bluetoothRequestIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(bluetoothRequestIntent, 1);
            } else
                scanForaBluetoothDevice();
        }
        return super.onOptionsItemSelected(item);
    }
}
