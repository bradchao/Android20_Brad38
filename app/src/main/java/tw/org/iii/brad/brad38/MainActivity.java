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
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;

public class MainActivity extends AppCompatActivity {
    private BluetoothClient mClient;
    private final BluetoothStateListener mBluetoothStateListener = new BluetoothStateListener() {
        @Override
        public void onBluetoothStateChanged(boolean openOrClosed) {
            Log.v("brad", openOrClosed?"Open":"Close");
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
        mClient.connect(connectMAC, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile profile) {
                if (code == REQUEST_SUCCESS) {
                    Log.v("brad", "connected");
                }
            }
        });
    }

    private String connectMAC = null;
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

            if (name.equals("BradAsus")){
                Log.v("brad", "i got it");
                connectMAC = mac;
                mClient.stopSearch();
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
        mClient.closeBluetooth();
        mClient.unregisterBluetoothStateListener(mBluetoothStateListener);
    }
}
