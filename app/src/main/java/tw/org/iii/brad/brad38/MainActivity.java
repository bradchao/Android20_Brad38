package tw.org.iii.brad.brad38;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.model.BleGattCharacter;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;

import java.util.List;
import java.util.UUID;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

public class MainActivity extends AppCompatActivity {
    private BluetoothClient mClient;
    private final BluetoothStateListener mBluetoothStateListener = new BluetoothStateListener() {
        @Override
        public void onBluetoothStateChanged(boolean openOrClosed) {
            Log.v("brad", openOrClosed?"Open":"Close");
        }

    };

    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {

        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status == STATUS_CONNECTED) {
                Log.v("brad", "connected");
                test4(null);
            } else if (status == STATUS_DISCONNECTED) {
                Log.v("brad", "disconnected");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    123);
        }else{
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        init();
    }

    private void init(){
        mClient = new BluetoothClient(this);
        mClient.registerBluetoothStateListener(mBluetoothStateListener);
        mClient.openBluetooth();
    }

    public void test1(View view) {
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
                .searchBluetoothLeDevice(2000)      // 再扫BLE设备2s
                .build();

        mClient.search(request, new MySearchListener() );
    }

    public void test2(View view) {
        mClient.stopSearch();
    }

    public void test3(View view) {
        Log.v("brad", "connecting...");

        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)   // 连接如果失败重试3次
                .setConnectTimeout(30000)   // 连接超时30s
                .setServiceDiscoverRetry(3)  // 发现服务如果失败重试3次
                .setServiceDiscoverTimeout(20000)  // 发现服务超时20s
                .build();

        mClient.connect(connectMAC, options, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile profile) {
                if (code == REQUEST_SUCCESS) {
                    Log.v("brad", "respone success");

                    List<BleGattService> services = profile.getServices();
                    for (BleGattService service : services){
                        String uuid = service.getUUID().toString();
                        //Log.v("brad", "service: " + uuid);

                        List<BleGattCharacter> cs = service.getCharacters();
                        for (BleGattCharacter c : cs){
                            //Log.v("brad", "c: " + c);
                        }

                    }

                }
            }
        });
    }

    private String connectMAC = null;

    private UUID serviceUUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    private UUID characterUUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    public void test4(View view) {
        Log.v("brad", connectMAC);
        Log.v("brad", serviceUUID.toString());
        Log.v("brad", characterUUID.toString());
        mClient.refreshCache(connectMAC);
        mClient.notify(connectMAC, serviceUUID, characterUUID, new BleNotifyResponse() {

            @Override
            public void onNotify(UUID service, UUID character, byte[] value) {
                Log.v("brad", "notify");
                for (int v : value){
                    Log.v("brad", "value = " + v);
                }
            }

            @Override
            public void onResponse(int code) {
                if (code == REQUEST_SUCCESS) {
                    Log.v("brad", "success");
                }else{
                    Log.v("brad", "not success");
                }
            }
        });
    }

    private class MySearchListener implements SearchResponse {

        @Override
        public void onSearchStarted() {

        }

        @Override
        public void onDeviceFounded(SearchResult result) {
            BluetoothDevice device = result.device;
            String name = result.getName();
            String mac = result.getAddress();
            //Log.v("brad", name + ":" + mac);

            // BradAsus / Pixel 4 XL
            if (name.equals("BradAsus")){
                Log.v("brad", "i got it");
                connectMAC = mac;
                mClient.stopSearch();
                mClient.registerConnectStatusListener(connectMAC, mBleConnectStatusListener);
            }

        }

        @Override
        public void onSearchStopped() {

        }

        @Override
        public void onSearchCanceled() {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mClient.disconnect(connectMAC);
        mClient.closeBluetooth();
        mClient.unregisterBluetoothStateListener(mBluetoothStateListener);
        mClient.unregisterConnectStatusListener(connectMAC, mBleConnectStatusListener);
    }
}
