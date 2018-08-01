package life.cleverize.bio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private String TAG = "MainActivity";

    //    private static MqttAndroidClient client;
    public static PahoMqttClient subClient;
    public static PahoMqttClient pubClient;
    public static PahoMqttClient subClient2;


    private EditText txtHost2Sub, txtMsg2Pub, txtTopic2Sub, txtHost2Pub, txtTopic2Pub;
    private Button btnPub, btnSub, btnUnSub;
    private ImagePro imagePro;
    private ImagePro.ImageDetails imageDetails;
    private ImageView ivCrop;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImagePro.CAMERA_CODE) {
            imageDetails = imagePro.getImagePath(ImagePro.CAMERA_CODE, RESULT_OK, data);
//            Log.d(" ### ", imageDetails.getPath() + ", " + imageDetails.getBitmap().getConfig());
        } else if (requestCode == ImagePro.GALLERY_CODE) {
            Log.d(" $$$ ","resultCode:"+resultCode);
            imageDetails = imagePro.getImagePath(ImagePro.GALLERY_CODE, RESULT_OK, data);
        }

        try {
            this.showRegisterDialog(imageDetails.getBitmap(), Files.readAllBytes(imageDetails.getFile().toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ivCrop.setImageBitmap(imageDetails.getBitmap());

//imageDetails.getPath(), imageDetails.getBitmap(), imageDetails.getUri(), imageDetails.getFile
    }

    protected String bitmapToBase64String(byte [] b) {
//        ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();
//        if ( b.compress(Bitmap.CompressFormat.JPEG, 100, jpeg_data)) {
//            byte[] code = jpeg_data.toByteArray();
            String b64Encoded = Base64.encodeToString(b, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING );

            return b64Encoded;
//        }
//        return "";
    }

    protected String getRecogReqString(byte [] b,String name) {
        String b64Encoded=bitmapToBase64String(b);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("req",""+System.currentTimeMillis());
            jsonObject.put("name",name);
            jsonObject.put("ext","jpeg");
            jsonObject.put("img",b64Encoded);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    protected  void showRegisterDialog(final Bitmap b,final byte [] ba){
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);
        View mView = layoutInflaterAndroid.inflate(R.layout.user_name_input_dialog, null);
        ImageView imageView=mView.findViewById(R.id.whosthis);
        imageView.setImageBitmap(b);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
        alertDialogBuilderUserInput.setView(mView);
        final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Register", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                        String n = userInputDialogEditText.getText().toString().trim();
                        String reqMsg=getRecogReqString(ba,n);
                        publishMsg(reqMsg);
                        dialogBox.dismiss();
                    }
                })
                .setNeutralButton("Recognize",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                String reqMsg=getRecogReqString(ba,"");
                                publishMsg(reqMsg);
                                dialogBox.dismiss();
                            }
                        });

        final AlertDialog dialog = alertDialogBuilderUserInput.create();
        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onShow(DialogInterface arg0) {
                Button negbtn=dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                Button posbtn=dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                negbtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                negbtn.setTextColor(Color.WHITE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(20,0,0,0);
                negbtn.setLayoutParams(params);

                posbtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                posbtn.setTextColor(Color.WHITE);

            }
        });
        dialog.show();
    }
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                            MY_CAMERA_REQUEST_CODE);
                }
                imagePro.openImagePickOption();

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
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

        imagePro = new ImagePro(this);
        ivCrop = (ImageView) findViewById(R.id.iv_crop);

        btnPub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = txtMsg2Pub.getText().toString().trim();
                publishMsg(msg);
            }
        });

        btnSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String host = txtHost2Sub.getText().toString().trim();
                String topic = txtTopic2Sub.getText().toString().trim();
                if (!topic.isEmpty() && !host.isEmpty()) {
                    try {
                        if (subClient == null) {
                            subClient = new PahoMqttClient(getApplicationContext(), host, topic, 1, "subby", true, null);
                            subClient2= new PahoMqttClient(getApplicationContext(), host, topic, 1, "subby2", true, null);
                            subClient2.setCallback(new MqttCallbackExtended() {
                                @Override
                                public void connectComplete(boolean b, String s) {

                                }

                                @Override
                                public void connectionLost(Throwable throwable) {

                                }

                                @Override
                                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                                    JSONObject jsonObject = new JSONObject(s);
                                    String name=jsonObject.getString("name");
                                    Snackbar.make(v, name, Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }

                                @Override
                                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                                }
                            });
                        } else if (subClient.host != host) {
                            subClient.host = host;
                            subClient.reconnect();
                            subClient2.host = host;
                            subClient2.reconnect();
                        }
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
                        subClient2.unSubscribe(topic);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void publishMsg(String msg){
        String topic = txtTopic2Pub.getText().toString().trim();
        String host = txtHost2Pub.getText().toString().trim();
        Log.i(" ### ", "host:topic:msg ==>" + host + ":" + topic + ":" + msg);

        if (!msg.isEmpty() && !topic.isEmpty() && !host.isEmpty()) {
            try {
                if (pubClient == null) {
                    pubClient = new PahoMqttClient(getApplicationContext(), host, topic, 1, "pubby", false, msg);
                } else {
                    pubClient.publishMessage(msg, 1, topic);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
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
