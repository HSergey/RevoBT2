package com.revolife.revobt2.revobt;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class SplashActivity extends AppCompatActivity {
    final String LOG_TAG = "myLogs";
    final int TASK1_CODE = 1;

    public final static int STATUS_START = 100;
    public final static int STATUS_FINISH = 203;
    public final static int STATUS_LOADING = 202;
    public final static String PARAM_PINTENT = "pendingIntent";
    public final static String PARAM_RESULT = "result";

    private static ProgressBar LaunchPb1;
    private static TextView LaunchTv1;
    private static TextView LaunchTv2;
    static String ShowDeviceCaption="";

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning=false;
    private Handler mHandler;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private static final int REQUEST_ENABLE_BT = 1;

    public static String EXTRAS_DEVICE_NAME = "";
    public static String EXTRAS_DEVICE_ADDRESS = "";
    private static boolean DeviceFound = false;
    private static boolean DictionaryLoaded = false;


    private ArrayList<BluetoothDevice> mLeDevices;

   // private LeDeviceListAdapter mLeDeviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

        LaunchPb1 = (ProgressBar)findViewById(R.id.progressBar_splash_loading);
        LaunchPb1.setProgress(0);

        LaunchTv1 = (TextView) findViewById(R.id.textView2);
        LaunchTv2 = (TextView) findViewById(R.id.tv_loadingText);

        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        PendingIntent pi;
        // Создаем PendingIntent для Task1
        pi = createPendingResult(TASK1_CODE, new Intent(), 0);
        // Создаем Intent для вызова сервиса, кладем туда
        // созданный PendingIntent
        Intent intent1 = new Intent(this, T9.class).putExtra(PARAM_PINTENT, pi);
        startService(intent1);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (DictionaryLoaded) {
            scanLeDevice(false);
            mLeDevices.clear();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if ((!DeviceFound)&&(DictionaryLoaded)){
            mLeDevices = new ArrayList<>();

            // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
            // fire an intent to display a dialog asking the user to grant permission to enable it.
            if (!mBluetoothAdapter.isEnabled()) {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }

            //// Initializes list view adapter.
            //mLeDeviceListAdapter = new LeDeviceListAdapter();
            //setListAdapter(mLeDeviceListAdapter);
            scanLeDevice(true);
        }

    }


    private void scanLeDevice(final boolean enable) {
        Log.d(LOG_TAG, "scanLeDevice: enable="+Boolean.toString(enable) + " mScanning="+Boolean.toString(mScanning));

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    Log.d(LOG_TAG, "scanLeDevice: postDelayed; public void run(); mScanning="+Boolean.toString(mScanning));

                    // ЗАГЛУШКА ДЛЯ ТЕСТОВ!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    /*Log.d(LOG_TAG, "scanLeDevice: СРАБОТАЛА ЗАГЛУШКА ДЛЯ ТЕСТОВ! УСТРОЙСТВО НЕ НАЙДЕНО!");
                    DeviceFound = true;

                    EXTRAS_DEVICE_NAME=SampleGattAttributes.DevNameCmp1;
                    EXTRAS_DEVICE_ADDRESS=SampleGattAttributes.macAdrCmp1;
                    tryLoadMainActivity();*/
                    // КОНЕЦ ЗАГЛУШКИ

                }
            }, SCAN_PERIOD);

            if (!mScanning) {
                Log.d(LOG_TAG, "scanLeDevice: Вывод надписи о начале сканирования. mScanning="+Boolean.toString(mScanning));
                ShowDeviceCaption += getResources().getString(R.string.start_find_device) + "\r\n";
                LaunchTv2.setText(ShowDeviceCaption);
            }
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "onActivityResult: requestCode = " + requestCode + ", resultCode = "
                + resultCode);

        // Ловим сообщения о старте задач
        if (resultCode == STATUS_START) {
            String str1 = data.getStringExtra(PARAM_RESULT);
            LaunchTv1.setText(str1);
        }

        // Ловим сообщения об процессе загрузки словаря
        if (resultCode == STATUS_LOADING) {
            int progr1 = data.getIntExtra(PARAM_RESULT, 0);
            LaunchPb1.setProgress(progr1);
        }

        // Ловим сообщения об окончании загрузки словаря
        if (resultCode == STATUS_FINISH) {
            Log.d(LOG_TAG, "onActivityResult: requestCode = " + requestCode + ", resultCode = "
                    + resultCode + "Ответ о загрузке словаря получен");
            //int progr1 = data.getIntExtra(PARAM_RESULT, 0);
            LaunchPb1.setProgress(100);
            LaunchTv1.setText(getResources().getString(R.string.text_splash_loaded)+"\r\n"+getResources().getString(R.string.start_find_device));
            DictionaryLoaded = true;
            tryLoadMainActivity();
        }
    }

    private void tryLoadMainActivity(){
        Log.d(LOG_TAG, "tryLoadMainActivity: Попытка запуска MainActivity с параметрами: DictionaryLoaded="+Boolean.toString(DictionaryLoaded) + " DeviceFound="+Boolean.toString(DeviceFound)+
        " EXTRAS_DEVICE_NAME=" + EXTRAS_DEVICE_NAME + " EXTRAS_DEVICE_ADDRESS=" + EXTRAS_DEVICE_ADDRESS);



        if ((DictionaryLoaded)&&(DeviceFound)) {
            Intent intent1 = new Intent(this, MainActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // очищаем историю чтобы новая активити могла нормально закрываться
            intent1.putExtra(MainActivity.EXTRAS_DEVICE_NAME, EXTRAS_DEVICE_NAME);              // прокидываем имя
            intent1.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, EXTRAS_DEVICE_ADDRESS);        // и адрес устройства для подключения
            startActivity(intent1);
        }
    }



    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {


                            // проверка на уже найденое устройство
                            if(!mLeDevices.contains(device)) {
                                mLeDevices.add(device);

                                String DeviceCaption;
                                final String deviceName = device.getName();
                                if (deviceName != null && deviceName.length() > 0)
                                    DeviceCaption = deviceName;
                                else
                                    DeviceCaption = getResources().getString(R.string.unknown_device);
                                DeviceCaption += " ("+device.getAddress()+")";
                                ShowDeviceCaption += DeviceCaption +")\r\n";

                                Log.d(LOG_TAG, "onLeScan: Найдено новое BLE устройство: "+DeviceCaption);

                                LaunchTv2.setText(ShowDeviceCaption);

                                final String macAdr = device.getAddress();
                                // Compare devices mac adress and connect if correct it
                                if (macAdr.equals(SampleGattAttributes.macAdrCmp1)) {
                                    Log.d(LOG_TAG, "onLeScan: Найдено НУЖНОЕ BLE устройство: "+DeviceCaption);

                                    EXTRAS_DEVICE_NAME = device.getName();
                                    EXTRAS_DEVICE_ADDRESS = device.getAddress();

                                    if (mScanning) {
                                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                                        mScanning = false;
                                    }

                                    DeviceFound = true;

                                    tryLoadMainActivity();
                                }
                            }
                        }
                    });
                }
            };

}
