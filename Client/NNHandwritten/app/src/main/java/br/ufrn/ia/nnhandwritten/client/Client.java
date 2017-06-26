package br.ufrn.ia.nnhandwritten.client;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Created by dhiogoboza on 25/04/16.
 */
public class Client {

    private static final String TAG = "NNClient";

    private Config config;

    private Socket mSocket;
    private OutputStream mOutput;
    private BufferedReader mBufferedReader;
    private boolean appOpened = true;
    private Thread mReceiveMessagesThread;

    private boolean mConnected;

    private ServerConnectionListener serverConnectionListener;

    private static Client instance;

    private final Runnable mReceiveMessagesRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Log.i(TAG, "New connection accepted " + mSocket.getInetAddress() + ": " + mSocket.getPort());
                String data;
                do {
                    data = mBufferedReader.readLine();

                    Log.d(TAG, "Data received: " + data);

                } while (appOpened);
                //mSocket.close();

            } catch (IOException ex) {
                Log.e(TAG, "In loop", ex);
            }
        }
    };

    public static Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }

        return instance;
    }

    public Client() {

    }

    public Client init(String serverAddress, int serverPort, ServerConnectionListener serverConnectionListener) {
        config = new Config();

        config.setServerAddress(serverAddress);
        config.setServerPort(serverPort);
        this.serverConnectionListener = serverConnectionListener;

        mReceiveMessagesThread = new Thread(mReceiveMessagesRunnable);

        return this;
    }

    public void connect() {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                if (mSocket != null && mSocket.isConnected()) {
                    mConnected = true;
                }

                try {
                    mSocket = new Socket(config.getServerAddress(), config.getServerPort());

                    mSocket.setKeepAlive(true);
                    mSocket.setSoTimeout(0);
                    mSocket.setTcpNoDelay(true);

                    mOutput = mSocket.getOutputStream();

                    mBufferedReader = new BufferedReader(new InputStreamReader(
                            mSocket.getInputStream(), Charset.forName("ASCII")));

                    Log.d(TAG, "Connection result: " + mSocket.isConnected());

                    mConnected =  mSocket.isConnected();

                    if (mConnected) {
                        try {
                            mReceiveMessagesThread.start();
                        } catch (Exception e) {
                            mReceiveMessagesThread = new Thread(mReceiveMessagesRunnable);
                            mReceiveMessagesThread.start();
                        }
                    }
                } catch (IOException ex) {
                    Log.e(TAG, "Could not create socket: " + config, ex);

                    mConnected = false;
                }

                return mConnected;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (aBoolean) {
                    serverConnectionListener.onConnectionSuccess();
                } else {
                    serverConnectionListener.onConnectionFail();
                }
            }
        }.execute();
    }

    public boolean disconnect() {
        Log.d(TAG, "Closing socket: " + mSocket);

        if (this.mSocket != null && this.mSocket.isConnected()) {
            try {
                mSocket.close();
                mSocket = null;

                return true;
            } catch (IOException ex) {
                Log.e(TAG, "Closing socket", ex);
            }
        }

        return true;
    }

    public void sendData(String data) {

        try {
            String toSend = data + "\r\n";

            if (mOutput != null) {
                mOutput.write(toSend.getBytes("ASCII"));
                mOutput.flush();
            } else {
                throw new IOException("Client not connected");
            }
        } catch (IOException e) {
            Log.e(TAG, "sending message", e);
        }
    }

    public boolean isConnected() {
        return mConnected;
    }

    public void shutdown() {
        appOpened = false;
    }

    public interface ServerConnectionListener {

        void onConnectionSuccess();
        void onConnectionFail();

    }
}
