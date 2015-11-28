package jp.kanagawa.kawasaki.suicaviewer;

import java.util.Locale;

/**
 * http://sourceforge.jp/projects/felicalib/wiki/suica
 * http://www.kotemaru.org/2013/10/20/android-pasmo.html
 * http://www.denno.net/SFCardFan/webservice.html
 */
public class Blockdata{
	//http://sourceforge.jp/projects/felicalib/wiki/suicaからの情報
	//16bytes
    private int dev_id;        //1byte デバイス種別
    private int act_id;        //2byte 行動種別
                               //3byte 不明
                               //4byte 不明
    private int year;          //5-6 byte 年
    private int month;         //5-6 byte 月
    private int day;           //5-6 byte 日
    private String in_line;    //7byte 路線(入る)
    private String in_station; //8byte 駅名(入る)
    private String out_line;   //9byte 路線(出る)
    private String out_station;//10byte 駅名(出る)
    private int remain;        //11-12byte 残高
    private int seqNo;         //13-15byte シーケンス番号
    private int region;       //16byte リージョン?
    
    private String resolved_in_line=null;
    private String resolved_in_station=null;
    private String resolved_out_line=null;
    private String resolved_out_station=null;
    
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
        
        
        int ymdint = (res[off+4] & 0x000000ff)<<8 | (res[off+5] & 0x000000ff);
        //先頭から7ビット(0x7f)が年、４ビット(0x1)が月、残り５ビット(0x1f)が日
        this.year  = (ymdint>>9) & 0x0000007f;
        this.month = (ymdint>>5) & 0x0000000f;
        this.day   = ymdint & 0x0000001f;

        if (isBuppan(this.act_id)) {
            this.in_station = "物販";
        } else if (isBus(this.act_id)) {
            this.in_station = "バス";
        } else {
        	if(res[off+6] < 0x80){
        		this.in_line = "";
        	}else if(res[off+15] == 0x00){
        		this.in_line = "1";
        	}else{
        		this.in_line = "2";
        	}
        	if(res[off+8] < 0x80){
        		this.out_line = "";
        	}else if(res[off+15] == 0x00){
        		this.out_line = "1";
        	}else{
        		this.out_line = "2";
        	}
        }
        this.in_line += Integer.toHexString(0xff & res[off+6]);
        this.in_station = Integer.toHexString(0xff & res[off+7]);
        this.out_line += Integer.toHexString(0xff & res[off+8]);
        this.out_station = Integer.toHexString(0xff & res[off+9]);
        
        this.remain = (res[off+11] & 0x000000ff)<<8 | (res[off+10] & 0x000000ff);//little endian
        this.seqNo = (res[off+12] & 0x000000ff)<<16 | (res[off+13] & 0x000000ff)<<8 | (res[off+14] & 0x000000ff);
        this.region = res[off+15]; //15: �ｿｽ�ｿｽ�ｿｽ[�ｿｽW�ｿｽ�ｿｽ�ｿｽ�ｿｽ 
    }

    private boolean isBuppan(int act_id) {
        return act_id == 70 || act_id == 73 || act_id == 74 || act_id == 75 || act_id == 198 || act_id == 203;
    }
    private boolean isBus(int act_id) {
        return act_id == 13|| act_id == 15|| act_id ==  31|| act_id == 35;
    }

    /******************
     * getterメソッド
     */
    public String getSequenceNum(){
    	return Integer.toString(this.seqNo);
    }
    public String getDevName(){
    	DeviceCodedata devCodeData = new DeviceCodedata();
    	return devCodeData.devmap.get(this.dev_id);
    }
    public String getActName(){
    	ActCodedata actCodedata = new ActCodedata();
    	return actCodedata.actmap.get(this.act_id);
    }
    public String getDay(){
    	return "20" + Integer.toString(this.year) + "年" 
    				+ String.format(Locale.JAPAN,"%1$02d",this.month) + "月" 
    				+ String.format(Locale.JAPAN,"%1$02d",this.day) + "日";
    }
    public String getInLine(){
    	return this.in_line;
    }
    public String getInStation(){
    	return this.in_station;
    }
    public String getOutLine(){
    	return this.out_line;
    }
    public String getOutStation(){
    	return this.out_station;
    }
    public String getRemain(){
    	//return String.format(Locale.JAPAN,"%1$6d",this.remain);
    	return Integer.toString(this.remain);
    }
    public String getResolvedInLine(){
    	return this.resolved_in_line;
    }
    public String getResolvedInStation(){
    	return this.resolved_in_station;
    }
    public String getResolvedOutLine(){
    	return this.resolved_out_line;
    }
    public String getResolvedOutStation(){
    	return this.resolved_out_station;
    }
    /******************
     * setterメソッド
     */
    public void setInLine(String in_line){
    	this.in_line=in_line;
    }
    public void setInStation(String in_station){
    	this.in_station=in_station;
    }
    public void setOutLine(String out_line){
    	this.out_line=out_line;
    }
    public void setOutStation(String out_station){
    	this.out_station=out_station;
    }
    public void setResolvedInLine(String resolved_in_line){
    	this.resolved_in_line=resolved_in_line;
    }
    public void setResolvedInStation(String resolved_in_station){
    	this.resolved_in_station=resolved_in_station;
    }
    public void setResolvedOutLine(String resolved_out_line){
    	this.resolved_out_line=resolved_out_line;
    }
    public void setResolvedOutStation(String resolved_out_station){
    	this.resolved_out_station=resolved_out_station;
    }
}