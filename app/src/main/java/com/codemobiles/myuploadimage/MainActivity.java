package com.codemobiles.myuploadimage;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codemobiles.cmuploadimage.util.BitmapHelper;
import com.codemobiles.cmuploadimage.util.CameraIntentHelperActivity;
import com.codemobiles.cmuploadimage.util.UploadImageUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends CameraIntentHelperActivity {

    public Handler mUIHandler = new Handler();
    private String selectedImagePath;
    private ImageView selectImageInCameraBtn;
    private ImageView selectImageInGalleryBtn;
    private TextView imageFileName;
    private ImageView imageView;
    private ImageView photoFileImageView;
    private TextView updateStatusTextView;
    private ImageView mobile_icon;
    private Bitmap mPhotoBitMap;
    public String mUploadedFileName;

    private ArrayList<LatLng> markerPoints;
    private GoogleMap mMapView;
    private TextView mLocationTextView;
    private ImageView mGeoCodingBtn;
    private EditText mAddressEditText;
    private ImageView splashImageView;
    public static int REQ_GEO_CODING_SEARCH = 1;
    private List<LatLng> listOfLatLng = new ArrayList<>();
    private GoogleApiClient googleApiClient;
    private String currentLocationStr;
    private Polygon polygon;

    // your Final lat Long Values
    private Float Latitude, Longitude;

    private ProgressBar progressBar;
    private TextView txtPercentage;
    long totalSize = 0;

    public static final int UPLOAD_OK = 0;
    public static final int UPLOAD_NOK = -1;

    private static final String TAG = "jojo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindWidget();
        setWidgetListener();
        checkSharedIntent();

        Format formatter;
        formatter = new SimpleDateFormat("dd/MMMM/yyyy", new Locale("th", "TH"));
        Log.e("codemobiles", formatter.format(new Date()));

    }


    private void checkSharedIntent() {
        // TODO Auto-generated method stub
        Intent _intent = getIntent();
        if (_intent != null) {

            Bundle extras = _intent.getExtras();
            if (extras != null) {
                Uri imageUri = (Uri) extras.get(Intent.EXTRA_STREAM);
                onPhotoUriFound(imageUri);

            }
        }
    }


    private void bindWidget() {
        mobile_icon = (ImageView) findViewById(R.id.mobile_icon);
        //photoFileImageView = (ImageView) findViewById(R.id.photofileImage);
        selectImageInCameraBtn = (ImageView) findViewById(R.id.selectImageInCameraBtn);
        selectImageInGalleryBtn = (ImageView) findViewById(R.id.selectImageInGalleryBtn);
        imageFileName = (TextView) findViewById(R.id.imagename);
        imageView = (ImageView) findViewById(R.id.imageview);
        updateStatusTextView = (TextView) findViewById(R.id.updateStatusTextView);

        //txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        //progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // old animation style
        /*
		float animDistance = getResources().getDimension(
				R.dimen.animation_distance);
		TranslateAnimation animation = new TranslateAnimation(0.0f,
				animDistance, 0.0f, 0.0f);
		animation.setDuration(2000);
		animation.setRepeatCount(100000);
		animation.setFillAfter(true);
		photoFileImageView.startAnimation(animation);
		*/

        // new animation style

        //View v = new View(this);
        //v.setBackgroundColor(Color.parseColor("#FF0000"));
       //addContentView(v, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);

/*
        float animDistance = getResources().getDimension(
                R.dimen.animation_distance);
        ObjectAnimator oa = ObjectAnimator.ofFloat(photoFileImageView, "x", 0, animDistance);
        //ObjectAnimator oa = ObjectAnimator.ofFloat(photoFileImageView, "rotation", 0, 360*3);
        //ObjectAnimator oa = ObjectAnimator.ofFloat(photoFileImageView, "alpha", 0, 1);
        oa.setDuration(2000);
        oa.setRepeatCount(10000);
        oa.start();*/

    }

    private void setWidgetListener() {

        mobile_icon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // String urlServer = "http://codemobiles.com/tutorials/upload.php";

                //image have a lat long
                /*if (checkLatLong()) {
                    Log.e(TAG,"Latitude: " + String.valueOf(Latitude));
                    Log.e(TAG,"Longtitude: " + String.valueOf(Longitude));

                } else {
                    new UploadImageTask().execute("http://cmc.bangkok.go.th/androidupload/fileUpload.php");
                }*/
                new UploadImageTask().execute("http://cmc.bangkok.go.th/androidupload/fileUpload.php");
                //Toast.makeText(getApplicationContext(), "555", Toast.LENGTH_LONG).show();

            }
        });

        // Select image from camera
        selectImageInCameraBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {

                imageFileName.setText("");
                startCameraIntent();
            }
        });

        // Select image from gallery
        selectImageInGalleryBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                startGalleryIntent();
            }
        });

    }

    private boolean checkLatLong() {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(selectedImagePath);
            String LATITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String LATITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String LONGITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String LONGITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);


            if ((LATITUDE != null) && (LATITUDE_REF != null)
                    && (LONGITUDE != null) && (LONGITUDE_REF != null)) {

                if (LATITUDE_REF.equals("N")) {
                    Latitude = convertToDegree(LATITUDE);
                } else {
                    Latitude = 0 - convertToDegree(LATITUDE);
                }

                if (LONGITUDE_REF.equals("E")) {
                    Longitude = convertToDegree(LONGITUDE);
                } else {
                    Longitude = 0 - convertToDegree(LONGITUDE);
                }
                return true;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    protected void onPhotoUriFound(Uri _photoUri) {
        super.onPhotoUriFound(_photoUri);

        // get Image File Path
        selectedImagePath = UploadImageUtils.getImageFilePath(_photoUri, this);
        imageFileName.setText(selectedImagePath);

        // get Image Bitmap
        mPhotoBitMap = BitmapHelper.readBitmap(this, _photoUri);
        if (mPhotoBitMap != null) {
            mPhotoBitMap = BitmapHelper.shrinkBitmap(mPhotoBitMap, 500,
                    rotateXDegrees);
            imageView.setImageBitmap(mPhotoBitMap);
        }

    }

    public class UploadImageTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            //called before calling doInbackground - for updateing UI
            // setting progress bar to zero
            //progressBar.setProgress(0);
            super.onPreExecute();
            updateStatusTextView.setTextColor(Color.parseColor("#FFFFFF"));
            updateStatusTextView.setText("Uploading...");
            Log.e(TAG, "onPreExecute");
        }
/*
        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBar.setProgress(progress[0]);

            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }*/

        protected String doInBackground(String... params) {

            //custom thread cannot update UI
            mUploadedFileName = UploadImageUtils.getRandomFileName();
            final String jsonString = UploadImageUtils.uploadFile(
                    mUploadedFileName, params[0], mPhotoBitMap);
            Log.e(TAG, "doInBackground");
            return jsonString;
        }


        @Override
        protected void onPostExecute(String jsonString) {
            //called after calling doInbackground - for updateing UI
            //progressBar.setVisibility(View.GONE);
            super.onPostExecute(jsonString);
            Log.e(TAG, "onPostExecute");

            try {



                JSONObject json = new JSONObject(jsonString);

                int code = json.getInt("code");
                Log.e(TAG, String.valueOf(code));



                String message = json.getString("message");
                String target_path = json.getString("target_path");

                String name = json.getString("file_name");
                String description = json.getString("description");
                String x = json.getString("x");
                String y = json.getString("y");
                String date = json.getString("date");

                //String file_path = json.getString("file_path");
                //String file_upload_url = json.getString("file_upload_url");

                Log.e(TAG, target_path);
                Log.e(TAG, name);
                //Log.e(TAG, file_path);
                //Log.e(TAG, file_upload_url);

                if (code == 0) {
                    updateStatusTextView.setText("Uploading failed!");
                    updateStatusTextView.setTextColor(Color.parseColor("#FF0000"));
                    Log.e(TAG, "onPostExecute 0");
                    Log.e(TAG, message);
                } else if (code == 1) {
                    updateStatusTextView.setText("Uploading completed");
                    updateStatusTextView.setTextColor(Color.parseColor("#5FECFF"));
                    Log.e(TAG, "onPostExecute 1");
                    Log.e(TAG, message);
                } else if (code == 2) {
                    updateStatusTextView.setText("Uploading failed");
                    updateStatusTextView.setTextColor(Color.parseColor("#FF0000"));
                    Log.e(TAG, "onPostExecute 2");
                    Log.e(TAG, message);
                } else if (code == 3) {
                    updateStatusTextView.setText("Uploading failed");
                    updateStatusTextView.setTextColor(Color.parseColor("#FF0000"));
                    Log.e(TAG, "onPostExecute 3");
                    Log.e(TAG, message);
                    //Toast.makeText(MainActivity.this, message,Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                Log.e(TAG, "onPostExecute catch");
                //Toast.makeText(this,"catcherror",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }



    }

    public void turnLocationTrackingOn(Context context){
        String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps")){ //if gps is disabled
            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            Toast.makeText(context, "Please turn on Location Tracking", Toast.LENGTH_SHORT).show();
        }
    }

    private Float convertToDegree(String stringDMS){
        Float result = null;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0/D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0/M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0/S1;

        result = new Float(FloatD + (FloatM/60) + (FloatS/3600));

        return result;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return (String.valueOf(Latitude)
                + ", "
                + String.valueOf(Longitude));
    }

    public int getLatitudeE6(){
        return (int)(Latitude*1000000);
    }

    public int getLongitudeE6(){
        return (int)(Longitude*1000000);
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
