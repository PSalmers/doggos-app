package ca.patsworld.freshdoggos;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_TAKE_PHOTO = 1;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void takePic(View view) {
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;

        try {
            photoFile = createImageFile();
            currentPhotoPath = photoFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "ca.patsworld.android.fileprovider",
                    photoFile);
            photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(photoIntent, REQUEST_TAKE_PHOTO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            addToGallery(currentPhotoPath);
            setPicPreview();
        }
    }

    private void setPicPreview() {
        ImageView imageView = (ImageView) findViewById(R.id.picPreview);
        Bitmap imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);
        imageView.setImageBitmap(imageBitmap);
    }

    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "DOGGO_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void addToGallery(String photoPath) {
        Log.d("doggos", photoPath);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(photoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    public void uploadPic(View view) {
        Log.d("doggos", "uploadPic started");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("imageFile", this.fileToBytes(currentPhotoPath));
            Log.d("doggos", "File read successfully");
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        HTTPRequestMaker httpRequestMaker = new HTTPRequestMaker();
        httpRequestMaker.sendJsonToServer(jsonObject);
    }

    private byte[] fileToBytes(String path) throws IOException {
        File file = new File(currentPhotoPath);
        byte[] bytes = new byte[(int) file.length()];
        int fileReadResult;
        FileInputStream fis;

        fis = new FileInputStream(file);
        fis.read(bytes);
        fis.close();

        return bytes;
    }
}
