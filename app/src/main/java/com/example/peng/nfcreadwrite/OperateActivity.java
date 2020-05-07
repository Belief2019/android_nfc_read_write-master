package com.example.peng.nfcreadwrite;


import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;



public class OperateActivity extends Activity {
    EditText name;
    EditText type;
    EditText scale;
    EditText  amount;
    EditText offer;
    EditText price;
    Button btn_update;
    Button btn_output;
    Button btn_input;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operate);

        name = (EditText) findViewById(R.id.op_name);
        type = (EditText) findViewById(R.id.op_type);
        scale=(EditText) findViewById(R.id.op_scale);
        amount=(EditText) findViewById(R.id.op_amount);
        offer=(EditText) findViewById(R.id.op_offer);
        price=(EditText) findViewById(R.id.op_price);
        btn_update=(Button)findViewById(R.id.btn_update);
        btn_output=(Button)findViewById(R.id.btn_output);
        btn_input=(Button)findViewById(R.id.btn_input);





    }
}
