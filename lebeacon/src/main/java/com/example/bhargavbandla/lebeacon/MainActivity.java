package com.example.bhargavbandla.lebeacon;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {
    TextView deviceName;
    TextView deviceuuid;
    TextView deviceAddress;
    TextView major;
    TextView minor;
    TextView distance;
    int rssi;
    BluetoothAdapter bluetoothAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        deviceName=(TextView)findViewById(R.id.deviceName);
        deviceuuid=(TextView)findViewById(R.id.uuid);
        deviceAddress=(TextView)findViewById(R.id.deviceAddress);
        distance=(TextView)findViewById(R.id.distance);
        major=(TextView)findViewById(R.id.major);
        minor=(TextView)findViewById(R.id.minor);
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(getBaseContext(),"You Device Doesn't Support BLE",Toast.LENGTH_LONG).show();
            finish();
        }
        BluetoothManager bluetoothManager=(BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter=bluetoothManager.getAdapter();
        if((bluetoothManager.getAdapter()==null))
        {
            Toast.makeText(getBaseContext(),"You Device Doesn't Support BlueTooth",Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!bluetoothAdapter.isEnabled())
        {
            Intent bluetoothRequestIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivityForResult(bluetoothRequestIntent,1);
        }
        scanForaBluetoothDevice();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1&&resultCode== Activity.RESULT_CANCELED)
        {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public void scanForaBluetoothDevice()
    {
        bluetoothAdapter.startLeScan(scanCallback);
    }
    BluetoothAdapter.LeScanCallback scanCallback=new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    deviceName.setText("Device Name: " + device.getName());
                    deviceAddress.setText("Device Address "+device.getAddress());
                    ParseData(scanRecord,rssi);
                    bluetoothAdapter.stopLeScan(scanCallback);
                }
            });
        }
    };
    private void ParseData(byte[] scanRecord,int rssi)
    {
        int startByte = 2;
        boolean patternFound = false;
        Log.d("Data Found", bytesToHex(scanRecord));
        while (startByte <= 5) {
            if (    ((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                    ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                patternFound = true;
                break;
            }
            startByte++;
        }

        if (patternFound) {
            //Convert to hex String
            byte[] uuidBytes = new byte[16];
            System.arraycopy(scanRecord, startByte+4, uuidBytes, 0, 16);
            String hexString = bytesToHex(uuidBytes);

            //Here is your UUID
            String uuid =  hexString.substring(0,8) + "-" +
                    hexString.substring(8,12) + "-" +
                    hexString.substring(12,16) + "-" +
                    hexString.substring(16,20) + "-" +
                    hexString.substring(20,32);

            // Major value
            deviceuuid.setText("UUID :"+uuid);
            int major = (scanRecord[startByte+20] & 0xff) * 0x100 + (scanRecord[startByte+21] & 0xff);

            //Minor value
            int minor = (scanRecord[startByte+22] & 0xff) * 0x100 + (scanRecord[startByte+23] & 0xff);
            //Tx Power
            int strength = ((scanRecord[startByte+24]))  ;

            this.major.setText(String.format("Major :%d",major));
            this.minor.setText(String.format("Minor :%d",minor));

            /*
            RSSI = TxPower - 10 * n * lg(distance)
            distance= 10 ^ ((TxPower - RSSI) / (10 * n))
             */
            //Distance
           double dis= Math.pow(10d, ((double) strength - rssi) / (10 * 2));
            distance.setText(String.format("Distance :%.2f",dis));
            Log.d("Parse iBeacon  Data",String.format("Found Proxity UUID %s, major-%d, minor-%d Txpower-%f",uuid,major,minor,dis));


        }
        else
        {
            Log.d("Parse iBeacon  Data","Unable to Parse iBeacon");
        }

    }
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id==R.id.action_refresh)
        {
            if(!bluetoothAdapter.isEnabled())
            {
                Intent bluetoothRequestIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(bluetoothRequestIntent,1);
            }
            scanForaBluetoothDevice();
        }

        return super.onOptionsItemSelected(item);
    }
}
