package com.example.peng.nfcreadwrite;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.peng.nfcreadwrite.beans.Data;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

public class MainActivity extends Activity implements View.OnClickListener {

    public static final String ERROR_DETECTED = "No NFC tag detected!";
    public static final String WRITE_SUCCESS = "Text written to the NFC tag successfully!";
    public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Tag myTag;
    Context context;
    TextView tvNFCContent;
    TextView message;
    Button btnWrite;

    Button btn_clear; //清空
    Button btn_finish;//完成
    Button btn_edit;
    Button btn_write;
    Button btn_manage;
    Button btn_read;

    LinearLayout ly_edit;

    TextView[] textViews = new TextView[6];
    EditText[] editTexts = new EditText[6];
    int[] et_ids = {R.id.et_name, R.id.et_scale, R.id.et_amount, R.id.et_offer, R.id.et_price, R.id.et_type};
    int[] tv_ids = {R.id.tv_name, R.id.tv_scale, R.id.tv_amount, R.id.tv_offer, R.id.tv_price, R.id.tv_type};
    boolean blankflag = false; //非空

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initview();
        context = this;


        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        }
        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[]{tagDetected};
    }

    //封装一波
    private void initview() {

        ly_edit = (LinearLayout) findViewById(R.id.ly_edit);
        btn_clear = (Button) findViewById(R.id.btn_clear);
        btn_finish = (Button) findViewById(R.id.btn_finish);
        btn_edit = (Button) findViewById(R.id.btn_edit);
        btn_manage = (Button) findViewById(R.id.btn_manage);
        btn_read = (Button) findViewById(R.id.btn_read);
        btn_write = (Button) findViewById(R.id.btn_write);

        for (int i = 0; i < et_ids.length; i++)
            editTexts[i] = (EditText) findViewById(et_ids[i]);
        for (int i = 0; i < tv_ids.length; i++)
            textViews[i] = (TextView) findViewById(tv_ids[i]);

        btn_finish.setOnClickListener(this);
        btn_clear.setOnClickListener(this);
        btn_edit.setOnClickListener(this);
        btn_manage.setOnClickListener(this);
        btn_read.setOnClickListener(this);
        btn_write.setOnClickListener(this);
    }

    //json相关
    //json测试
    //输入框数据封装到Data,再转为json字符串
    //用于写数据
    public String data2JsonString() {
        Gson gson = new Gson();
        //获取输入框输入的数据
        //这里不做判断，在完成按钮内做格式判断
        String name = editTexts[0].getText().toString();
        String scale = editTexts[1].getText().toString();
        int amount = Integer.parseInt(editTexts[2].getText().toString());
        String offer = editTexts[3].getText().toString();
        int price = Integer.parseInt(editTexts[4].getText().toString());
        String type = editTexts[5].getText().toString();
        //用构造函数封装成Data，然后转乘json字符串
        Data data = new Data(name, scale, amount, offer, price, type, new Date().toLocaleString());
        return gson.toJson(data);
    }

    //读取到字符串转成Data对象
    public void Str2Data(String jsonString) {
        if (jsonString == null)
            Toast.makeText(context, "无法读取数据", Toast.LENGTH_LONG).show();
        else {
            try {
                //还是得防止闪退
                Gson gson = new Gson();
                Data data = gson.fromJson(jsonString, Data.class);
                String str = gson.toJson(data);
                Log.i("Main", data.getName());
                textViews[0].setText(data.getName());
                textViews[1].setText(data.getScale());
                textViews[2].setText(String.valueOf(data.getAmount()));
                textViews[3].setText(data.getOffer());
                textViews[4].setText(String.valueOf(data.getPrice()));
                textViews[5].setText(data.getType());
                Log.i("Main", str);
            } catch (Exception e) {

            }

        }

    }

    /******************************************************************************
     **********************************Read From NFC Tag***************************
     ******************************************************************************/
    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            //解析原生数据
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);//显示数据到界面
        }
    }

    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;
        String text = "";
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }

        //用Str2Data转换读取到到string数据,并解析显示到界面
        Str2Data(text);
        //直接在这里解析读取的数据
    }


    /******************************************************************************
     **********************************Write to NFC Tag****************************
     ******************************************************************************/
    private void write(String text, Tag tag) throws IOException, FormatException {
        NdefRecord[] records = {createRecord(text)};
        NdefMessage message = new NdefMessage(records);
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();
    }

    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang = "en";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        int textLength = textBytes.length;
        byte[] payload = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);

        return recordNFC;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        readFromIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume() {
        super.onResume();
        WriteModeOn();
    }


    /******************************************************************************
     **********************************Enable Write********************************
     ******************************************************************************/
    private void WriteModeOn() {
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }

    /******************************************************************************
     **********************************Disable Write*******************************
     ******************************************************************************/
    private void WriteModeOff() {
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_edit:

                break;
            case R.id.btn_manage://仓库管理，跳转到第二个页面，或者是利用framelayout全部在主界面显示
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, OperateActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_read:  //暂定先不动，后期再试试手动读取

                break;
            case R.id.btn_write:
                try {
                    if (myTag == null) {
                        Toast.makeText(context, ERROR_DETECTED, Toast.LENGTH_LONG).show();
                    } else {
                        write(data2JsonString(), myTag); //数据显示到输入框
                        Toast.makeText(context, WRITE_SUCCESS, Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(context, WRITE_ERROR, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (FormatException e) {
                    Toast.makeText(context, WRITE_ERROR, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                break;
            case R.id.btn_clear: //清空输入框
                for (int i = 0; i < et_ids.length; i++)
                    editTexts[i].setText("");
                break;
            case R.id.btn_finish:  //点击完成后进行格式判断，简单判空
                for (int i = 0; i < et_ids.length; i++)
                    if (editTexts[i].getText() == null) {
                        blankflag = true;
                    }
                if (blankflag)
                    Toast.makeText(this, "遗漏输入,请补齐", Toast.LENGTH_LONG).show();
                break;
        }
    }
}