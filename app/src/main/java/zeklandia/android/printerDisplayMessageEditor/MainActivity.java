package zeklandia.android.printerDisplayMessageEditor;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final int printerPort = 9100;
    private final static String formatRDYMSG = "\u001B%-12345X@PJL JOB\n" +
            "@PJL RDYMSG DISPLAY=\"{0}\"\n@PJL EOJ\n\u001B%-12345X\n";
    private final static String formatOPMSG = "\u001B%-12345X@PJL JOB\n" +
            "@PJL OPMSG DISPLAY=\"{0}\"\n@PJL EOJ\n\u001B%-12345X\n";
    private final static String formatERRMSG = "\u001B%-12345X@PJL JOB\n" +
            "@PJL ERRMSG DISPLAY=\"{0}\"\n@PJL EOJ\n\u001B%-12345X\n";
    private static final Pattern IP_ADDRESS
            = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText ip = (EditText) findViewById(R.id.printerIPAddress);
                String ipString = ((EditText) ip).getText().toString();

                Matcher matcher = IP_ADDRESS.matcher(ipString);
                if (matcher.matches()) {
                } else {
                    Snackbar snackbar = Snackbar
                            .make(view, "Invalid IP address", Snackbar.LENGTH_LONG);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.RED);
                    snackbar.show();
                    return;
                }

                EditText message = (EditText) findViewById(R.id.printerMessageText);
                String messageString = ((EditText) message).getText().toString();

                RadioGroup messageType = (RadioGroup) findViewById(R.id.printerMessageType);
                RadioButton messageTypeRDY = (RadioButton) findViewById(R.id.radioButtonRDY);
                RadioButton messageTypeERR = (RadioButton) findViewById(R.id.radioButtonERR);
                RadioButton messageTypeOP = (RadioButton) findViewById(R.id.radioButtonOP);
                int radioButtonID = messageType.getCheckedRadioButtonId();
                View radioButton = messageType.findViewById(radioButtonID);

                if (messageType.indexOfChild(radioButton) == 0) {
                    new sendRDYMSGTask().execute(ipString, messageString);
                } else if (messageType.indexOfChild(radioButton) == 1) {
                    new sendERRMSGTask().execute(ipString, messageString);
                } else if (messageType.indexOfChild(radioButton) == 2) {
                    new sendOPMSGTask().execute(ipString, messageString);
                }
                Snackbar.make(view, "Sending...", Snackbar.LENGTH_INDEFINITE).show();

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    private class sendRDYMSGTask extends AsyncTask<String, Void, Integer> {
        public static final int TOO_LONG_ERROR = 0;
        public static final int CONNECTION_ERROR = 1;
        public static final int SUCCESS = 2;

        protected Integer doInBackground(String... params) {
            return sendRDYMSG(params[0], params[1]);
        }

        protected void onPostExecute(Integer result) {
            switch (result) {
                case SUCCESS:
                    Snackbar.make(findViewById(R.id.fab), "Sent.", Snackbar.LENGTH_SHORT).show();
                    break;
                case CONNECTION_ERROR:
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.fab), "Connection error", Snackbar.LENGTH_LONG);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.RED);
                    snackbar.show();
                    break;
                default:
                    break;
            }
        }

        int sendRDYMSG(String ipString, String messageString) {
            try {
                Socket socket = new Socket(ipString, printerPort);
                OutputStream out = socket.getOutputStream();
                out.write(MessageFormat.format(formatRDYMSG, messageString).getBytes());
                out.close();
                socket.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return CONNECTION_ERROR;
            } catch (IOException e) {
                e.printStackTrace();
                return CONNECTION_ERROR;
            }
            return SUCCESS;
        }
    }

    private class sendERRMSGTask extends AsyncTask<String, Void, Integer> {
        public static final int TOO_LONG_ERROR = 0;
        public static final int CONNECTION_ERROR = 1;
        public static final int SUCCESS = 2;

        protected Integer doInBackground(String... params) {
            return sendERRMSG(params[0], params[1]);
        }

        protected void onPostExecute(Integer result) {
            switch (result) {
                case SUCCESS:
                    Snackbar.make(findViewById(R.id.fab), "Sent.", Snackbar.LENGTH_SHORT).show();
                    break;
                case CONNECTION_ERROR:
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.fab), "Connection error", Snackbar.LENGTH_LONG);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.RED);
                    snackbar.show();
                    break;
                default:
                    break;
            }
        }

        int sendERRMSG(String ipString, String messageString) {
            try {
                Socket socket = new Socket(ipString, printerPort);
                OutputStream out = socket.getOutputStream();
                out.write(MessageFormat.format(formatERRMSG, messageString).getBytes());
                out.close();
                socket.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return CONNECTION_ERROR;
            } catch (IOException e) {
                e.printStackTrace();
                return CONNECTION_ERROR;
            }
            return SUCCESS;
        }
    }

    private class sendOPMSGTask extends AsyncTask<String, Void, Integer> {
        public static final int TOO_LONG_ERROR = 0;
        public static final int CONNECTION_ERROR = 1;
        public static final int SUCCESS = 2;

        protected Integer doInBackground(String... params) {
            return sendOPMSG(params[0], params[1]);
        }

        protected void onPostExecute(Integer result) {
            switch (result) {
                case SUCCESS:
                    Snackbar.make(findViewById(R.id.fab), "Sent.", Snackbar.LENGTH_SHORT).show();
                    break;
                case CONNECTION_ERROR:
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.fab), "Connection error", Snackbar.LENGTH_LONG);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.RED);
                    snackbar.show();
                    break;
                default:
                    break;
            }
        }

        int sendOPMSG(String ipString, String messageString) {
            try {
                Socket socket = new Socket(ipString, printerPort);
                OutputStream out = socket.getOutputStream();
                out.write(MessageFormat.format(formatOPMSG, messageString).getBytes());
                out.close();
                socket.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return CONNECTION_ERROR;
            } catch (IOException e) {
                e.printStackTrace();
                return CONNECTION_ERROR;
            }
            return SUCCESS;
        }
    }
}
