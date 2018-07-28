package life.cleverize.bio;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private String TAG = "MainActivity";

//    private static MqttAndroidClient client;
    public static PahoMqttClient subClient;
    public static PahoMqttClient pubClient;

    private EditText txtHost2Sub, txtMsg2Pub, txtTopic2Sub, txtHost2Pub,txtTopic2Pub;
    private Button btnPub, btnSub, btnUnSub;
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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        txtHost2Sub = (EditText) findViewById(R.id.txtHost2Sub);
        txtTopic2Sub = (EditText) findViewById(R.id.txtTopic2Sub);
        btnSub = (Button) findViewById(R.id.btnSub);
        btnUnSub = (Button) findViewById(R.id.btnUnSub);

        txtHost2Pub = (EditText) findViewById(R.id.txtHost2Pub);
        txtTopic2Pub = (EditText) findViewById(R.id.txtTopic2Pub);
        txtMsg2Pub = (EditText) findViewById(R.id.txtMsg2Pub);
        btnPub = (Button) findViewById(R.id.btnPub);


        btnPub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = txtMsg2Pub.getText().toString().trim();
                String topic = txtTopic2Pub.getText().toString().trim();
                String host=txtHost2Pub.getText().toString().trim();
                Log.i(" ### ","host:topic:msg ==>"+host+":"+topic+":"+msg);

                if (!msg.isEmpty() && !topic.isEmpty() && !host.isEmpty()) {
                    try {
                        if (pubClient == null) {
                            pubClient = new PahoMqttClient(getApplicationContext(),  host,topic,1, "pubby",false, msg);
                        } else {
                            pubClient.publishMessage( msg, 1, topic);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btnSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String host = txtHost2Sub.getText().toString().trim();
                String topic = txtTopic2Sub.getText().toString().trim();
                if (!topic.isEmpty() && !host.isEmpty()) {
                    try {
                        if (subClient == null ) {
                            subClient = new PahoMqttClient(getApplicationContext(), host,topic,1, "subby", true, null);
                        } else if (subClient.host != host) {
//                            subClient.disconnect();
                            subClient.host=host;
                            subClient.reconnect();
                        }
//                        subClient.subscribe( topic, 1); connect call back will call subscribe
                        Intent intent = new Intent(MainActivity.this, MqttMessageService.class);
                        if (MqttMessageService.isInstanceCreated())
                            stopService(intent);
                        startService(intent);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        btnUnSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topic = txtTopic2Sub.getText().toString().trim();
                if (!topic.isEmpty()) {
                    try {
                        subClient.unSubscribe(topic);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
