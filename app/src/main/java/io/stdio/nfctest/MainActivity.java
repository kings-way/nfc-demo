package io.stdio.nfctest;

import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.Service;
import android.content.Intent;
import android.app.PendingIntent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;

import android.widget.Toast;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcV;
import android.nfc.tech.Ndef;
import android.nfc.tech.IsoDep;
import android.nfc.tech.TagTechnology;
import android.nfc.NfcAdapter;


public class MainActivity extends AppCompatActivity {
    private static final String[] cardtype={"Type A","Type B"};
    private TextView tv_output;
    private Button bt_send;
    private Spinner spinner_type;
    private Vibrator vibrator;
    private NfcAdapter nfc_adapter;
    private ArrayAdapter<String> array_adapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt_send = (Button)findViewById(R.id.bt_send);
        tv_output = (TextView)findViewById(R.id.tv_output);
        spinner_type = (Spinner)findViewById(R.id.spinner_cardtype);

        // set spinner data
        array_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,cardtype);
        array_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_type.setAdapter(array_adapter);
        spinner_type.setVisibility(View.VISIBLE);

        vibrator=(Vibrator)getSystemService(Service.VIBRATOR_SERVICE);
        nfc_adapter = NfcAdapter.getDefaultAdapter(getApplicationContext());



        addListenerOnButton();
    }

    @Override
    protected void onResume(){
        super.onResume();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);




        /*******************  Method 1 *******************/
        /* <p>If you pass "null" for both the "filters" and "techLists" parameters
         * that acts a wild card and will cause the foreground activity to receive all tags via the
         * {@link NfcAdapter#ACTION_TAG_DISCOVERED} intent.
         */

        // ACTION_NDEF_DISCOVERED > ACTION_TECH_DISCOVERED > ACTION_TAG_DISCOVERED
//        IntentFilter iso = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
//        String[][] techLists = new String[][]{new String[]{NfcA.class.getName()},
//                                                new String[]{NfcB.class.getName()},
//                                                new String[]{NfcV.class.getName()},
//                                                new String[]{Ndef.class.getName()},
//                                                new String[]{IsoDep.class.getName()},
//                                                };
//        nfc_adapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{iso}, techLists);


        /*******************  Method 2 *******************/
        nfc_adapter.enableForegroundDispatch(this, pendingIntent, null, null);

    }

    @Override
    protected void onPause(){
        super.onPause();
        nfc_adapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        autoReadCard(intent);
    }

    private void autoReadCard(Intent intent){
        vibrator.vibrate(100);
        Toast.makeText(this,"NFC Card Detected...", Toast.LENGTH_SHORT ).show();

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(tag != null) {
            try {
//                NfcA nfc = NfcA.get(tag);

//                nfc.connect();
//                byte[] SEND_DATA = {0x00, 0x36, 0x00, 0x00, 0x08};
//                byte[] resp = nfc.transceive(SEND_DATA);
//                tv_output.append('\n' + resp.toString());
                tv_output.setText("UID:" + bytesToHexstring(tag.getId()));
                tv_output.append('\n' + "TechList:");

                for (String i:tag.getTechList())
                {
                    tv_output.append('\n' + i);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addListenerOnButton() {
        bt_send.setOnClickListener( new View.OnClickListener() {
            public void onClick(View view) {
                if(spinner_type.getSelectedItem() == cardtype[0])
                    tv_output.append("\nType A, waiting for card...");
                else
                    tv_output.append("\nType B, waiting for card...");

                String action = getIntent().getAction();
                if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                        || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
                    Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    NfcB nfc = NfcB.get(tag);
                    String uid = nfc.getTag().getId().toString();
                    tv_output.append('\n' + uid);

                }

            }
        });
    }


    public static String bytesToHexstring(byte[] bytes) {
        char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
                '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        int length = bytes.length * 3 - 1;
        char[] buf = new char[length];
        int index = 0;
        for(byte b : bytes) {
            if (index > 0)
                buf[index++] = ':';
            buf[index++] = HEX_CHAR[b >>> 4 & 0xf];
            buf[index++] = HEX_CHAR[b & 0xf];
        }

        return new String(buf);
    }


}
