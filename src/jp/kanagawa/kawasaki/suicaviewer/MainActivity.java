package jp.kanagawa.kawasaki.suicaviewer;

//マネージャー、レシーバー等の設定関連
//http://developer.android.com/reference/android/nfc/package-summary.html
//実装(write、メッセージセンド等)
//http://developer.android.com/reference/android/nfc/tech/package-summary.html

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

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
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	private RequestQueue mQueue;
	private String recdata4in;
	private String recdata4out;
	volatile private StationCodedata stCodedata4in;
	volatile private StationCodedata stCodedata4out;
	volatile private ArrayList<Blockdata> block;
	
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
    		parse(res);
    		nfc_f.close();
            //ネットワークステータス処理
            ConnectivityManager connmanager = (ConnectivityManager)this.getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo info = connmanager.getActiveNetworkInfo();
            if (info == null || info.isConnected() != true) {
            	Toast.makeText(this, "Network Not Connected!", Toast.LENGTH_LONG).show();
            }else{
            	//for(int i=0;i<block.size();i++){
            	for(int i=0;i<2;i++){
            		searchStationCode(i);
            	}
            }
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
    private void parse(byte[] res) throws Exception {
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
        	return;
        };
        int size = res[12];
        
        NfcRecordAdapter nfcRecordAdpter = new NfcRecordAdapter();
        block = new ArrayList<Blockdata>();

        for (int i = 0; i < size; i++) {
        	block.add(Blockdata.parse(res, 13 + i * 16));
        }
        ListView listView1 = (ListView)findViewById(R.id.nfc_record_listview);
        listView1.setAdapter(nfcRecordAdpter);

    }
    
    private void searchStationCode(final int listnum){
    	try{
    		if(block.get(listnum).getResolvedInLine()!=null || block.get(listnum).getResolvedInStation()!=null){
        		return;
        	}
        	String accessurl = "http://mysterious-reaches-6275.herokuapp.com/services/timez/db?"
                						+"linecode="+block.get(listnum).getInLine()
                						+"&stationcode="+block.get(listnum).getInStation();
        	mQueue.add(new StationRequest<String>(accessurl, new Response.Listener<String>() {
            	@Override
            	public void onResponse(String response) {
            		recdata4in=response;
            		Gson gson = new Gson();
            		stCodedata4in = gson.fromJson(recdata4in, StationCodedata.class);
            		block.get(listnum).setResolvedInLine(stCodedata4in.getLineName());
            		block.get(listnum).setResolvedInStation(stCodedata4in.getStationName());
            		
            		//inStationとoutStationのキューが同時に動くので必要
            		ListView listView1 = (ListView)findViewById(R.id.nfc_record_listview);
            		View targetView = listView1.getChildAt(listnum);
            		listView1.getAdapter().getView(listnum, targetView, listView1);
            	}
            },
            new Response.ErrorListener() {
            	@Override
            	public void onErrorResponse(VolleyError error) {
            		Log.d("myapp","volley error StationIn:"+error.getMessage());
            		Log.d("myapp","listnum;"+Integer.toString(listnum));
            	}
            }));
            
        	if(block.get(listnum).getResolvedOutLine()!=null || block.get(listnum).getResolvedOutStation()!=null){
        		return;
        	}
            accessurl = "http://mysterious-reaches-6275.herokuapp.com/services/timez/db?"
    						+"linecode="+block.get(listnum).getOutLine()
    						+"&stationcode="+block.get(listnum).getOutStation();
            mQueue.add(new StationRequest<String>(accessurl, new Response.Listener<String>() {
            	@Override
            	public void onResponse(String response) {
            		recdata4out=response;
            		Gson gson = new Gson();
            		stCodedata4out = gson.fromJson(recdata4out, StationCodedata.class);
            		block.get(listnum).setResolvedOutLine(stCodedata4out.getLineName());
            		block.get(listnum).setResolvedOutStation(stCodedata4out.getStationName());

            		ListView listView1 = (ListView)findViewById(R.id.nfc_record_listview);
            		View targetView = listView1.getChildAt(listnum);
            		listView1.getAdapter().getView(listnum, targetView, listView1);
            	}
            },
            new Response.ErrorListener() {
            	@Override
            	public void onErrorResponse(VolleyError error) {
            		Log.d("myapp","volley error StationOut"+error.getMessage());
            	}
            }));
            mQueue.start();
            
    	}catch (Exception e){
    		Log.d("myapp","thread error");
        }
                
    }
    
    private class NfcRecordAdapter extends BaseAdapter {
    	@Override
        public int getCount() {
        	return block.size();
        }

        @Override
        public Object getItem(int position) {
        	return block.get(position);
        }

        @Override
        public long getItemId(int position) {
        	return position;
        }

        @Override
        public View getView(int position,View convertView,ViewGroup parent) {
        	TextView mTextView_seq_num;
        	TextView mTextView_day;
        	TextView mTextView_remain;
        	
        	TextView mTextView_devname;
        	TextView mTextView_actname;
        	//TextView mTextView_cost;
        	
        	TextView mTextView_inline;
        	TextView mTextView_instation;
        	TextView mTextView_outline;
        	TextView mTextView_outstation;
        	
        	View v = convertView;
        	if(v==null){
        		LayoutInflater inflater =  (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        		v = inflater.inflate(R.layout.nfc_record, (ViewGroup)null);
        	}
        	Blockdata block = (Blockdata)getItem(position);
        	if(block != null){
        		mTextView_seq_num = (TextView) v.findViewById(R.id.seq_num);
        		mTextView_day = (TextView) v.findViewById(R.id.day);
        		mTextView_remain = (TextView) v.findViewById(R.id.remain);
        		mTextView_devname = (TextView) v.findViewById(R.id.devname);
        		mTextView_actname = (TextView) v.findViewById(R.id.actname);
        		mTextView_inline = (TextView) v.findViewById(R.id.inline);
        		mTextView_instation = (TextView) v.findViewById(R.id.instation);
        		mTextView_outline = (TextView) v.findViewById(R.id.outline);
        		mTextView_outstation = (TextView) v.findViewById(R.id.outstation);
        		
        		mTextView_seq_num.setText(block.getSequenceNum());
        		mTextView_day.setText(block.getDay());
        		mTextView_remain.setText("残高:"+block.getRemain()+"円");
        		mTextView_devname.setText(block.getDevName());
        		mTextView_actname.setText(block.getActName());
        		if(block.getResolvedInLine()!=null){
        			mTextView_inline.setText(block.getResolvedInLine());
        		}else{
        			mTextView_inline.setText(block.getInLine());
        		}
        		if(block.getResolvedInStation()!=null){
        			mTextView_instation.setText(block.getResolvedInStation());
        		}else{
        			mTextView_instation.setText(block.getInStation());
        		}
        		if(block.getResolvedOutLine()!=null){
        			mTextView_outline.setText(block.getResolvedOutLine());
        		}else{
        			mTextView_outline.setText(block.getOutLine());
        		}
        		if(block.getResolvedOutStation()!=null){
        			mTextView_outstation.setText(block.getResolvedOutStation());
        		}else{
        			mTextView_outstation.setText(block.getOutStation());
        		}
        	}
        	return v;
        }
        
    }
}


