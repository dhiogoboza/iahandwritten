package br.ufrn.ia.nnhandwritten;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;

import br.ufrn.ia.nnhandwritten.client.Client;

public class MainActivity extends FragmentActivity implements View.OnClickListener, Client.ServerConnectionListener {

    private EditText mServerAddress;
    private EditText mServerPort;
    private View mConnectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        mServerAddress = (EditText) findViewById(R.id.fl_et_server_address);
        mServerPort = (EditText) findViewById(R.id.fl_et_server_port);

        mConnectButton = findViewById(R.id.fl_button_connect);
        mConnectButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        mConnectButton.setEnabled(false);

        DigitResolver.init(mServerAddress.getText().toString(), mServerPort.getText().toString());

        startActivity(new Intent(MainActivity.this, CameraActivity.class));
        mConnectButton.setEnabled(true);
    }

    @Override
    public void onConnectionSuccess() {
        startActivity(new Intent(MainActivity.this, CameraActivity.class));
        mConnectButton.setEnabled(true);
    }

    @Override
    public void onConnectionFail() {
        mConnectButton.setEnabled(true);
    }
}
