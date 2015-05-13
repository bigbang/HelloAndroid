package bigbang.io.helloandroid;

import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import io.bigbang.client.Action;
import io.bigbang.client.Action2;
import io.bigbang.client.AndroidBigBangClient;
import io.bigbang.client.BigBangClient;
import io.bigbang.client.Channel;
import io.bigbang.client.ChannelError;
import io.bigbang.client.ChannelMessage;
import io.bigbang.client.ConnectionError;
import io.bigbang.protocol.JsonObject;


public class MainActivity extends ActionBarActivity {

    private BigBangClient client;
    private Channel chatChannel;

    private EditText chatInput;
    private ListView chatWindow;
    private List<String> chats = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chatInput = (EditText) findViewById(R.id.chatEntry);
        chatWindow = (ListView) findViewById(R.id.listView);
        chatInput.setEnabled(false);

        Log.i("bigbang", "onCreate()");
        initChat();
    }

    private void initChat() {
        getActionBar().setTitle("CONNECTING...");

        final Handler bigBangHandler = new Handler(getMainLooper());
        client = new AndroidBigBangClient(new Action<Runnable>() {
            @Override
            public void result(Runnable result) {
                bigBangHandler.post(result);
            }
        });

        client.connect("https://demo.bigbang.io", new Action<ConnectionError>() {
            @Override
            public void result(ConnectionError error) {
                if (error != null) {
                    Log.i("bigbang", error.toString());
                } else {

                    getActionBar().setTitle("CONNECTED");
                    chatInput.setEnabled(true);
                    client.subscribe("helloChat", new Action2<ChannelError, Channel>() {
                        @Override
                        public void result(ChannelError channelError, Channel channel) {
                            chatChannel = channel;
                            channel.onMessage(new Action<ChannelMessage>() {
                                @Override
                                public void result(ChannelMessage result) {
                                    JsonObject msg = result.getPayload().getBytesAsJSON().asObject();
                                    updateChatWindow(msg.getString("msg"));
                                }
                            });
                        }
                    });
                }
            }
        });

        client.disconnected(new Action<Void>() {
            @Override
            public void result(Void result) {
                getActionBar().setTitle("DISCONNECTED");
            }
        });
    }


    private void updateChatWindow(String newChat) {
        chats.add(newChat);
        chatWindow.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, chats.toArray(new String[chats.size()])));
    }

    public void chatButtonClick(View v) {
        String inputText = chatInput.getText().toString();

        if (null != inputText && inputText.length() > 0) {
            JsonObject json = new JsonObject();
            json.putString("msg", inputText);
            chatInput.setText("");
            chatChannel.publish(json);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("bigbang", "onStop()");
    }

    @Override
    public void onRestart() {
        super.onRestart();
        Log.i("bigbang", "onRestart()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("bigbang", "onPause()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        client.disconnect();
        Log.i("bigbang", "onDestroy()");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
