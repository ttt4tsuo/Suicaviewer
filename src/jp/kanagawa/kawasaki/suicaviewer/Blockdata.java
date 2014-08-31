package jp.kanagawa.kawasaki.suicaviewer;

import java.util.concurrent.CountDownLatch;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import android.app.Activity;
import android.content.Context;
import android.util.SparseArray;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * http://sourceforge.jp/projects/felicalib/wiki/suica
 * http://www.kotemaru.org/2013/10/20/android-pasmo.html
 * http://www.denno.net/SFCardFan/webservice.html
 */
public class Blockdata{
	//16bytes
    private int dev_id;        //1byte�ｿｽ�ｿｽ �ｿｽ[�ｿｽ�ｿｽ�ｿｽ�ｿｽ
    private int act_id;        //2byte�ｿｽ�ｿｽ �ｿｽ�ｿｽ�ｿｽ�ｿｽ
                              //3byte�ｿｽ�ｿｽ ?
                              //4byte�ｿｽ�ｿｽ ?
    private int year;          //5-6 byte�ｿｽ�ｿｽ �ｿｽ�ｿｽ�ｿｽt(7bit�ｿｽN)
    private int month;         //5-6 byte�ｿｽ�ｿｽ �ｿｽ�ｿｽ�ｿｽt(4bit�ｿｽ�ｿｽ)
    private int day;           //5-6 byte�ｿｽ�ｿｽ �ｿｽ�ｿｽ�ｿｽt(5bit�ｿｽ�ｿｽ)
    private String in_line;    //7byte�ｿｽ�ｿｽ �ｿｽ�ｿｽ/�ｿｽ�ｿｽ�ｿｽ�ｿｽR�ｿｽ[�ｿｽh
    private String in_station; //8byte�ｿｽ�ｿｽ �ｿｽ�ｿｽ/�ｿｽw�ｿｽ�ｿｽ�ｿｽR�ｿｽ[�ｿｽh
    private String out_line;   //9byte�ｿｽ�ｿｽ �ｿｽo/�ｿｽ�ｿｽ�ｿｽ�ｿｽR�ｿｽ[�ｿｽh
    private String out_station;//10byte�ｿｽ�ｿｽ �ｿｽo/�ｿｽw�ｿｽ�ｿｽ�ｿｽR�ｿｽ[�ｿｽh
    private int remain;        //11-12byte�ｿｽ�ｿｽ �ｿｽc�ｿｽ�ｿｽ(�ｿｽ�ｿｽ�ｿｽg�ｿｽ�ｿｽ�ｿｽG�ｿｽ�ｿｽ�ｿｽf�ｿｽB�ｿｽA�ｿｽ�ｿｽ)
    private int seqNo;         //13-15byte�ｿｽ�ｿｽ �ｿｽA�ｿｽ�ｿｽ
    private int region;       //16byte�ｿｽ�ｿｽ �ｿｽ�ｿｽ�ｿｽ[�ｿｽW�ｿｽ�ｿｽ�ｿｽ�ｿｽ
    
    public Blockdata(){
    }

    public static Blockdata parse(byte[] res, int off) {
    	Blockdata self = new Blockdata();
        self.init(res, off);
        return self;
    }

    private void init(byte[] res, int off) {
    	
        this.dev_id = res[off+0];
        this.act_id = res[off+1];
        int mixInt = toInt(res, off, 4,5);
        this.year  = (mixInt >> 9) & 0x07f;
        this.month = (mixInt >> 5) & 0x00f;
        this.day   = mixInt & 0x01f;

        if (isBuppan(this.act_id)) {
            this.in_station = "物販";
        } else if (isBus(this.act_id)) {
            this.in_station = "バス";
        } else {
        	if(res[off+6] < 0x80){
        		this.in_line = "0 ";
        	}else if(res[off+15] == 0x00){
        		this.in_line = "1 ";
        	}else{
        		this.in_line = "2 ";
        	}
        	if(res[off+8] < 0x80){
        		this.out_line = "0 ";
        	}else if(res[off+15] == 0x00){
        		this.out_line = "1 ";
        	}else{
        		this.out_line = "2 ";
        	}
        }
        this.in_line += Integer.toHexString(0xff & res[off+6]);
        this.in_station = Integer.toHexString(0xff & res[off+7]);
        this.out_line += Integer.toHexString(0xff & res[off+8]);
        this.out_station = Integer.toHexString(0xff & res[off+9]);
        
        this.remain  = toInt(res, off, 11,10); //10-11: �ｿｽc�ｿｽ�ｿｽ (little endian)
        this.seqNo   = toInt(res, off, 12,13,14); //12-14: �ｿｽA�ｿｽ�ｿｽ
        this.region = res[off+15]; //15: �ｿｽ�ｿｽ�ｿｽ[�ｿｽW�ｿｽ�ｿｽ�ｿｽ�ｿｽ 
    }

    /*Util Func*/
    private int toInt(byte[] res, int off, int... idx) {
        int num = 0;
        for (int i=0; i<idx.length; i++) {
            num = num << 8;
            num += ((int)res[off+idx[i]]) & 0x0ff;
        }
        return num;
    }
    private boolean isBuppan(int act_id) {
        return act_id == 70 || act_id == 73 || act_id == 74 || act_id == 75 || act_id == 198 || act_id == 203;
    }
    private boolean isBus(int act_id) {
        return act_id == 13|| act_id == 15|| act_id ==  31|| act_id == 35;
    }

    public String toString() {
    	DeviceActCodedata daCode = new DeviceActCodedata();
        String str = seqNo
                +","+daCode.devmap.get(dev_id)
                +","+ daCode.actmap.get(act_id)
                +","+ in_line +" "+ in_station
                +","+ out_line +" "+ out_station
                +","+year+"/"+month+"/"+day
                +",残金："+remain+"円";
        return str;
    }
    
}