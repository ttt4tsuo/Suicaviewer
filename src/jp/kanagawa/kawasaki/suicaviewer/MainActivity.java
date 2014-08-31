package jp.kanagawa.kawasaki.suicaviewer;

//マネージャー、レシーバー等の設定関連
//http://developer.android.com/reference/android/nfc/package-summary.html
//実装(write、メッセージセンド等)
//http://developer.android.com/reference/android/nfc/tech/package-summary.html

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	private RequestQueue mQueue;
	private String recdata;
	private StationCodedata stCodedata;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //https://developer.android.com/training/volley/simple.html
        mQueue = Volley.newRequestQueue(this);

        
        Intent intent = getIntent();        
        Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        
        try{
        	NfcF nfc_f = NfcF.get(tag);
            byte[] felicaIDm = tag.getId();
    		nfc_f.connect();
    		byte[] req = readWithoutEncryption(felicaIDm, 10);
    		byte[] res = nfc_f.transceive(req);
    		TextView textView = (TextView)findViewById(R.id.textview);
            textView.setText(parse(res));
            //ネットワークステータス処理
            ConnectivityManager connmanager = (ConnectivityManager)this.getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo info = connmanager.getActiveNetworkInfo();
            if (info == null || info.isConnected() != true) {
            	Toast.makeText(this, "Network Not Connected!", Toast.LENGTH_LONG).show();
            }else{
            	searchStationCode();
            }
    		nfc_f.close();
    	}catch(Exception e){

    	}
    }
    
    private byte[] readWithoutEncryption(byte[] idm, int size) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(100);

        bout.write(0);           // データ長(データリンク層)
        bout.write(0x06);        // コマンドコード「Read Without Encryption」
        bout.write(idm);         // 製造ID(IDm) [製造者コード]+[カード識別番号] 8byte
        bout.write(1);           // サービス数(サービスコードリストの数)
        bout.write(0x0f);        // サービスコードリストその1(サービスコード下位バイト)
        bout.write(0x09);        // サービスコードリストその1(サービスコード上位バイト)
        bout.write(size);        // ブロック数
        for (int i = 0; i < size; i++) {//ブロックリスト
            bout.write(0x80);    // ブロックリストエレメント(1byte目) bit表現 1000 0000
                                 // ブロックリストエレメントは2bytesで表現
                                 // パースサービスへのキャッシュバックアクセス以外のブロックへの読み書きを行う
            bout.write(i);       // ブロックリストエレメント(1byte目) ブロック番号を指定
        }
        byte[] msg = bout.toByteArray();
        //データリンク層の[データ長]+[パケットデータ]を作成。transceiveメソッドで[CRC]は自動で付加される
        msg[0] = (byte) msg.length; // 先頭１バイトはデータ長
        return msg;
    }
    private String parse(byte[] res) throws Exception {
    	//////////////////////////////////////////////////////////////
        // 0バイト目    = データ長(データリンク層)
        // 1バイト目    = レスポンスコード(0x07)
        // 2-9バイト目  = 製造ID(IDm) [製造者コード]+[カード識別番号] 8byte
        // 10バイト目   = ステータスフラグ1。0=正常。
    	// 11バイト目   = ステータスフラグ2。0=正常。
        // 12バイト目   = ブロック数
        // 13バイト目   = ブロックデータ。16byte。この後はブロックデータの繰り返し。
    	//////////////////////////////////////////////////////////////
    	
        if (res[10] != 0x00 || res[11] != 0x00){
        	return "Read Status Error";
        };
        int size = res[12];
        String str = "";
        for (int i = 0; i < size; i++) {
        	Blockdata block = Blockdata.parse(res, 13 + i * 16);
            str += block.toString() +"\n";
        }
        return str;
    }
    
    private void searchStationCode(){
    	final CountDownLatch latch = new CountDownLatch(1);
    	
    	new Thread(new Runnable() {
            public void run() {
                try{
                	String accessurl = "http://mysterious-reaches-6275.herokuapp.com/services/timez/db?linecode=1&stationcode=6";
                	mQueue.add(new StationRequest<String>(accessurl,
                			new Response.Listener<String>() {
                				@Override
                				public void onResponse(String response) {
                					Log.d("myAppp","ok"); 
                					recdata=response;
                					latch.countDown();
                				}
                			},
                			new Response.ErrorListener() {
                				@Override
                				public void onErrorResponse(VolleyError error) {
                					Log.d("myAppp","volley error");
                					Log.d("myAppp",error.networkResponse.toString());
                					latch.countDown();
                				}
                			}));
                }catch (Exception e){
                	Log.d("myAppp","thread error");
                }
            }
    	}).start();
    	
    	new Thread(new Runnable(){
            public void run() {
                try{
                	latch.await();
                	Gson gson = new Gson();
                	StationCodedata stationcodedata = gson.fromJson(recdata, StationCodedata.class);
                	stCodedata=stationcodedata;
                }catch (Exception e){
                	Log.d("myAppp gson",e.toString());
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                    	TextView textView = (TextView) findViewById(R.id.textview2);
                    	textView.setText(stCodedata.getLineName());
                    }
            	});
            }
    	}).start();
    }
}


